(ns stockquote.worker
  (:require [stockquote.core :refer :all]
            [cemerick.bandalore :as sqs]
            [clj-http.client :as http]
            [clojure.data.csv :as csv]
            [cheshire.core :as json])
  (:gen-class))

(defn get-quote [symbol]
  (let [server-url (format "http://download.finance.yahoo.com/d/quotes.csv?f=sl1d1t1c1ohgvj1pp2owern&e=.csv&s=%s" symbol)]
    (-> server-url
        http/get
        :body
        csv/read-csv
        first)))

(defn process-stock [stock-symbol]
  (->> stock-symbol get-quote json/encode (sqs/send client result-queue)))

(defn process-message [message]
  (let [stock-symbol (:body message)]
    (process-stock stock-symbol)))

(defn start-consuming-work [client q]
  (dorun (pmap (sqs/deleting-consumer client process-message)
               (sqs/polling-receive client q :period 1000 :max-wait Long/MAX_VALUE :limit 10))))

(defn -main
  "Starts a worker that reads stock symbols from a work queue, asks Yahoo
 for a stock quote, and writes the quote on a result queue as a JSON array"
  [& args]
  (println "Listening for stock symbols on queue:\n\t" work-queue "\nWriting quotes to queue:\n\t" result-queue)
  (start-consuming-work client work-queue))
