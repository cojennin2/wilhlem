(ns wilhelm.core.exceptional
    (:use ring.util.response))

; This is actually a nifty bit of middleware
; I based off something on stackoverflow +
; the json-response-body middleware that comes with
; ring
(defn is-exception?
  {:arglists '([handler options])}
  [handler & [{:as options}]]
  (fn [request]
    (try
      (let [response (handler request)]
        response)
      (catch Exception e
        (let [error-response (response {:error true :msg (.getMessage e)})]
             (status error-response 500)
             error-response)))))