(ns wilhelm.core.http
  (:require [clj-http.client :as client]))

(defn get [url options]
  (client/get url options))

(defn get-resp-body [resp]
  (:body (resp)))

(defn get-resp-code [resp]
  (:status (resp)))

(defn get-simple [url options]
  (get-resp-body (get url options)))

(defn get-simple-json [url options]
  (->
    (get-simple url options)
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