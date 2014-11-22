(ns wilhelm.core.api
  (:require [wilhelm.core.http :as http])
  (:require [wilhelm.core.cache :as cache]))

(def api "http://api.themoviedb.org/3/")
(def apikey "abac630288252315438d1c09840f4297")
(def api-cache-time 500)
(def api-cache-timeout 1000)
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
  (let [val (cache/get endpoint)]
    (if (not (nil? val))
      val
      (let [val (http-request endpoint params)]
        (do
          (cache/set! endpoint val expire))))))

; Standard api http request with async caching
; For this project useful since not sure if system running
; will have memcache setup correctly. If cannot retrieve from cache
; after 200ms (ie: if memcache is not running) just do a standard
; api http request
; todo: something is not right. Is an exception being thrown? Taking as long as http-request
(defn http-request-cached-async [endpoint params expire]
  (let [async-val (cache/get-async endpoint)]
    (try
      (let [val (deref async-val api-cache-timeout (http-request endpoint params))]
        (if (not (nil? val))
          val
          (let [val (http-request endpoint params)]
            (cache/set! endpoint val expire))))
      (catch Exception e
        (try
          (http-request endpoint params)
          (catch Exception e (throw e)))))))

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
(defn api-call-paged
  ([endpoint offset] (api-call-paged endpoint offset nil nil))
  ([endpoint offset params] (api-call-paged endpoint offset params nil))
  ([endpoint offset params expire]
    (let [page (from-offset-page-start offset)]
      (try
        (concat
          (->
            (api-call endpoint (assoc params :page page) expire)
            (get "results"))
            (lazy-seq (api-call endpoint (assoc params :page (+ page 1)) expire)))
        (catch Exception e (throw e))))))