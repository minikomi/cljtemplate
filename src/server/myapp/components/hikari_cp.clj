(ns myapp.components.hikari-cp
  (:require
   [environ.core :refer [env]]
   [hikari-cp.core :as hcp]
   [clojure.java.jdbc :as jdbc]
   [clojure.pprint :as pprint]
   [mount.core :refer [defstate]]))

(defstate datasource
  :start
  {:datasource (hcp/make-datasource {:adapter (:db-adapter env)
                                     :url "jdbc:postgresql://localhost:5432/shouter"})}
  :stop
  (hcp/close-datasource (:datasource datasource)))

(defn query [query-str]
  (jdbc/with-db-connection [conn datasource]
    (let [result (jdbc/query conn query-str)]
      (pprint/print-table result)
      result)))
