(defproject stockquote-client "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [com.cemerick/bandalore "0.0.3"]
                 [clj-http "0.6.4"]
                 [org.clojure/data.csv "0.1.2"]
                 [seesaw "1.4.3"]
                 [cheshire "5.0.2"]]
  :main stockquote-client.client
  :aot :all)
