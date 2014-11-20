(ns wilhelm.core.handler
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route])
  (:require [ring.util.response :refer :all]
            [ring.middleware.json :as json]
            [ring.util.response :only [response]]
            [ring.middleware.cors :refer [wrap-cors]])
  (:require [wilhelm.core.movies :as movies]))

(defn get-now-playing [options] (response (movies/now-playing options)))

(defroutes app-routes
  (GET "/" [] "<a href='https://www.youtube.com/watch?v=cdbYsoEasio'>Wilhelm</a>")
  (GET "/movies/now-playing" [limit offset] (get-now-playing {:limit limit :offset offset}))
  (route/files "/app")
  (route/not-found "Not Found"))

(def app
  (->
    (handler/site app-routes)
    (json/wrap-json-body)
    (json/wrap-json-response)
    (wrap-cors :access-control-allow-origin #"http://localhost:3000" :access-control-allow-methods [:get :put :post :delete])))