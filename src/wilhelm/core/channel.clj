(ns wilhelm.core.channel
    (:require [clojure.core.async :as async :refer :all]))

(declare ^:dynamic *channel*)

(defn init! []
      (alter-var-root (var *channel*) (constantly (async/chan))))