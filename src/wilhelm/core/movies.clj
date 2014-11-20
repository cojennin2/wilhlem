(ns wilhelm.core.movies
  (:require [wilhelm.core.http :as http]))

; The default result count from a query to /movie/now_playing is
; 20 movies. By default it starts from page one.
(def moviedb-result-count 20)
(def api "http://api.themoviedb.org/3")
(def apikey "abac630288252315438d1c09840f4297")

; Since we're using limit/offset we need
; to normalize. Figure out what page we need to
; start with based on the offset
(defn from-offset-page-start [offset]
    (let [pages (/ offset moviedb-result-count)]
      (if (<= pages 1)
        1
        pages)))

; A nice way to take pages and turn them into a limit.
; Make calls to a paged api a lazy list so we
; can just take an arbitrary number of items from it
; (and behind the scenes we'll recursively call the api and
; add to the list by incrementing pages).
(defn get-paged-results [page url & params]
  (concat
   (get (http/get-simple-json url (merge {:page page} params) "results")
   (lazy-seq (get-paged-results (+ page 1) url)))))

; Fetch results for movies that are now_playing in a given area
; This api is paged, but with a nice lazy list we can just visualize
; it as a stream of movies.
(defn now-playing [^int offset ^int limit]
  (let [page (from-offset-page-start offset)]
    (take limit (get-paged-results page (str api "/movie/now_playing") {:api_key apikey}))))

; note I could not find this the api documentation. Ended up googling around
; to see if the endpoint existed and turns out it did (eg, "themoviedatabase api movie credits").
(defn get-cast [id]
  (get (http/get-simple-json (str url "/movie/" id "/credits") {:api_key apikey})) "cast")