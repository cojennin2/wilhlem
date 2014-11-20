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
(defn now-playing [offset limit & options]
  (let [page (from-offset-page-start offset)]
    (take limit (api/api-call-paged page "movie/now_playing" options))))

; note I could not find this the api documentation. Ended up googling around
; to see if the endpoint existed and turns out it did (eg, "themoviedatabase api movie credits").
(defn cast [id]
  (->
    (api/api-call (str "movie/" id "/credits"))
    (get "cast")))