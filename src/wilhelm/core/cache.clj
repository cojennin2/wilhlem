(ns wilhelm.core.cache
  (:require [clojure.core.cache :as c]))

(def default-cache-expiration 86400000) ; default is a day
(def cache (atom (c/ttl-cache-factory {} :ttl default-cache-expiration)))

(defn delete-cache! [key]
      (swap! cache (constantly (c/evict @cache (keyword key)))))

(defn get-cache [key]
      (if (c/has? @cache (keyword key))
        (c/lookup @cache (keyword key))
        nil))

(defn set-cache! [key val & options]
      (if (c/has? @cache key)
        (do
          (swap! cache (constantly (c/evict @cache (keyword key))))
          (swap! cache (constantly (c/miss @cache (keyword key) val)))
          (c/lookup @cache (keyword key)))
        (do
          (swap! cache (constantly (c/miss @cache (keyword key) val)))
          (c/lookup @cache (keyword key)))))