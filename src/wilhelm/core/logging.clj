(ns wilhelm.core.logging)

; A middleware to log incoming requests (usually for debugging)

(defn print-log [request]
      "Print a request"
      (println "##### Request #####")
      (println request)
      (println "####################"))

(defn log-me
      "This is based again off https://github.com/ring-clojure/ring-json.
      Uncomment/comment to not print/print a request"
      {:arglists '([handler options])}
      [handler]
      (fn [request]
          ;(print-log request)
          (handler request)))
