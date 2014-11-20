(ns wilhelm.core.utils)

(defn str-to-int [str]
  (if (number? str)
    str
    (Integer/parseInt str)))