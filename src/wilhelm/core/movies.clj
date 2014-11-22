(ns wilhelm.core.movies
  (:require [wilhelm.core.api :as api]
            [wilhelm.core.utils :as utils])
  (:require [clojure.core.async :as async]))

(def channel-movies (async/chan))
(def channel-cast (async/chan))


; Kicks off our caching pipeline
(defn put-movies-onto-queue [movies]
      (async/go-loop [movies movies]
                     (if (nil? (first movies))
                       0
                       (do
                         (async/>! channel-movies (first movies))
                         (recur (next movies))))))

; Fetch results for movies that are now_playing in a given area
; This api is paged, but with a nice lazy seq we can just visualize
; it as a stream (and utilize limit/offset instead).
(defn now-playing [offset limit]
      (let [movies (take limit (drop offset (api/api-call-paged "movie/now_playing" offset)))]
           (do
             ;(put-movies-onto-queue movies)
             movies)))

; note I could not find this the api documentation. Ended up googling around
; to see if the endpoint existed and turns out it did (eg, "themoviedatabase api movie credits").
; Here we retrieve a list of cast members (basic profle information).
(defn cast-of-movie [id]
  (try
    (get (api/api-call (str "movie/" id "/credits")) "cast")
    (catch Exception e (throw e))))

; The cast-of-movie call returns basic profile information
; We need advanced profile information (advanced profile information has birthdays)
(defn cast-member-profile [cast-member]
  (try
    (api/api-call (str "person/" (get cast-member "id")))
    (catch Exception e (throw e))))

(defn cast-member-age [profile]
  (let [birthday (get profile "birthday")]
    (if (nil? birthday)
      0
      (try
        (utils/get-years-since-date-ymd birthday)
        (catch Exception e 0)))))

; our trusty friend map reduce
; We need to get the average age of the cast for a given movie.
; todo: handle cast members without birthdays (should they be removed, counted, etc?)
(defn average-age-of-cast [id]
  (try
    (let [cast (cast-of-movie id)]
      {:average_age
       (let [total-age (reduce +
                               (map cast-member-age
                                    (map cast-member-profile cast)))]
            (if (> total-age 0) ; Sometimes reduction is failing (usually when we hit rate limit). Avoid divide-by-zero
            (/ total-age (count cast))
            0))
      :movieid id})
    (catch Exception e (throw e))))


; This is an excercise in nonsense (awesome, hilarious nonsense!)

; The asynchronous portion of our movie service

; Here we work hard at avoiding hard external depdencies.
; In order to get around the rate limitations on the moviedb api, we're going to do the best
; we can to pre-cache a bunch of requests.

; Part of our caching pipeline. Here we hang out and wait for movies
; to show up on the queue. Once there's a movie, we make a call to get
; the cast of said movie (priming our cache). Then we send this the basic
; cast member profiles onto the next stage of the pipeline
(defn listen-for-movies []
      (async/go-loop []
                     (let [movie (async/<! channel-movies)
                           cast (cast-of-movie (get movie "id"))]
                          (loop [cast cast]
                                (if (nil? (first cast))
                                  0
                                  (do
                                    (async/>! channel-cast (first cast))
                                    (recur (next cast)))))
                          (async/<! (async/timeout (rand-nth (range 500 1000)))))
                     (recur)))

; This is the next stage (final stage) of the pipeline. Here we hang out and wait
; for basic cast member profiles to show up on the queue. Once we have one, we make a
; call to our advanced cast member profile api (priming our cache). At this point
; we'll have both the api call for cast members and the api calls for all advanced
; profiles of cast members hanging in cache.
(defn listen-for-cast-members []
      (async/go-loop []
                     (let [cast-member (async/<! channel-cast)]
                          (do
                            (cast-member-profile cast-member)
                            (async/<! (async/timeout (rand-nth (range 500 1000))))))
                     (recur)))