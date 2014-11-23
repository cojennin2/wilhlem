(ns wilhelm.core.api
  (:require [wilhelm.core.http :as http])
  (:require [wilhelm.core.cache :as cache]))

; All themoviedb.org api specific functionality.
; We could keep adding new api's into the mix
; by making new api services like this one.

(def api "http://api.themoviedb.org/3/")
(def apikey "abac630288252315438d1c09840f4297")
(def api-cache-time 3600000)
(def api-default-paged-count 20)

(defn from-offset-page-start [offset]
      "Take a given offset and turn it into pages."
  (let [pages (/ offset api-default-paged-count)]
    (if (<= pages 1)
      1
      pages)))

(defn http-request [endpoint params]
      "The basic api http request. Will make http calls to json
      endpoints and return edn."
  (try
    (http/get-simple-json (str api endpoint) params)
    (catch Exception e (throw e))))

(defn http-request-cached [endpoint params expire]
      "Calls the basc api http request function and then
      places the response into the cache. Cache keys are based on the
      endpoint being called."
  (let [val (cache/get-cache endpoint)]
    (if (not (nil? val))
      val
      (let [val (http-request endpoint params)]
           (let [res (cache/set-cache! endpoint val expire)]
                res)))))

(defn api-call
      "The generic api call. Any function/service/etc that
      wants to query for information in themovidedb.org will
      usually call this function. Multiple arities to make it
      simpler."
  ([endpoint] (api-call endpoint nil nil))
  ([endpoint params] (api-call endpoint params nil))
  ([endpoint params expire]
    (let [params (assoc params :api_key apikey)
          expire (or expire api-cache-time)]
      (try
        (http-request-cached endpoint params expire)
        (catch Exception e (throw e))))))

; todo: still not sure if this is the best way to call this. Offset is an argument but limit is optional?
(defn api-call-paged
      "The generic paged api call. Any function/service/etc that
      wants to query for information that will be returned as list
      will probably want to use this function. It normalizes a paged
      response into limit/offset using a lazy seq."
  ([endpoint] (api-call-paged endpoint 1 nil nil))
  ([endpoint offset] (api-call-paged endpoint offset nil nil))
  ([endpoint offset params] (api-call-paged endpoint offset params nil))
  ([endpoint offset params expire]
    (let [page (from-offset-page-start offset)]
      (try
        (concat
          (get (api-call endpoint (assoc params :page page) expire) "results")
            (lazy-seq (api-call-paged endpoint (+ page 1) params expire)))
        (catch Exception e (throw e))))))

(defn now-playing [offset]
      (api-call-paged "movie/now_playing" offset))

(defn movie-cast [id]
      (api-call (str "movie/" id "/credits")) "cast")

(defn cast-profile [cast-member]
      (api-call (str "person/" (get cast-member "id"))))