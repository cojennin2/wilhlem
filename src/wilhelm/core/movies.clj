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
  (get (api/api-call (str "movie/" id "/credits")) "cast"))

; retrieve profile information on a cast member
; based on a given id
(defn cast-member-profile [cast-member]
  (api/api-call (str "person/" (get cast-member "id"))))

(defn cast-member-age [profile]
  (let [birthday (get profile "birthday")]
    (if (nil? birthday)
      0
      (utils/get-years-since-date-ymd birthday))))

; our trusty friend map reduce
; map over cast member information to get profiles
; map over profiles to get ages
; reduce to get a total of ages
; divide by number of cast members
; todo: handle cast members without birthdays
(defn average-age-of-cast [id]
  (let [cast (cast-of-movie id)]
    {:average_age
     (/
      (reduce +
        (map cast-member-age
          (map cast-member-profile cast)))
      (count cast))}))