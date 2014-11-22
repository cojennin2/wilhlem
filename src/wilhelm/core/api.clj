(ns wilhelm.core.api
  (:require [wilhelm.core.http :as http])
  (:require [wilhelm.core.cache :as cache]))

(def api "http://api.themoviedb.org/3/")
(def apikey "abac630288252315438d1c09840f4297")
(def api-cache-time 3600000)
(def api-default-paged-count 20)

; Since we're using limit/offset we need
; to normalize. Figure out what page we need to
; start with based on the offset
(defn from-offset-page-start [offset]
  (let [pages (/ offset api-default-paged-count)]
    (if (<= pages 1)
      1
      pages)))

; Standard api http request
(defn http-request [endpoint params]
  (try
    (http/get-simple-json (str api endpoint) params)
    (catch Exception e (throw e))))

; Standard api http request with caching
(defn http-request-cached [endpoint params expire]
  (let [val (cache/get-cache endpoint)]
    (if (not (nil? val))
      val
      (let [val (http-request endpoint params)]
           (let [res (cache/set-cache! endpoint val expire)]
                res)))))

; Multiple arity api call.
(defn api-call
  ([endpoint] (api-call endpoint nil nil))
  ([endpoint params] (api-call endpoint params nil))
  ([endpoint params expire]
    (let [params (assoc params :api_key apikey)
          expire (or expire api-cache-time)]
      (try
        (http-request-cached endpoint params expire)
        (catch Exception e (throw e))))))

; Multiple arity paged api call. Lazy sequence so we can do
; cool stuff like pretend an api uses limits/offsets instead of pages.
; todo: still not sure if this is the best way to call this. Offset is an argument but limit is optional?
(defn api-call-paged
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