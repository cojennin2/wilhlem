(ns wilhelm.core.utils
  (:require [clj-time.core :as t]
            [clj-time.format :as f]))

(defn str-to-int [str]
  (if (number? str)
    str
    (Integer/parseInt str)))

(def ymd-parser (f/formatter "YYYY-MM-dd"))

(defn get-years-since-date-ymd [date]
  (let [then (f/parse ymd-parser date)
        now (t/now)]
    (t/in-years (t/interval then now))))