(ns stockquote.queue
  (:require [stockquote.core :refer :all ]
            [cemerick.bandalore :as sqs]
            [clojure.string :as str]
            [clojure.pprint :refer :all])
  (:gen-class))

(defonce queue-names
  (set (map #(last (str/split % #"/")) (sqs/list-queues client))))

(defn -main
  "Checks the given queue for status. If no queue is given, checks
 both the work queue and the result queue"
  [& args]
  (when-let [qs (or args queue-names)]
    (doseq [q qs]
      (if (queue-names q)
        (let [queue (str queue-base q)]
          (println "Status for queue" queue)
          (pprint (sqs/queue-attrs client queue))
          (flush))
        (println (format "Error: No queue named '%s'; valid queue names are %s" q queue-names))))))
