(ns wilhelm.core.movies
    (:require [wilhelm.core.api :as api])
    (:require [wilhelm.core.utils :as utils])
    (:require [clojure.core.async :as async]))

; A service for movie specific information.

(def channel-movies (async/chan))
(def channel-cast (async/chan))

(defn put-movies-onto-queue [movies]
      "Puts movies into a channel. This is done inside a go-loop
      so that placing movies into the channel is async."
      (async/go-loop [movies movies]
                     (if (nil? (first movies))
                       0
                       (do
                         (async/>! channel-movies (first movies))
                         (recur (next movies))))))

(defn now-playing [offset limit]
      "Fetch movies that are now playing in theaters. Takes a limit
      and an offset."
      (let [movies (api/now-playing offset limit)]
           (do
             (put-movies-onto-queue movies)
             movies)))

; The API didn't have documentation on this endpoint,
; found it online somewhere.
(defn cast-of-movie [id]
      "Get the cast of a movie given a movie id."
      (try
        (api/movie-cast id)
        (catch Exception e (throw e))))

; There are two kinds of profiles. The "basic" profiles
; which come from "movie/:movieID/credits" and the "advanced"
; profiles which come from "person/:personID"
; The "advanced" profiles contain birthday information.

; todo: Make this use a cast id, not a profile
(defn cast-member-profile [cast-member]
      "Get the advanced profile information of a given cast member
      from a basic cast member profile"
      (try
        (api/cast-profile cast-member)
        (catch Exception e (throw e))))

(defn cast-member-age [profile]
      "Given an advanced profile get the age of a cast member."
      (let [birthday (get profile "birthday")]
           (if (nil? birthday)
             0
             (try
               (utils/get-years-since-date-ymd birthday)
               (catch Exception e 0)))))

; todo: handle cast members without birthdays (should they be removed, counted, etc?)
(defn average-age-of-cast [id]
      "Given the average age of a cast of a movie given
      a movie id."
      (try
        (let [cast (cast-of-movie id)
              cast-profiles (map cast-member-profile cast)
              cast-ages (filter utils/gtzero (map cast-member-age cast-profiles))]
             {:average_age
                       (let [total-age (reduce + cast-ages)]
                            (if (> total-age 0) ; Sometimes reduction is failing (usually when we hit rate limit). Avoid divide-by-zero
                              (/ total-age (count cast-ages))
                              0))
              :movieid id})
        (catch Exception e (throw e))))


; ####### core.async ######
; todo: Move the code below to separate namespace?.
; themoviedb.org api comes with a rate limit (5 calls/second, 10,000 calls/day)
; In order to avoid running into the rate limit and given the constraints of
; our setup we prime the cache of our program by calling cast member related
; profile information asynchronously (with a timeout so we don't overload the
; 5 calls/second constraint).

(defn listen-for-movies []
      "Listen for movies that are put in \"channel-movies\".
      This is a part of the caching pipline. This function will make
      calls to get the cast of a movie, then push \"basic\" profiles onto
      the next pipline"
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

(defn listen-for-cast-members []
      "Listen for \"basic\" cast member profiles that are put in \"channel-cast\".
      This is a part of the caching pipeline. This function will make calls to get
      the \"advanced\" cast profile information of a given cast member."
      (async/go-loop []
                     (let [cast-member (async/<! channel-cast)]
                          (do
                            (cast-member-profile cast-member)
                            (async/<! (async/timeout (rand-nth (range 500 1000))))))
                     (recur)))