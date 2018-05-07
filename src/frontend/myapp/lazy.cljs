(ns myapp.lazy
  (:require [reagent.core :as r]
            [goog.dom :as gdom]
            [goog.events.EventType :as event-type]
            [goog.events :as events])
  (:import goog.async.Throttle))

(defonce lazy-els (atom {}))
(defonce lazy-loaded (r/atom #{}))

(defn is-scrolled-into-view [el]
  (let [rect (.getBoundingClientRect el)
        el-top (.-top rect)
        el-height (.-height rect)]
    (and (<= (- 0 el-height) el-top)
         (>= (.-innerHeight js/window) el-top))))

(defn unregister-lazy-load [el-uuid]
  (swap! lazy-els dissoc el-uuid))

(defn check-offsets [_]
  (doseq [[el-uuid [el src]] @lazy-els]
    (when (is-scrolled-into-view el)
      (let [img-obj (js/Image.)]
        (set! (.-onload img-obj)
              (fn [_]
                (swap! lazy-loaded conj src)))
        (set! (.-src img-obj) src))
      (unregister-lazy-load el-uuid))))

(defn register-lazy-load [src el]
  (when-not (@lazy-loaded src)
    (if (is-scrolled-into-view el)
      (swap! lazy-loaded conj src)
      (swap! lazy-els assoc (random-uuid) [el src]))))

(defn -mount-or-update [this huh]
  (register-lazy-load (:src (r/props this)) (r/dom-node this)))

(defn lazy-loading-image [{:keys [wrapper-class src alt]}]
  (r/create-class
   {:component-did-mount
    -mount-or-update
    :component-did-update
    -mount-or-update
    :component-will-unmount
    (fn [this _]
      (unregister-lazy-load (r/dom-node this)))
    :reagent-render
    (fn [{:keys [wrapper-class src alt]}]
      [:span.lazy-wrapper
       {:class (str wrapper-class (if (get @lazy-loaded src)
                                    " loaded"
                                    " loading"))
        :key ["wrapper" src]}
       (if-not (get @lazy-loaded src)
         [:span.loader]
         [:img.loaded {:src src :alt alt}])])}))

(defn init-scroll-listener [f]
  (let [thr (Throttle. #(f (.-y (gdom/getDocumentScroll))) 300)]
    (events/listen js/window event-type/RESIZE #(.fire thr))
    (events/listen js/window event-type/SCROLL #(.fire thr)))
  (f (.-y (gdom/getDocumentScroll))))

(defonce initialized-lazy-load
  (do
    (init-scroll-listener check-offsets)
    true))
