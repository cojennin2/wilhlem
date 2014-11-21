(ns wilhelm.core.api
  (:require [wilhelm.core.http :as http])
  (:require [wilhelm.core.cache :as cache]))

(def api "http://api.themoviedb.org/3/")
(def apikey "abac630288252315438d1c09840f4297")
(def api_cache_time 500)
(def api_cache_timeout 200)

; Standard api http request
(defn http-request [endpoint params]
  (http/get-simple-json (str api endpoint) params))

; Standard api http request with caching
(defn http-request-cached [endpoint params expire]
  (let [val (cache/get endpoint)]
    (if (not (nil? val))
      val
      (let [val (http-request endpoint params)]
        (cache/set! endpoint val expire)))))

; Standard api http request with async caching
; For this project useful since not sure if system running
; will have memcache setup correctly. If cannot retrieve from cache
; after 200ms (ie: if memcache is not running) just do a standard
; api http request
(defn http-request-cached-async [endpoint params expire]
  (let [async-val (cache/get-async endpoint)]
    (try
      (let [val (deref async-val api_cache_timeout (http-request endpoint params))]
      (if (not (nil? val))
        val
        (let [val (http-request endpoint params)]
          (cache/set! endpoint val expire))))
      (catch Exception e (http-request endpoint params)))))

; Multiple arity api call.
(defn api-call
  ([endpoint] (api-call endpoint nil nil))
  ([endpoint params] (api-call endpoint params nil))
  ([endpoint params expire]
    (let [params (assoc params :api_key apikey)
          expire (or expire api_cache_time)]
      (http-request-cached-async endpoint params expire))))

; Multiple arity paged api call. Lazy sequence so we can do
; cool stuff like pretend an api uses limits/offsets instead of pages.
(defn api-call-paged
  ([endpoint page] (api-call-paged endpoint page nil nil))
  ([endpoint page params] (api-call-paged endpoint page params nil))
  ([endpoint page params expire]
    (concat
      (->
        (api-call endpoint (assoc params :page page) expire)
        (get "results"))
      (lazy-seq (api-call endpoint (assoc params :page (+ page 1)) expire)))))