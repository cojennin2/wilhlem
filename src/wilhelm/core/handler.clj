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
            [wilhelm.core.utils :as utils]))

(def default-limit 20)
(def default-offset 1)

(defn get-now-playing [options]
  (let [offset (or (:offset options) default-offset)
        limit (or (:limit options) default-limit)]
    (response (movies/now-playing (utils/str-to-int offset) (utils/str-to-int limit)))))

(defn get-cast-of-movie [movieid]
  (response (movies/cast-of-movie movieid)))

(defn get-average-age-of-cast [movieid]
  (response (movies/average-age-of-cast movieid)))

(defroutes app-routes
  (GET "/" [] "<a href='https://www.youtube.com/watch?v=cdbYsoEasio'>Wilhelm</a>")
  (GET "/movies/now-playing" {params :query-params} (get-now-playing params))
  (GET "/movies/:movieid/cast/" [movieid] (get-cast-of-movie movieid))
  (GET "/movies/:movieid/average-age-of-cast" [movieid] (get-average-age-of-cast movieid))
  (route/files "/app")
  (route/not-found "Not Found"))

(def app
  (do
    (cache/connect!)
    (->
      (handler/site app-routes)
      (json/wrap-json-body)
      (json/wrap-json-response)
      (wrap-cors :access-control-allow-origin #"http://localhost:3000" :access-control-allow-methods [:get :put :post :delete]))))