(ns wilhelm.core.api
  (:require [wilhelm.core.http :as http])
  (:require [wilhelm.core.cache :as cache]))

(def api "http://api.themoviedb.org/3/")
(def apikey "abac630288252315438d1c09840f4297")
(def api_cache_time 500)


(defn api-call [endpoint & options]
  (let [params (merge {:api_key apikey} (:params options))
        expires (or (:expires options) api_cache_time)]
    (let [val (cache/get endpoint)]
      (if (not (nil? val))
        val
        (let [val (http/get-simple-json (str api endpoint) params)]
          (cache/set! endpoint val {:expires api_cache_time}))))))

(defn api-call-paged
  [page endpoint & options]
  (concat
    (->
      (api-call endpoint (merge {:page page} options)))
      (get "results")
      (lazy-seq (api-call-paged (+ page 1) endpoint))))