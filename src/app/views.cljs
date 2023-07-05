(ns app.views
  (:require
    [reagent.core :as r]
    [app.engine :as engine]))

(defn draw-hatching-pattern [path settings]
  (let [pattern-gen (gensym "pattern")]
    (r/create-class
      {:component-did-mount #(engine/load-pat-file
                               path
                               (fn [pattern]
                                 (let [canvas (js/document.getElementById pattern-gen)]
                                   (engine/draw-pattern canvas pattern
                                                        (merge {:width  275
                                                                :height 275
                                                                :scale  0.3} settings)))))
       :reagent-render      (fn []
                              [:canvas.item {:id pattern-gen} ""])})))

(defn app []
  [:div.container
   [draw-hatching-pattern "/patterns/HBFLEMET.pat" {:scale 0.2}]
   [draw-hatching-pattern "/patterns/HBLOCKES.pat" {:scale 0.05}]
   [draw-hatching-pattern "/patterns/HSBDR3E0.pat" {:scale 1}]
   [draw-hatching-pattern "/patterns/HVEGE100.pat" {:scale 3}]])
