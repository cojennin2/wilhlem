(ns wilhelm.core.api
  (:require [wilhelm.core.http :as http])
  (:require [wilhelm.core.cache :as cache]))

(def api "http://api.themoviedb.org/3/")
(def apikey "abac630288252315438d1c09840f4297")
(def api_cache_time 500)

(defn api-call
  ([endpoint] (api-call endpoint nil nil))
  ([endpoint params] (api-call endpoint params nil))
  ([endpoint params expire]
    (let [params (assoc params :api_key apikey)
          expire (or expire api_cache_time)]
      (let [val (cache/get endpoint)]
        (if (not (nil? val))
          val
          (let [val (http/get-simple-json (str api endpoint) params)]
            (cache/set! endpoint val expire)))))))

(defn api-call-paged
  ([endpoint page] (api-call-paged endpoint page nil nil))
  ([endpoint page params] (api-call-paged endpoint page params nil))
  ([endpoint page params expire]
    (concat
      (->
        (api-call endpoint (assoc params "page" page) expire)
        (get "results")
        (lazy-seq (api-call-paged endpoint (+ page 1)))))))