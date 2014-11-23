(ns wilhelm.core.http
    (:require [clj-http.client :as client])
    (:require [cheshire.core :refer :all])
    (:require [clojure.core.match :as matching]))

; All functionality related to making generic http requests

; Some custom responses to normalize any error messages from responses
(def text-401 "Does't seem like you've got the correct permissions to make this call.")
(def text-404 "Hrm. This doesn't appear to exist?")
(def text-500 "Woah. Something's way off. Try again in a bit?")

; Note: clj-http does have some basic
; deserialization from json into edn but it seemed
; to throw some errors (even though they just use chesire too...?)
; todo: find out why clj-http throws exceptions when using the clj-http json parse functionality
(defn from-json-to-edn [json-string]
      "Deserialize from json into edn."
      (parse-string json-string))

; todo: Propagate meta information from the reqeust (ie: status, method, url, etc)?
(defn throw-http-exception-message [error-msg]
      "This normalizes exception messages from http requests into
      exception messages specified.."
      (throw
        (matching/match [(:status (:object (ex-data error-msg)))]
                        [401] (Exception. text-401)
                        [404] (Exception. text-404)
                        [_] (Exception. text-500))))

(defn http-get [url options]
      "Make an http get requset. Takes a url and options (mostly params)"
      (try
        (client/get url (assoc options :throw-entire-message? true))
        (catch Exception e (throw-http-exception-message e))))

(defn http-get-resp-body [resp]
      "Get the response body of an http get request"
      (:body resp))

(defn http-get-resp-code [resp]
      "Get the response code of an http get request"
      (:status (resp)))

(defn http-get-simple
      "Make an http get request and return the response body"
      [url params]
      (http-get-resp-body (http-get url {:query-params params})))

(defn http-get-simple-json
      "Make an http get request to a json endpoint,
      deserialize the response body into edn and return."
      [url params]
      (try
        (->
          (http-get-simple url params)
          (from-json-to-edn))
        (catch Exception e (throw e))))