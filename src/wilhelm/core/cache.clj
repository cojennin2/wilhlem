(ns wilhelm.core.cache
  (:require [clojurewerkz.spyglass.client :as cache]))

(declare ^:dynamic *memcache*)

(def servers "127.0.0.1:11211")

(defn set! [key val & options]
  (let [expire (or (:expire options) 500)]
    (cache/set *memcache* key expire val)))

(defn delete [key]
  (cache/delete *memcache* key))

(defn get-async [key]
  (cache/async-get *memcache* key))

(defn get [key]
  (cache/get *memcache* key))

(defn connect! []
  (defn connect! []
    (alter-var-root (var *memcache*) (constantly (cache/text-connection servers)))))