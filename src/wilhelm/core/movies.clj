(ns wilhelm.core.movies
  (:require [wilhelm.core.http :as http]))

; The default result count from a query to /movie/now_playing is
; 20 movies. By default it starts from page one.
(def default-number-of-results 20)
(def default-page-start 1)
(def maximum-offset 20)
(def maximum-limit 20)
(def url "http://api.themoviedb.org/3")
(def apikey "abac630288252315438d1c09840f4297")

; Since we're using limit/offset we need
; to normalize. Figure out what page we need to
; start with based on the offset
(defn from-offset-page-start [offset]
  (if (>= offset maximum-offset)
    maximum-offset
    (let [pages (/ offset default-number-of-results)]
      (if (<= pages 1)
        default-page-start
        pages))))

; A nice way to take pages and turn them into a limit.
; Make calls to a paged api a lazy list so we
; can just take an arbitrary number of items from it
; (and behind the scenes we'll recursively call the api and
; add to the list by incrementing pages).
(defn get-paged-results-from-moviedb [page endpoint]
  (concat
   (get (http/get-simple-json (str url endpoint) {:page page :api_key apikey}) "results")
   (lazy-seq (get-paged-results (+ page 1) endpoint))))

; Fetch results for movies that are now_playing in a given area
; This api is paged, but with a nice lazy list we can just visualize
; it as a stream of movies.
(defn now-playing [options]
  (let [limit (or (:limit options) default-number-of-results)
       page (from-offset-page-start (or (:offset options) default-page-start))]
    (take limit (get-paged-results-from-moviedb page "/movie/now_playing"))))

(defn get-cast [id]
  (get (http/get-simple-json (str url "/movie/" id "/credits") {:api_key apikey})) "cast")