(ns wilhelm.core.cast)

(def url "http://api.themoviedb.org/3")
(def apikey "abac630288252315438d1c09840f4297")

; take a date and return the years
; since that date
(defn years-from-date [date])

; retrieve profile information on a cast member
; based on a given id
(defn get-cast-member-profile [id]
  (get (http/get-simple-json (str url "/person/" id) {:api_key apikey})))

; get the current age of a cast member given a profile
; Todo: what's the best way to handle no birthday?
(defn get-cast-member-age [profile]
  (let [birthday (or (:birthday profile) nil)]
    (if nil
      0
      (years-from-date birthday))))