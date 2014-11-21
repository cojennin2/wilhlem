(ns wilhelm.core.exceptional)

; This was a nifty bit of middleware I found
; Seemed convenient for this use case
(defn is-exception? [f]
  {:arglists '([handler options])}
  [handler]
  (fn [request]
    (try
      (let [response (handler request)]
        response)
      (catch Exception e
        (let [error-response (response (.getMessage e))]
             (status error-response 500)
             error-response)))))