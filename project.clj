(defproject wilhelm "0.1.0-SNAPSHOT"
  :description "Find movies. Find actors. Do math"
  :url "http://i.am.not.real"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.2.0"]
                 [ring/ring-defaults "0.1.2"]
                 [ring/ring-json "0.3.1"]
                 [ring-cors "0.1.4"]
                 [ring/ring-jetty-adapter "1.3.1"]
                 [cheshire "5.3.1"]
                 [clj-http "1.0.1"]
                 [org.clojure/core.cache "0.6.4"]
                 [clj-time "0.8.0"]
                 [org.clojure/core.match "0.2.1"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]]
  :plugins [[lein-ring "0.8.13"]]
  ;:ring {:handler wilhelm.core.handler/app}
  :main wilhelm.core.handler
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})
