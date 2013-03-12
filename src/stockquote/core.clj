(ns stockquote.core
  (:require [cemerick.bandalore :as sqs]))

(def stocks ["VRTA" "OREX" "LEDR" "FSTF" "DXYN" "BOCH" "ENZN"
             "SPIR" "SNMX" "ANCX" "CENTA" "HSNI" "GILT"
             "GPRE" "SAFM" "HCKT" "MSFT" "INTC" "CSCO"
             "ORCL" "YHOO" "AAPL" "AMAT" "CMCSA" "DELL" "SYMC"
             "EBAY" "QCOM" "SCHW" "DRYS" "SBUX" "BRCM" "DTV"])

(def auth (read-string (slurp (str (System/getProperty "user.home") "/.aws.properties"))))

(def ^:dynamic *endpoint* "https://sqs.eu-west-1.amazonaws.com/")

(def ^:dynamic *base* "496151958509/")

(def ^:dynamic *work-queue-name* "sqwork")

(def ^:dynamic *result-queue-name* "sqresult")

(defonce client
  (doto
    (sqs/create-client (:aws.accessKeyId auth) (:aws.secretKey auth))
    (.setEndpoint *endpoint*)))

(def queue-base (str *endpoint* *base*))

(def work-queue (str queue-base *work-queue-name*))

(def result-queue (str queue-base *result-queue-name*))
