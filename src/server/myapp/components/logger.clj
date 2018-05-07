(ns myapp.components.logger
  (:require [environ.core :refer [env]]
            [mount.core :refer [defstate] :as mount]
            [taoensso.timbre :as timbre :include-macros true]
            [taoensso.timbre.appenders.core :as appenders]))

(defn setup []
  (let [fname (or (:log-file env) "log/myapp.log")
        standard-out (env :std-out false)]
    (timbre/merge-config! {:appenders
                           {:spit (appenders/spit-appender
                                   {:fname fname})}})
    (timbre/merge-config! {:appenders
                           {:println {:enabled? standard-out}}})))

(defstate logger
  :start (setup))
