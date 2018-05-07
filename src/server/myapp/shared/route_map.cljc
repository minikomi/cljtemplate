(ns myapp.shared.route-map
  (:require [bidi.bidi :as b]))

(def admin-urls
  {"" :admin/home})

(def route-map
  [""
   {"/" :home
    ;; login
    "/login" :login/login
    "/logout" :login/logout
    ;; admin
    "/admin" admin-urls}])

(def to (partial b/path-for route-map))
