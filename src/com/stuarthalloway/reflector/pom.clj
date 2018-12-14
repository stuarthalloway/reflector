;;   Copyright (c) Stuart Halloway
;;   The use and distribution terms for this software are covered by the
;;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;;   which can be found in the file epl-v10.html at the root of this distribution.
;;   By using this software in any fashion, you are agreeing to be bound by
;;   the terms of this license.
;;   You must not remove this notice, or any other, from this software.

(ns com.stuarthalloway.reflector.pom
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [clojure.xml :as xml]
   [clojure.zip :as zip]
   [com.stuarthalloway.reflector.coerce :as coerce])
  (:import
   [java.io File]
   [java.net URL]))

(defn guess-pom
  "Given a URL, try to guess the associated pom."
  [url]
  (when (instance? URL url)
    (some-> (.getFile url)
            (str/replace #"^file:" "")
            (str/replace #"!.*" "")
            (str/replace #".jar$" ".pom")
            (io/file))))

(defn pom-contents
  [url]
  (when-let [^File pom (guess-pom url)]
    (when (.exists pom)
      (slurp pom))))

(def extractors
  (let [content (comp coerce/urlify first :content zip/node)
        child-content (fn [node ks]
                        (reduce
                         (fn [m {:keys [tag] :as node}]
                           (if (ks tag)
                             (assoc m tag (-> node :content first coerce/urlify))
                             m))
                         {}
                         (:content node)))
        children-content (fn [keyset]
                           (fn [node]
                             (mapv #(child-content % keyset) (zip/children node))))]
    {:url content
     :description content
     :developers (children-content #{:name :email})
     :licenses (children-content #{:name :url :distribution})
     :scm #(child-content (zip/node %) #{:connection :developerConnection :url :tag})
     :dependencies (children-content #{:groupId :artifactId :version :scope})}))

(defn summary
  [xml]
  (let [zip (zip/xml-zip xml)]
    (reduce
     (fn [m zipper]
       (let [{:keys [tag] :as node} (zip/node zipper)]
         (if-let [extractor (extractors tag)]
           (assoc m tag (extractor zipper))
           m)))
     {}
     (->> (zip/down zip)
          (iterate zip/right)
          (take-while identity)))))

