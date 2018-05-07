(ns myapp.pages.util
  (:require [clojure.data.json :as json]
            [clojure.string :as s]))

(defn json-blob [data] [:script {:type "text/json"} (json/write-str data)])

(defn paragraphify [& st]
  (map (fn [lns]
         (into [:p] (interpose [:br] (s/split-lines lns))))
       (s/split (apply str st) #"\n\n")))
