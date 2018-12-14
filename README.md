# Reflector

Sample app showing bespoke use of [datafy and
nav](http://corfield.org/blog/2018/12/03/datafy-nav/).

# Trying it

Add this code to a running REPL that can do something useful with
datafy and nav,
e.g. [REBL](https://github.com/cognitect-labs/REBL-distro). You can
add the code as a [git
dep](https://clojure.org/guides/deps_and_cli#_using_git_libraries) or
simply evaluate the namespaces.

Then evaluate 

    (com.stuarthalloway.reflector/on (find-ns 'clojure.core))

# What it Can Do

Given a Clojure namespace, navigate quickly to

* source code for a var
* ClojureDocs for a var
* origin SCM repository
* code license

# Contributing

Start with this code and make something better.

# License

EPL same as Clojure.
