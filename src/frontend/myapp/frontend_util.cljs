(ns myapp.frontend-util
  (:require [goog.dom :as gdom]
            [goog.dom.classes :as gclass]
            [goog.events :as events]
            [goog.events.EventType :as event-type]
            [goog.functions :as gf]
            [clojure.string :as s]
            [re-frisk.core :refer [enable-re-frisk!]]
            [reagent.core :as r]
            [re-frame.core :as rf])
  (:import goog.dom.ViewportSizeMonitor
           goog.async.Throttle))

;; dev setup

(defn dev-setup []
  (if goog.DEBUG
    (do (enable-console-print!)
        (enable-re-frisk!)
        (println "dev mode"))
    (set! *print-fn* (fn [& _]))))

;; body class

(defn toggle-body-class [active class]
  (let [body js/document.body]
    (if active
      (when-not (gclass/has body class)
        (gclass/add body class))
      (when (gclass/has body class)
        (gclass/remove body class)))))

;; constants

(def transition-length 800)
(def transition-delay 4000)
(defn now [] (.now js/Date))
(defn flip [f] (fn [t] (- 1 (f (- 1 t)))))
(defn quad [i] (* i i i i))

;; polyfill

(def request-animation-frame
  (or (.-requestAnimationFrame js/window)
      (.-webkitRequestAnimationFrame js/window)
      (.-mozRequestAnimationFrame js/window)
      (.-msRequestAnimationFrame js/window)
      (.-oRequestAnimationFrame js/window)
      (let [t0 (.getTime (js/Date.))]
        (fn [f] (js/setTimeout #(f (- (.getTime (js/Date.)) t0)) 16.66666)))))

;; viewport monitoring

(defn get-viewport-props []
  (let [viewport-size (gdom/getViewportSize)
        width         (.-width viewport-size)
        height        (.-height viewport-size)]
    {:width width
     :height height}))

(def dims (r/atom (get-viewport-props)))

(defn resize-fn [_] (reset! dims (get-viewport-props)))

(def is-mobile (r/track! (fn [d] (<= (:width @d) 767)) dims))

(defn get-row-n []
  (js/Math.floor
   (/ (:width @dims) 240)))

(def viewport-monitor (ViewportSizeMonitor.))

(defonce listened
  (events/listen viewport-monitor
                 event-type/RESIZE
                 (gf/throttle resize-fn 180)))

(defn nodelist-to-seq
  "Converts nodelist to (not lazy) seq."
  [nl]
  (mapv #(.item nl %) (range (.-length nl))))

(defn extract-json-first-element
  [el]
  (as-> el x
        (gdom/getFirstElementChild x)
        (.-innerHTML x)
        (.parse js/JSON x)
        (js->clj x :keywordize-keys true)))

(defn add-scrolled-class [y]
  (toggle-body-class
   (<= 64 y) "scrolled"))

(defn init-scroll-listener [f]
  (let [thr (Throttle. #(f (.-y (gdom/getDocumentScroll))) 80)]
    (events/listen js/window event-type/SCROLL #(.fire thr)))
  (f (.-y (gdom/getDocumentScroll))))

(defn init-resize-listener [f]
  (events/listen js/window event-type/RESIZE (gf/throttle f 80))
  (f))

(defn is-scrolled-into-view [el]
  (let [el-top (.-top (.getBoundingClientRect el))]
    (and (<= 0 el-top)
         (>= (.-innerHeight js/window) el-top))))

;; annimation loop

(defn start-loop!
  [component-active inner-fn]
  (reset! component-active true)
  (let [loop-fn (fn loop-fn []
                  (when @component-active (request-animation-frame loop-fn))
                  (inner-fn))]
    (loop-fn)))

;; re-frame helper

(defn dispatch-> [data]
  (fn [ev]
    (.preventDefault ev)
    (.stopPropagation ev)
    (rf/dispatch data)))

;; anti-forgery

(defn read-anti-forgery []
  (.-value (gdom/getElement "__anti-forgery-token")))

(defn search-was-empty []
  [:h4.search-was-empty "該当する商品はありません"])

;; header menu

(defn init-header-menu []
  (let [open (gdom/getElement "header-menu-button")
        close (gdom/getElement "header-menu-close-button")]
    (events/listen open
                   event-type/CLICK
                   (fn [ev]
                     (.preventDefault ev)
                     (gclass/add js/document.body "menu-open")))
    (events/listen close
                   event-type/CLICK
                   (fn [ev]
                     (.preventDefault ev)
                     (gclass/remove js/document.body "menu-open")))))

(defn format-kw [kw]
  (-> (name kw)
      (s/replace #"[-_]" " ")
      s/capitalize))
