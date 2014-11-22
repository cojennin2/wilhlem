(ns wilhelm.core.cache
  (:require [clojure.core.cache :as c]))

; A basic in memory caching solution

(def default-cache-expiration 86400000) ; default is a day
(def cache (atom (c/ttl-cache-factory {} :ttl default-cache-expiration)))

; Note the usage of swap! here. Functions in clojure.core.cache
; that you might expect to mutate the cache (evict, hit, miss)
; instead return the entirety of the cache. So when we want to mutate
; the cache, we can do so but it's done atomically. The usage of (constantly)
; is required because https://clojuredocs.org/clojure.core/swap! expects a
; function as the 3rd argument (in this case, it's a function that just returns
; the entirety of the cache.
;
; Also note I used (keyword) a lot. I'm not entirely sure if that's required
; but when I was experimenting in the clojure repl with this functionality
; it seemed to cause probelsm if the keys we're not keywords
; (eg, :i-am-a-key, "i-am-not-a-key")
; todo: double check the usages of (keyword)

(defn delete-cache! [key]
      "Delete value from cache with a given key"
      (swap! cache (constantly (c/evict @cache (keyword key)))))

(defn get-cache [key]
      "Retrieve a value from cache with a given key"
      (if (c/has? @cache (keyword key))
        (c/lookup @cache (keyword key))
        nil))

; todo: Can we use expire in options to set a cache time for individual items?
(defn set-cache! [key val & options]
      "Set a value in cache with a given key"
      (if (c/has? @cache key)
        (do
          (swap! cache (constantly (c/evict @cache (keyword key))))
          (swap! cache (constantly (c/miss @cache (keyword key) val)))
          (c/lookup @cache (keyword key)))
        (do
          (swap! cache (constantly (c/miss @cache (keyword key) val)))
          (c/lookup @cache (keyword key)))))