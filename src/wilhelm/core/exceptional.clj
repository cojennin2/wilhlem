(ns wilhelm.core.exceptional
    (:use ring.util.response))

; Deal with exceptions

(defn is-exception?
      "Some middleware to detect exceptions and
      then wrap them in a 500 response. Based a little bit
      off some snippet on stackoverflow but corrected based
      on https://github.com/ring-clojure/ring-json"
      {:arglists '([handler options])}
      [handler & [{:as options}]]
      (fn [request]
          (try
            (let [response (handler request)]
                 response)
            (catch Exception e
              (let [error-response (response {:error true :msg (.getMessage e)})]
                   (status error-response 500))))))