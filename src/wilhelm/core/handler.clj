(ns wilhelm.core.handler
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route])
  (:require [ring.util.response :refer :all]
            [ring.middleware.json :as json]
            [ring.util.response :only [response]]
            [ring.middleware.cors :refer [wrap-cors]])
  (:require [wilhelm.core.movies :as movies]
            [wilhelm.core.cache :as cache]
            [wilhelm.core.utils :as utils]
            [wilhelm.core.logging :as log]
            [wilhelm.core.exceptional :as exceptional]))

(def default-limit 20)
(def default-offset 0)

(defn get-now-playing [options]
  (let [offset (or (get options "offset") default-offset)
        limit (or (get options "limit") default-limit)]
    (response (movies/now-playing (utils/str-to-int offset) (utils/str-to-int limit)))))

(defn get-cast-of-movie [movieid]
  (response (movies/cast-of-movie movieid)))

(defn get-average-age-of-cast [movieid]
  (response (movies/average-age-of-cast movieid)))

(defroutes app-routes
  (route/resources "/")
  (GET "/movies/now-playing" {params :query-params} (get-now-playing params))
  (GET "/movies/:movieid/cast" [movieid] (get-cast-of-movie movieid))
  (GET "/movies/:movieid/average-age-of-cast" [movieid] (get-average-age-of-cast movieid))
  (route/not-found "Not Found"))

(def app
  (do
    (cache/connect!)
    ; Kick off priming cache at application start
    (movies/listen-for-movies)
    (movies/listen-for-cast-members)
    (movies/now-playing)
    (->
      (handler/site app-routes)
      (exceptional/is-exception?)
      (log/log-me)
      (json/wrap-json-body)
      (json/wrap-json-response)
      (wrap-cors :access-control-allow-origin #"\*" :access-control-allow-methods [:get :put :post :delete]))))