(ns wilhelm.core.http
  (:require [clj-http.client :as client])
  (:require [cheshire.core :refer :all]))

(defn from-json-to-edn [json-string]
  (parse-string json-string))

(defn get [url options]
  (client/get url options))

(defn get-resp-body [resp]
  (:body resp))

(defn get-resp-code [resp]
  (:status (resp)))

(defn get-simple [url params]
  (get-resp-body (get url {:query-params params})))

(defn get-simple-json [url params]
  (->
    (get-simple url params)
    (from-json-to-edn)))

(defn iserror? [resp]
  (if
    (>= (:status resp) 500)
    true
    false))

(defn ismissing? [resp]
  (if
    (= (:status resp ) 404)
    true
    false))

(defn isproblem? [resp]
  (if
    (or (iserror? resp) (ismissing? resp))
    true
    false))