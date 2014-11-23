(ns wilhelm.core.utils
    (:require [clj-time.core :as t]
      [clj-time.format :as f]))

; Some basic utility functions.

(defn str-to-int [str]
      "Takes a string and uses Java's parseInt to turn it into
      an integer."
      (if (number? str)
        str
        (Integer/parseInt str)))

(def ymd-parser (f/formatter "YYYY-MM-dd"))

(defn get-years-since-date-ymd [date]
      "Get the years since a given data and right now"
      (let [then (f/parse ymd-parser date)
            now (t/now)]
           (t/in-years (t/interval then now))))

(defn gtzero [val]
      (> val 0))