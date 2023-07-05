(ns app.fetch
  (:require [clojure.string :as str]
            [shadow.cljs.modern :refer (js-await)]
            [cljs.reader :refer [read-string]]))

(defn parse-pattern [raw]
  (let [lines (str/split-lines raw)]
    (->> lines
         (mapv (fn [line]
                 (let [rows (->> (str/split line ",")
                                 (mapv read-string))]
                   (when (number? (first rows))
                     {:cells rows}))))
         (remove nil?)
         (into []))))

(defn fetch-and-parse-pattern! [path]
  (-> (.fetch js/window path)
      (.then #(.text %))
      (.then #(parse-pattern %))))

(defn fetch-and-parse-pattern-async! [name]
  (js-await [pattern (fetch-and-parse-pattern! (str "/patterns/" name ".pat"))]
            pattern))