(ns wilhelm.core.http
  (:require [clj-http.client :as client])
  (:require [cheshire.core :refer :all])
  (:require [clojure.core.match :only (match)]))

(def text-401 "There was an error with the API key. Is it correct?")
(def text-404 "Hrm. This doesn't appear to exist?")
(def text-500 "Woah. Something's way off. Try again in a bit?")

; clj-http has some support for deserializing into
; json, clojure data, etc. Ended up running into
; issues with it so opting for chesire for the time
; being. Can revisit later.
(defn from-json-to-edn [json-string]
  (parse-string json-string))

(defn app-specific-http-exception-messages [error-msg]
      (throw
        (match [(:status error-msg)]
             [401] (Exception. text-401)
             [404] (Exception. text-404)
             [500] (Exception. text-500))))

; Todo: what's the best way to handle anomalies? (anything that is not a 200, 301, 302).
; Need to propagate errors.
(defn get [url options]
  (try
    (client/get url (assoc options :throw-entire-message? true)
    (catch Exception e (throw-app-specific-http-exception e))))

(defn get-resp-body [resp]
  (:body resp))

(defn get-resp-code [resp]
  (:status (resp)))

; Simplified request. Just takes url + params
(defn get-simple
  [url params]
    (get-resp-body (get url {:query-params params})))

; Simpler simplified request. Just takes url + params
; and coerce response body from json to edn
(defn get-simple-json
  [url params]
  (try
    (->
      (get-simple url params)
      (from-json-to-edn)))
  (catch Exception e (throw e)))

; Helper methods to determine the response
; todo: find a use for these? Currently unused, but seem like they could come in handy
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