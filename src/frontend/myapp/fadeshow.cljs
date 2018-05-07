(ns myapp.fadeshow
  (:import [goog.dom ViewportSizeMonitor])
  (:require [reagent.core :as r]
            [clojure.string :as s]
            [myapp.frontend-util :as u]
            [goog.dom :as gdom]
            [goog.dom.classes :as gclass]
            [goog.dom.dataset :as dset]
            [goog.functions]
            [goog.events :as goog-events]
            [goog.events.EventType :as event-type]))

(defn last-img-fn [{:keys [images transitioning transition-start current-n next-n]}]
  (when @transitioning
    (reset! current-n
            (if (zero? @current-n) (dec (count images)) (dec @current-n))))
  (reset! next-n (if (zero? @current-n) (dec (count images)) (dec @current-n)))
  (reset! transition-start (u/now))
  (reset! transitioning true))

(defn next-img-fn [{:keys [images transitioning transition-start current-n next-n]}]
  (when @transitioning (reset! current-n (mod (inc @current-n) (count images))))
  (reset! next-n (mod (inc @current-n) (count images)))
  (reset! transition-start (u/now))
  (reset! transitioning true))

(defn slide-panel [locals]
  (let [dom-node (atom nil)]
    (fn [{:keys [hovered transitioning images click-change next-opacity next-n current-n]}]
      [:div.slides
       (let [finger-down (atom false)
             touch-down-y (atom nil)
             trigger-transition
             (fn [ev]
               (.preventDefault ev)
               (.stopPropagation ev)
               (if
                   (or
                    (and (= "click" (.-type ev))
                         (>= (/ (.. @dom-node -clientWidth) 2)
                             (.. ev -nativeEvent -offsetX)))
                    (and (= "touchend" (.-type ev))
                         (>= (/ (.. @dom-node -clientWidth) 2)
                             (.. (aget (.. ev -changedTouches) 0) -pageX))))
                 (last-img-fn locals)
                 (next-img-fn locals)))]
         {:ref (fn [el] (reset! dom-node el))
          :on-mouse-over (fn [ev] (when-not @u/is-mobile (reset! hovered true)))
          :on-mouse-out (fn [ev] (reset! hovered false))
          :on-click (fn [ev]
                      (println click-change)
                      (when (and click-change (not @u/is-mobile))
                        (trigger-transition ev)))
          :on-touch-end trigger-transition})
       (when @transitioning
         [:div.next-img {:style {:opacity ((u/flip u/quad) @next-opacity)}}
          [:img {:src (get images (mod @next-n (count images)))}]])
       [:div.current-img
        [:img {:src (get images (mod @current-n (count images)))}]]])))

(defn create-loop-fn [{:keys [auto-change
                              transitioning
                              next-opacity
                              transition-start
                              transition-length
                              transition-delay
                              current-n
                              next-n
                              hovered]
                       :as locals}]
  (fn []
    (if @transitioning
      (do (reset! next-opacity
                  (/ (- (u/now) @transition-start) transition-length))
          (when (<= 1 @next-opacity)
            (reset! transitioning false)
            (reset! current-n @next-n)))
      (when auto-change
        (when (and (not @hovered)
                   (< (+ transition-delay transition-length)
                      (- (u/now) @transition-start)))
          (next-img-fn locals))))))

(defn loaded [images {:keys [auto-change
                             transition-delay
                             transition-length
                             click-change]}]
  (let [component-active (atom false)
        locals           {:images images
                          :next-opacity (r/atom 0)
                          :current-n (r/atom 0)
                          :next-n (r/atom 1)
                          :transitioning (r/atom false)
                          :transition-start (r/atom (u/now))
                          :hovered (r/atom false)
                          ;; options
                          :auto-change (if (nil? auto-change)
                                         true
                                         auto-change)
                          :transition-delay (or transition-delay 4000)
                          :transition-length (or transition-length 600)
                          :click-change (if (nil? click-change)
                                          true
                                          click-change)
                          }]
    (r/create-class
     {:component-did-mount
      (fn [_]
        (u/start-loop! component-active
                       (create-loop-fn locals)))
      :component-will-unmount
      (fn [_] (reset! component-active false))
      :reagent-render
      (fn [images _]
        [:div.slideshow-app
         [:div.slide-wrap
          [slide-panel locals]]])})))

(defn loading [unloaded images max-width]
  [:div.slide-wrap [:div.slides.loading [:div.current-img [:img {:src (first images)}]]]
   [:div.loading-container
    (for [i images]
      ^{:key i}
      [:img
       {:src i
        :on-load (fn [ev]
                   (when (< @max-width (.-width (.-target ev)))
                     (reset! max-width (.-width (.-target ev))))
                   (swap! unloaded disj i))}])]])

(defn desktop-slideshow [{:keys [images options]}]
  (let [unloaded  (r/atom (set images))
        max-width (atom 0)]
    (r/create-class
     {:reagent-render
      (fn [{:keys [images options]}]
        (if (empty? @unloaded)
          [loaded images
           (assoc options :max-width @max-width)]
          [loading unloaded images max-width]))})))

(defn slideshow [data]
  [:div
   [desktop-slideshow data]])

(defn init-fadeshows []
  (doall
   (for [s (u/nodelist-to-seq (gdom/getElementsByClass "fadeshow"))]
     (let [data (u/extract-json-first-element s)
           wrapper (gdom/getElementByClass "fadeshow-wrapper" s)]
       (r/render [slideshow data] wrapper)))))
