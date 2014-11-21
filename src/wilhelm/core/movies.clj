(ns wilhelm.core.movies
  (:require [wilhelm.core.api :as api]
            [wilhelm.core.utils :as utils]))

; Fetch results for movies that are now_playing in a given area
; This api is paged, but with a nice lazy list we can just visualize
; it as a stream of movies.
(defn now-playing [offset limit]
    (take limit (api/api-call-paged "movie/now_playing" offset)))

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
      (utils/get-years-since-date-ymd birthday))))

; our trusty friend map reduce
; We need to get the average age of the cast for a given movie.
; There are two endpoints we need. One endpoint gets us some
; basic credit infor for all cast members in the movie. The other endpoint
; will get us more in depth personal info (like birthday). The easiest way
; to handle this is to map over the basic info to make calls to advanced info
; and then map over that to get ages. Conveniently reduce and take average.
; todo: handle cast members without birthdays (should they be removed, counted, etc?)
(defn average-age-of-cast [id]
  (try
    (let [cast (cast-of-movie id)]
      {:average_age
      (/
        (reduce +
          (map cast-member-age
            (map cast-member-profile cast)))
        (count cast))
      :movieid id})
    (catch Exception e (throw e))))