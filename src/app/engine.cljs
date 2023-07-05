(ns app.engine
  (:require
    [app.fetch :as fetch]
    [shadow.cljs.modern :refer (js-await)]))

(def stroke-width 1)
(def draw-minimum 50)
(def draw-maximum 10000)
(def grid-margin 50)

(def colors
  {:bg    "#fff"
   :lines "#222"})

(def default-settings
  {:width  640
   :height 480
   :scale 1})

(defn distance [x1 y1 x2 y2]
  (Math/sqrt (+ (* (- x1 x2) (- x1 x2))
                (* (- y1 y2) (- y1 y2)))))

(defn splice [coll start stop]
  (take (- stop start) (drop start coll)))

(defn get-grid-side-dimensions [settings]
  {:grid-margin-x (/ (:width settings) 2)
   :grid-margin-y (/ (:height settings) 2)})

(defn get-paddings [settings]
  (let [w (:width settings)
        h (:height settings)
        scale (:scale settings)]
    {:br-x (/ (/ w 2) scale)
     :tr-x (/ (/ w 2) scale)
     :br-y (/ (/ h 2) scale)
     :bl-y (/ (/ h 2) scale)
     :bl-x (/ (/ (* -1 w) 2) scale)
     :tl-x (/ (/ (* -1 w) 2) scale)
     :tr-y (/ (/ (* -1 h) 2) scale)
     :tl-y (/ (/ (* -1 h) 2) scale)}))

(defn set-canvas-dimensions [canvas settings]
  (.setAttribute canvas "width" (:width settings))
  (.setAttribute canvas "height" (:height settings)))

(defn initial-drawing-settings [context settings]
  (let [grid-side-dimensions (get-grid-side-dimensions settings)
        scale (:scale settings)]
    (set! context.fillStyle (:bg colors))
    (.fillRect context 0 0 (:width settings) (:height settings))
    (.beginPath context)
    (.rect context 0 0 (:width settings) (:height settings))
    (.fill context)
    (.translate context (:grid-margin-x grid-side-dimensions) (:grid-margin-y grid-side-dimensions))
    (.scale context scale scale)
    (set! context.strokeStyle (or (:color settings)
                                  (:lines colors)))
    (set! context.lineWidth (/ stroke-width scale))))

(defn remove-duplicates [intersections]
  (map first
       (filter (fn [[k v]] (= v 1))
               (frequencies intersections))))

(defn- intersect?
  [min-a max-a value]
  (and (<= min-a value) (< value max-a)))

(defn check-line-intersection
  [line1-start-x line1-start-y line1-end-x line1-end-y
   line2-start-x line2-start-y line2-end-x line2-end-y]
  (let [denominator (- (* (- line2-end-y line2-start-y) (- line1-end-x line1-start-x))
                       (* (- line2-end-x line2-start-x) (- line1-end-y line1-start-y)))]
    (if (zero? denominator)
      {:x nil :y nil :on-line1 false :on-line2 false}
      (let [a (- line1-start-y line2-start-y)
            b (- line1-start-x line2-start-x)
            numerator1 (- (* (- line2-end-x line2-start-x) a)
                          (* (- line2-end-y line2-start-y) b))
            numerator2 (- (* (- line1-end-x line1-start-x) a)
                          (* (- line1-end-y line1-start-y) b))
            a (/ numerator1 denominator)
            b (/ numerator2 denominator)
            result-x (+ line1-start-x (* a (- line1-end-x line1-start-x)))
            result-y (+ line1-start-y (* a (- line1-end-y line1-start-y)))]

        {:x        result-x
         :y        result-y
         :on-line1 (intersect? 0.0 1.0 a)
         :on-line2 (intersect? 0.0 1.0 b)}))))

(defn draw-row [context
                {:keys [bl-x bl-y br-x br-y tl-x tl-y tr-x tr-y]}
                r perpOffset factor]
  (let [dasharray (splice r 5 (count r))
        dasharray (map (fn [v]
                         (cond
                           (< v 0) (* v -1)
                           :else v)) dasharray)
        a (/ (* (get r 0) Math/PI) 180)
        aP (/ (* (- (get r 0) 90) Math/PI) 180)

        x1 (- (+ (get r 1)
                 (* perpOffset (* (Math/cos aP) (get r 4))))
              (* perpOffset (* (Math/cos a) (get r 3))))

        y1 (- (+ (get r 2)
                 (* perpOffset (* (Math/sin aP) (get r 4))))
              (* perpOffset (* (Math/sin a) (get r 3))))


        t 1000000

        dif-x (* (Math/cos (+ a (* Math/PI factor))) t)
        dif-y (* (Math/sin (+ a (* Math/PI factor))) t)

        x2 (atom (+ x1 dif-x))
        y2 (atom (+ y1 dif-y))

        end? (atom false)]

    (let [intersect-bottom (check-line-intersection x1 y1 @x2 @y2 bl-x bl-y br-x br-y)
          intersect-top (check-line-intersection x1 y1 @x2 @y2 tl-x tl-y tr-x tr-y)
          intersect-left (check-line-intersection x1 y1 @x2 @y2 bl-x bl-y tl-x tl-y)
          intersect-right (check-line-intersection x1 y1 @x2 @y2 br-x br-y tr-x tr-y)
          intersections (into []
                              (filter #(:on-line2 %)
                                      [intersect-bottom intersect-top intersect-left intersect-right]))]
      (if (< (count intersections) 2)
        (reset! end? true)
        (let [a0 (* (/ (/ (Math/atan2
                            (- (get-in intersections [0 :y]) y1)
                            (- (get-in intersections [0 :x]) x1))
                          Math/PI) 2) 360)

              a1 (* (/ (/ (Math/atan2
                            (- (get-in intersections [1 :y]) y1)
                            (- (get-in intersections [1 :x]) x1))
                          Math/PI) 2) 360)
              comp (get r 0)

              comp
              (cond-> comp
                      (> comp 360) (- 360)
                      (< comp 0) (+ 360)
                      (pos? factor) (+ 180))

              comp
              (cond-> comp
                      (> comp 360) (- 360)
                      (< comp 0) (+ 360))

              a0 (cond-> a0
                         (> a0 360) (- 360)
                         (< a0 0) (+ 360))

              a1 (cond-> a1
                         (> a1 360) (- 360)
                         (< a1 0) (+ 360))

              a0 (Math/round a0)
              a1 (Math/round a1)
              comp (Math/round comp)]

          (cond
            (and (not= comp a0) (= a0 a1)) (reset! end? true)
            (and (= comp a0) (= comp a1))
            (let [intersections (into [] (sort (fn [a b]
                                                 (if (> (distance (:x a) (:y a) x1 y1)
                                                        (distance (:x b) (:y b) x1 y1))
                                                   -1
                                                   1)) intersections))]
              (reset! x2 (get-in intersections [0 :x]))
              (reset! y2 (get-in intersections [0 :y])))
            (= comp a0) (do
                          (reset! x2 (get-in intersections [0 :x]))
                          (reset! y2 (get-in intersections [0 :y])))
            (= comp a1) (do
                          (reset! x2 (get-in intersections [1 :x]))
                          (reset! y2 (get-in intersections [1 :y])))
            (and (not= comp a0)
                 (not= comp a1)) (reset! end? true)))))

    (when-not @end?
      (.beginPath context)
      (.moveTo context x1 (* -1 y1))
      (set! context.lineDashOffset 0)
      (.setLineDash context dasharray)
      (when (pos? factor)
        (set! context.lineDashOffset (* -1 (last dasharray)))
        (let [backwards (reverse dasharray)]
          (.setLineDash context [(last backwards) (first backwards)])))
      (.lineTo context @x2 (* -1 @y2))
      (set! context.lineCap "round")
      (.stroke context)
      {:x1 x1
       :y1 y1
       :x2 x2
       :y2 y2})))

(defn draw-row-sequence
  [context paddings min-val max-val row offset factor]
  (let [drawIt (atom true)
        index (atom factor)]
    (while
      (if (= factor 0)
        (and (or @drawIt (< @index min-val)) (< @index max-val))
        (and (or @drawIt (> @index (* -1 min-val))) (> @index (* -1 max-val))))
      (reset! drawIt (draw-row context paddings (:cells row) @index offset))
      (swap! index (if (= factor 0) inc dec)))))

(defn draw-pattern
  ([canvas pattern]
   (draw-pattern canvas pattern nil))
  ([canvas pattern settings]
   (let [settings (merge default-settings settings)
         context (.getContext canvas "2d")
         paddings (get-paddings settings)
         draw-row-sequence (partial draw-row-sequence context paddings draw-minimum draw-maximum)]
     (set-canvas-dimensions canvas settings)
     (initial-drawing-settings context settings)
     (doseq [row pattern]
       (draw-row-sequence row 0 0)
       (draw-row-sequence row 1 0)
       (draw-row-sequence row 0 -1)
       (draw-row-sequence row 1 -1))
     canvas)))

(defn load-pat-file [path cb]
  (js-await [pattern (fetch/fetch-and-parse-pattern! path)]
            (cb pattern)))
