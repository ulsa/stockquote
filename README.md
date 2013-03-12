# stockquote

A sample Swing-based client that demonstrates how to use Amazon SQS (Simple 
Queue Service). The application is a simple stock information system, where
you can add stock symbols and get stock quotes for them.

The application is written in Clojure, using several Clojure libraries for
various tasks, such as:

* bandalore for Amazon SQS access
* seesaw for a better Swing experience
* cheshire for JSON encoding and decoding
* clj-http for HTTP client access
* clojure.data.csv for CSV parsing

The application shows a table with preloaded stock symbols. When clicking the button
`Get Quotes`, each stock symbol is placed on an Amazon SQS work queue. This
queue can be polled by one or more workers. Each worker should grab a symbol
from the work queue, retrieve a stock quote for the symbol, and add the stock
quote result to a result queue. The client will poll the result queue using a
multi-threaded receiver, and update the GUI table with the retrieved results
accordingly.

The button `Clear` will clear the table form any stock information. New stock
symbols can be added by typing a stock symbol in the text field, like `GOOG`
or `AMZN` for example, and clicking `Add symbol`.

By default, the client starts its own multi-threaded worker, but it can also be
told to not start a worker. In the latter case, workers need to be provided by
other means, for example by starting workers as separate processes. More about
how to do that below.

## Quickstart

Run the client with Leiningen:

    $ lein run

## Installation

[Leiningen](https://github.com/technomancy/leiningen) is needed in order to compile 
the application. It will also make running it easier.

## Usage

### Starting the client with a worker

The client can be run in several ways. One way is via Leiningen:

    $ lein run

The application can also be compiled and packaged in a jar file containing all
dependencies:

    $ lein uberjar

The resulting jar file can be run directly:

    $ java -jar target/stockquote-0.1.0-SNAPSHOT-standalone.jar

It can also be run indirectly, with the main class specified:

    $ java -cp target/stockquote-0.1.0-SNAPSHOT-standalone.jar stockquote.client$_main

### Starting the client without a worker

With no arguments, the client starts its own worker. If you want to provide
your own workers, you can give `noworker` as an argument:

    $ java -cp target/stockquote-0.1.0-SNAPSHOT-standalone.jar noworker

### Starting a separate worker

The same jar file can be used to start a worker process as well:

    $ java -cp target/stockquote-0.1.0-SNAPSHOT-standalone.jar stockquote.worker$_main

### Getting queue information

The same jar file can also be used to get queue information. Without any argument, 
it will provide queue attributes for all the queues accessible by the account used:

    $ java -cp target/stockquote-0.1.0-SNAPSHOT-standalone.jar stockquote.queue$_main

One or more queue names can also be given as arguments:

    $ java -cp target/stockquote-0.1.0-SNAPSHOT-standalone.jar stockquote.queue$_main sqresult

## Options

* The `client` application accepts either no arguments or a `noworker` argument.
* The `worker` application accepts no arguments.
* The `queue` application accepts no arguments.

## Examples

### Starting a separate worker

    $ java -cp target/stockquote-0.1.0-SNAPSHOT-standalone.jar stockquote.worker$_main
    Listening for stock symbols on queue:
         https://sqs.eu-west-1.amazonaws.com/123456789012/sqwork 
    Writing quotes to queue:
         https://sqs.eu-west-1.amazonaws.com/123456789012/sqresult

### Queue information for all queues

    $ java -cp target/stockquote-0.1.0-SNAPSHOT-standalone.jar stockquote.queue$_main
    Status for queue https://sqs.eu-west-1.amazonaws.com/123456789012/sqwork
    {"ApproximateNumberOfMessages" "0",
     ...
    Status for queue https://sqs.eu-west-1.amazonaws.com/123456789012/sqresult
    {"ApproximateNumberOfMessages" "0",
     ...

### Queue information for a given queue

    $ java -cp target/stockquote-0.1.0-SNAPSHOT-standalone.jar stockquote.queue$_main sqresult
    Status for queue https://sqs.eu-west-1.amazonaws.com/123456789012/sqresult
    {"ApproximateNumberOfMessages" "0",
     ...

## License

Copyright © 2013 Ulrik Sandberg

Distributed under the Eclipse Public License, the same as Clojure.
