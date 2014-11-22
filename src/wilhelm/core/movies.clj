(ns wilhelm.core.movies
  (:require [wilhelm.core.api :as api]
            [wilhelm.core.utils :as utils])
  (:require [clojure.core.async :as async]))

(def channel-cast (async/chan))
(def channel-profile (async/chan))

(defn put-movies-onto-queue [movies]
      (async/go-loop [movies movies]
                     (if (nil? (first movies))
                       0
                       (do
                         (async/>! channel-cast (first movies))
                         (recur (next movies))))))

; Fetch results for movies that are now_playing in a given area
; This api is paged, but with a nice lazy list we can just visualize
; it as a stream of movies.
(defn now-playing [offset limit]
      (let [movies (take limit (drop offset (api/api-call-paged "movie/now_playing" offset)))]
           (do
             (put-movies-onto-queue movies)
             movies)))

; note I could not find this the api documentation. Ended up googling around
; to see if the endpoint existed and turns out it did (eg, "themoviedatabase api movie credits").
(defn cast-of-movie [id]
  (try
    (get (api/api-call (str "movie/" id "/credits")) "cast")
    (catch Exception e (throw e))))

; retrieve profile information on a cast member
; based on a given id
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
; There are two endpoints we need. One endpoint gets us some
; basic credit infor for all cast members in the movie. The other endpoint
; will get us more in depth personal info (like birthday). The easiest way
; to handle this is to map over the basic info to make calls to advanced info
; and then map over that to get ages. Conveniently reduce and take average.
; todo: handle cast members without birthdays (should they be removed, counted, etc?)
; todo: We're running up hard against the api rate limit. Need to pre-cache actors
(defn average-age-of-cast [id]
  (try
    (let [cast (cast-of-movie id)]
      {:average_age
       (let [total-age (reduce +
                               (map cast-member-age
                                    (map cast-member-profile cast)))]
            (if (> total-age 0) ; Sometimes reduction is failing (hitting limt). Avoid divide-by-zero
            (/ total-age (count cast))
            0))
      :movieid id})
    (catch Exception e (throw e))))


(defn listen-for-movies []
      (async/go-loop []
                     (let [movie (async/<! channel-cast)
                           cast (cast-of-movie (get movie "id"))]
                          (loop [cast cast]
                                (if (nil? (first cast))
                                  0
                                  (do
                                    (async/>! channel-profile (first cast))
                                    (recur (next cast)))))
                          (async/<! (async/timeout (rand-nth (range 500 1000)))))
                     (recur)))

(defn listen-for-cast-members []
      (async/go-loop []
                     (let [cast-member (async/<! channel-profile)]
                          (do
                            (cast-member-profile cast-member)
                            (async/<! (async/timeout (rand-nth (range 500 1000))))))
                     (recur)))