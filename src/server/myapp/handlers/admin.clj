(ns myapp.handlers.admin
  (:require [myapp.components.queries :refer [db]]
            [myapp.handlers.util :as util]
            [myapp.pages.layout :as layout]
            [hiccup.page :as hp]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [ring.util.http-response :as http]
            [ring.util.response :as resp]
            [taoensso.timbre :as timbre]
            [clj-time.core :as clj-time]))

;; home

(defn admin-handler [req]
  (layout/render
   (layout/base-template
    [:div#admin
     (anti-forgery-field)
     [:h1 "Admin"]
     [:div#app]
     (hp/include-js "/js/admin.js")]
    {:admin true})))

;; handler map

(defn handlers [kw]
  (when-let
   [h (case kw
        (:admin/home) admin-handler
        false)]
    (-> h
        util/wrap-restrict
        util/wrap-nocache)))
