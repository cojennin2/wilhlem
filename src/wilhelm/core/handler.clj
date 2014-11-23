(ns wilhelm.core.handler
    (:gen-class)
    (:require [compojure.core :refer :all]
      [compojure.handler :as handler]
      [compojure.route :as route])
    (:require [ring.util.response :refer :all]
      [ring.middleware.json :as json]
      [ring.util.response :only [response]]
      [ring.middleware.cors :refer [wrap-cors]])
    (:use ring.adapter.jetty)
    (:require [wilhelm.core.movies :as movies]
      [wilhelm.core.cache :as cache]
      [wilhelm.core.utils :as utils]
      [wilhelm.core.logging :as log]
      [wilhelm.core.exceptional :as exceptional]))

; API Controller

(def default-limit 20)
(def default-offset 0)

(defn get-now-playing [options]
      "Gets movies that are now playing in theaters."
      (let [offset (or (get options "offset") default-offset)
            limit (or (get options "limit") default-limit)]
           (response (movies/now-playing (utils/str-to-int offset) (utils/str-to-int limit)))))

(defn get-cast-of-movie [movieid]
      "Gets the cast of a given movie (by movie id)"
      (response (movies/cast-of-movie movieid)))

(defn get-average-age-of-cast [movieid]
      "Get's the average age of the cast of a given movie (by movie id)"
      (response (movies/average-age-of-cast movieid)))

(defroutes app-routes
           (route/resources "/")
           (GET "/movies/now-playing" {params :query-params} (get-now-playing params))
           (GET "/movies/:movieid/cast" [movieid] (get-cast-of-movie movieid))
           (GET "/movies/:movieid/average-age-of-cast" [movieid] (get-average-age-of-cast movieid))
           (route/not-found "Not Found"))

(def app
  (->
    (handler/site app-routes)
    (exceptional/is-exception?)
    (log/log-me)
    (json/wrap-json-body)
    (json/wrap-json-response)
    (wrap-cors :access-control-allow-origin #"\*" :access-control-allow-methods [:get :put :post :delete])))

; Before we even start our server we can start
; kicking off our caching pipeline. In fact, if we wanted to
; we could throw a set timeout to give the caching pipeline more time
; to work before server startup.
(defn -main [& args]
      (movies/now-playing 0 20)
      (run-jetty app {:port 8080}))