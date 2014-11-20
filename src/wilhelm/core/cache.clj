(ns wilhelm.core.cache)

(set! bucket {})

; todo: persist nil

(def update! [key val & options]
  (let [expire (or (:expire options) 0)]
    )

(def get [key])