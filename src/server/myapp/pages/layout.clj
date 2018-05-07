(ns myapp.pages.layout
  (:require [clj-time.local :as local]
            [hiccup.page :as hp]
            [ring.util.http-response :as http]))

(defn render [content]
  (-> content
      http/ok
      (http/content-type "text/html; charset=utf-8")))

(def admin-css
  (list
   (hp/include-css "/css/admin.css")))

(def default-css
  (list
   (hp/include-css "/css/styles.css?v=1")))

(def menu-button
  [:a#header-menu-button
   {:href "#"}
   [:span#menu-button
    [:span]
    [:span]
    [:span]]
   [:span.txt
    "MENU"]])

(def menu
  [:div#header-menu])

(def title-str "")

(defn make-title [params]
  (str title-str
       (when-let [extra (:title params)]
         (str " | " extra))))

(def description-str "")

(defn facebook [params]
  (list
   [:meta
    {:property "og:title"
     :content (make-title params)}]
   [:meta
    {:property "og:type"
     :content "website"}]
   [:meta
    {:property "og:url"
     :content ""}]
   [:meta
    {:property "og:image"
     :content ""}]
   [:meta
    {:property "og:description"
     :content (or (:description params)
                  description-str)}]))

(defn html-meta [params]
  (list
   [:meta {:charset "UTF-8"}]
   [:meta {:name "description" :content (or (:description params) description-str)}]
   [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}])
  (facebook params))

(def ga-code "")

(def google-analytics
  [:script
   (str "
    (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
    (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
    m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
    })(window,document,'script','https://www.google-analytics.com/analytics.js','ga');
    ga('create', '" ga-code "', 'auto');
    ga('send', 'pageview');")])

(defn base-template
  ([content] (base-template content {}))
  ([content params]
   (hp/html5
    [:head
     (str "<!--" (local/local-now) " -->")
     (html-meta params)
     (if (:admin params) admin-css default-css)
     [:title (make-title params)]]
    [:body {:class (:body-class params "default")}
     [:div#total-wrapper
      (when-not (:admin params)
        [:div#header
         menu-button
         menu
         [:h1#header-logo]])
      [:div#content content]
      google-analytics
      (when-not (:admin params)
        [:div#footer])]])))
