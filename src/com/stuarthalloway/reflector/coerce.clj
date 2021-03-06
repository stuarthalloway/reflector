;;   Copyright (c) Stuart Halloway
;;   The use and distribution terms for this software are covered by the
;;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;;   which can be found in the file epl-v10.html at the root of this distribution.
;;   By using this software in any fashion, you are agreeing to be bound by
;;   the terms of this license.
;;   You must not remove this notice, or any other, from this software.

(ns com.stuarthalloway.reflector.coerce
  (:require [clojure.string :as str])
  (:import
   [java.io File]
   [java.net URL]))

(defn urlify
  [s]
  (if (string? s)
    (if (str/starts-with? s "http")
      (try
       (URL. s)
       (catch Throwable _
         s))
      s)
    s))

