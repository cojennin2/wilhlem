(ns wilhelm.core.movies
  (:require [wilhelm.core.api :as api]))

; The default result count from a query to /movie/now_playing is
; 20 movies. By default it starts from page one.
(def moviedb-result-count 20)

; Since we're using limit/offset we need
; to normalize. Figure out what page we need to
; start with based on the offset
(defn from-offset-page-start [offset]
    (let [pages (/ offset moviedb-result-count)]
      (if (<= pages 1)
        1
        pages)))

; Fetch results for movies that are now_playing in a given area
; This api is paged, but with a nice lazy list we can just visualize
; it as a stream of movies.
(defn now-playing [offset limit]
  (let [page (from-offset-page-start offset)]
    (take limit (api/api-call-paged "movie/now_playing" page))))


; note I could not find this the api documentation. Ended up googling around
; to see if the endpoint existed and turns out it did (eg, "themoviedatabase api movie credits").
(defn cast-of-movie [id]
  (->
    (api/api-call (str "movie/" id "/credits"))
    (get "cast")))

; retrieve profile information on a cast member
; based on a given id
(defn get-cast-member-profile [cast-member]
  (get (api/api-call (str url "/person/" (:id cast-member)))))

; our trusty friend map reduce
; map over cast member information to get profiles
; map over profiles to get ages
; reduce to get a total of ages
; divide by number of cast members
; todo: handle cast members without birthdays
(defn average-age-of-cast [id]
  (let [cast (cast-of-movie id)]
    (/
      (reduce +
        (map
          (get-cast-member-age
            (map
              (get-cast-profile cast)))))) (count cast)))