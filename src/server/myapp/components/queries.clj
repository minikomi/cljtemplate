(ns myapp.components.queries
  (:require [hugsql.core :as hugsql]
            [mount.core :refer [defstate]]
            [myapp.components.hikari-cp :refer [datasource]]
            [clojure.java.io :as io]))

(def queries-file "queries/app.sql")

(defstate db
  :start
  (let [map-of-fns (hugsql/map-of-db-fns queries-file)]
    (fn query-fn
      ([]
       (doseq [[k {:keys [meta]}] map-of-fns]
         (println k)
         (println " " (:doc meta))
         (println  " " "command:" (:command meta) "result:" (:result meta))
         (println)))
      ([kw]
       (query-fn kw {}))
      ([kw opts]
       (if-let [f (get-in map-of-fns [kw :fn])]
         (f datasource opts)
         (throw (Exception. (str "No such sql fn [" kw "]"))))))))
