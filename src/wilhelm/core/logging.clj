(ns wilhelm.core.logging)

; Dumb logging middleware
; based on the right-json middleware
(defn print-log [request]
      (println "##### Request #####")
      (println request)
      (println "####################"))

(defn log-me
      {:arglists '([handler options])}
      [handler]
      (fn [request]
          (println request)
          (handler request)))
