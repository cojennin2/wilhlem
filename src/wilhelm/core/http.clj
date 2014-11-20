(ns wilhelm.core.http
  (:require [clj-http.client :as client])
  (:require [cheshire.core :refer :all]))

; clj-http has some support for deserializing into
; json, clojure data, etc. Ended up running into
; issues with it so opting for chesire for the time
; being. Can revisit later.
(defn from-json-to-edn [json-string]
  (parse-string json-string))

; Todo: what's the best way to handle anomalies? (anything that is not a 200, 301, 302).
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