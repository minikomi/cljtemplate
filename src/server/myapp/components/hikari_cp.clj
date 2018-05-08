(ns myapp.components.hikari-cp
  (:require
   [environ.core :refer [env]]
   [hikari-cp.core :as hcp]
   [clojure.java.jdbc :as jdbc]
   [clojure.pprint :as pprint]
   [mount.core :refer [defstate]]))

(defstate datasource
  :start
  {:datasource (hcp/make-datasource {:adapter "postgresql"
                                     :url (or
                                           (System/getenv "DATABASE_URL")
                                           (:db-url env))})}
  :stop
  (hcp/close-datasource (:datasource datasource)))

(defn query [query-str]
  (jdbc/with-db-connection [conn datasource]
    (let [result (jdbc/query conn query-str)]
      (pprint/print-table result)
      result)))
