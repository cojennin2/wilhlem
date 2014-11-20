(ns wilhelm.core.handler
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route])
  (:require [ring.util.response :refer :all]
            [ring.middleware.json :as json]
            [ring.util.response :only [response]]
            [ring.middleware.cors :refer [wrap-cors]])
  (:require [wilhelm.core.movies :as movies]))

(def default-limit 20)
(def default-offset 1)

(defn get-now-playing [options]
  (let [offset (or (:offset options) default-page-start)
        limit (or (:limit options) default-page-start)])
  (response (movies/now-playing offset limit)))

(defroutes app-routes
  (GET "/" [] "<a href='https://www.youtube.com/watch?v=cdbYsoEasio'>Wilhelm</a>")
  (GET "/movies/now-playing" {params :query-params} (get-now-playing params))
  (route/files "/app")
  (route/not-found "Not Found"))

(def app
  (->
    (handler/site app-routes)
    (json/wrap-json-body)
    (json/wrap-json-response)
    (wrap-cors :access-control-allow-origin #"http://localhost:3000" :access-control-allow-methods [:get :put :post :delete])))