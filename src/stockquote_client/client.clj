(ns stockquote-client.client
  (:require [stockquote-client.core :refer :all ]
            [stockquote-client.worker :as worker]
            [seesaw.core :refer :all ]
            [seesaw.table :as table]
            [seesaw.swingx :as swingx]
            [cemerick.bandalore :as sqs]
            [cheshire.core :as json]
            [clojure.string :as str])
  (:gen-class))

;; indexes stock symbol to row number in table model
(def ^:private rows (atom {}))

;; Initial GUI stuff

(def model
  (table/table-model
    :columns [{:key :symbol :text "Symbol"}
              {:key :last :text "Last"}
              {:key :change :text "Change"}
              {:key :percent-change :text "Percent"}
              {:key :high :text "High"}
              {:key :low :text "Low"}
              {:key :volume :text "Volume"}
              {:key :timestamp :text "Timestamp"}
              {:key :name :text "Name"}]
    :rows []))

(def f (frame :title "Amazon SQS Stock Quote Sample" :on-close :exit))

(def tf-symbol (text :columns 6))

;; processing stuff

(def all-keys [:symbol :last :date :time :change :open :high :low :volume :market-cap :previous-close :percent-change :unknown :annual-range :earnings :pe-ratio :name ])

(defn filter-quote [stockquote]
  (let [stockquote-map (zipmap all-keys stockquote)]
    (if-not (= 17 (count stockquote-map))
      (println "Error: stock quote for" (:symbol stockquote-map) "has incorrect format, expected 17 field, got" (count stockquote-map))
      (let [{:keys [symbol last date time change _ high low volume _ _ percent-change _ _ _ _ name]} stockquote-map]
        [symbol last change percent-change high low volume (str date \space time) name]))))

(defn update-table [stockquote]
  (when-let [result (filter-quote stockquote)]
    (let [stock-symbol (keyword (first result))]
      (if (neg? (stock-symbol @rows -1))
        (do
          (swap! rows assoc stock-symbol (table/row-count model))
          (invoke-now (table/insert-at! model (table/row-count model) result)))
        (invoke-now (table/update-at! model (stock-symbol @rows) result))))))

(defn start-consuming-results [client q]
  (future
    (dorun (pmap (sqs/deleting-consumer client (comp update-table json/decode :body))
             (sqs/polling-receive client q :period 1000 :max-wait Long/MAX_VALUE :limit 10)))))

(defn add-symbol [stock-symbol]
  (update-table (list* stock-symbol (repeat 16 nil))))

(defn no-quote-symbols-first []
  (map :symbol (sort-by :last (map (partial table/value-at model) (map second @rows)))))

;; GUI event handlers

(defn get-quotes-handler [event]
  (doseq [stock-symbol (no-quote-symbols-first)]
    (future (sqs/send client work-queue stock-symbol))))

(defn clear-quotes-handler [event]
  (invoke-now (table/clear! model))
  (reset! rows {}))

(defn add-symbol-handler [event]
  (let [new-symbol (text tf-symbol)]
    (when-not (str/blank? new-symbol)
      (add-symbol new-symbol)
      (text! tf-symbol ""))))

;; Connect behavior to GUI

(config! f
  :content (vertical-panel
             :items [(scrollable (swingx/table-x
                                   :model model
                                   :auto-resize :all-columns ))
                     (horizontal-panel
                       :items [(button
                                 :text "Get Quotes"
                                 :listen [:action get-quotes-handler])
                               (button
                                 :text "Clear"
                                 :listen [:action clear-quotes-handler])
                               (flow-panel
                                 :items [(button
                                           :text "Add Symbol"
                                           :listen [:action add-symbol-handler])
                                         tf-symbol])])]))

(defn -main
  "Starts a GUI where the user can ask for stock quotes for a list of stock
 symbols. Stock symbols are added to a work queue. Results are expected to be
 added to a result queue. Also starts a multi-threaded result queue listener.
 
 Unless given the argument 'noworker', this function also starts a multi-threaded
 worker that reads stock symbols from a work queue, asks Yahoo for a stock
 quote, and writes the quote on a result queue as a JSON array.
 
 If given a 'noworker' argument, no worker is started in the same process as
 the GUI. A worker must be started separately in order for any stock quotes to
 be displayed in the GUI."
  [& args]
  (doseq [s stocks]
    (add-symbol s))
  (invoke-later (-> f pack! show!))

  (println "Starting a result consumer")
  (start-consuming-results client result-queue)

  (if (and (seq args) (= "noworker" (first args)))
    (println "No worker started; you have to start one separately")
    (do
      (println "Starting a worker as well")
      (worker/start-consuming-work client work-queue))))
