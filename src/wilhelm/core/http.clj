(ns wilhelm.core.http
  (:require [clj-http.client :as client])
  (:require [cheshire.core :refer :all])
  (:require [clojure.core.match :as matching]))

; Some custom responses to normalize any error messages
; from our http requests.
(def text-401 "Does't seem like you've got the correct permissions to make this call.")
(def text-404 "Hrm. This doesn't appear to exist?")
(def text-500 "Woah. Something's way off. Try again in a bit?")

; clj-http has some support for deserializing into
; json, clojure data, etc. Ended up running into
; issues with it so opting for chesire for the time
; being. Can revisit later.
(defn from-json-to-edn [json-string]
  (parse-string json-string))

; This just adds normalized error messaging to the app
; A sharp thing to do if there's time would be to add
; some metadata to this error so that caller's could make decisions
; about whether to throw new error messages with more specific info
; (ie: if I call http/get and I know a 401 means my api key is wrong,
; I may want to make a more specific error message to propagate to the user).
(defn throw-http-exception-message [error-msg]
      (throw
        (matching/match [(:status (:object (ex-data error-msg)))]
             [401] (Exception. text-401)
             [404] (Exception. text-404)
             [_] (Exception. text-500))))

; Todo: what's the best way to handle anomalies? (anything that is not a 200, 301, 302).
; Need to propagate errors.
(defn get [url options]
  (try
    (client/get url (assoc options :throw-entire-message? true))
    (catch Exception e (throw-http-exception-message e))))

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
      (from-json-to-edn))
  (catch Exception e (throw e))))