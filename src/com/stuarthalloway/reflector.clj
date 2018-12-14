;;   Copyright (c) Stuart Halloway
;;   The use and distribution terms for this software are covered by the
;;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;;   which can be found in the file epl-v10.html at the root of this distribution.
;;   By using this software in any fashion, you are agreeing to be bound by
;;   the terms of this license.
;;   You must not remove this notice, or any other, from this software.

(ns com.stuarthalloway.reflector
  (:require
   [clojure.java.io :as io]
   [clojure.datafy :as dfy]
   [clojure.repl :as repl]
   [clojure.string :as str]
   [com.stuarthalloway.reflector.pom :as pom])
  (:import
   [clojure.lang Namespace]
   [java.net URL]))

(defprotocol Reflect
  (-reflect [_] "Impl detail, do not call"))

(defn whence-ns?
  "Given a Clojure namespace, returns a map telling where its clj, cljc,
and class file representation are visible on the classpath"
  [ns]
  (let [n (-> ns
              str
              (str/replace "." "/")
              (str/replace "-" "_"))]
    {:basename n
     :clj (io/resource (str n ".clj"))
     :cljc (io/resource (str n ".cljc"))
     :class (io/resource (str n "__init.class")) }))

(defn- with-var-nav
  "Var navigation that returns vars as a map with possible keys

:source
:doc
:clojuredocs"
  [v]
  (with-meta
    v
    {'clojure.core.protocols/nav
     (fn [_ k v]
       (if (var? v)
         (let [sym (.toSymbol v)
               ns (namespace sym)
               n (name sym)]
           (cond-> {:source (repl/source-fn sym)
                    :doc (with-out-str (@#'clojure.repl/print-doc (meta v)))}

                   (str/starts-with? ns "clojure")
                   (assoc :clojuredocs (URL. (str "https://clojuredocs.org/" (namespace sym) "/" (name sym))))))
         v))}))

(extend-protocol Reflect
  Object
  (-reflect [_])

  nil
  (-reflect [_] nil)

  clojure.lang.Namespace
  (-reflect
   [n]
   (let [resources (whence-ns? n)
         sum #(some-> % pom/guess-pom xml/parse pom/summary)
         pom (or (some-> resources :clj sum)
                 (some-> resources :class sum))]
     (with-meta
       (cond-> (dfy/datafy n)
               resources (assoc :resources resources)
               pom (assoc :pom pom))
       {'clojure.core.protocols/nav
        (fn [_ k v]
          (case k
                :publics (with-var-nav v)
                :interns (with-var-nav v)
                v))}))))

(defn on
  "Reflect on x, maybe adding useful nav. Intended only for e.g. REBL
browsing. There is no API contract here. I will change this."
  [x]
  (-reflect x))

