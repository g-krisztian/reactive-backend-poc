(defproject poc "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :source-paths ["src" "test"]
  :dependencies [[com.github.seancorfield/honeysql "2.1.833"]
                 [com.github.seancorfield/next.jdbc "1.2.753"]
                 [javax.servlet/servlet-api "2.5"]
                 [metosin/jsonista "0.3.5"]
                 [migratus "1.3.5"]
                 [org.clojure/clojure "1.10.1"]
                 [org.postgresql/postgresql "42.2.2"]
                 [ring "1.9.4"]]
  :repl-options {:init-ns poc.core})
