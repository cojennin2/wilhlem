# wilhelm

A wrapper around themoviedb.org api using Clojure. Inspired by Clojure/conj 2014 and [wilhelm](http://www.youtube.com/watch?v=cdbYsoEasio)

## Building

You will need [Leiningen][] 2.0.0 or above installed.

[leiningen]: https://github.com/technomancy/leiningen

I'd recommend following the installation instructions on Github (it's putting a bash script on your $PATH). 

There's some issues with Leiningen and different package managers. The only one I think with the most up to date version is Homebrew (if you're using a Mac).

You'll also need [Java 1.7](http://www.oracle.com/technetwork/java/index.html).

To start a development server, run:

    $ lein ring server

To build an uberjar (which ends up by default in target/uberjar/)
    
    $ lein compile
    $ lein uberjar


## Running

There's an uberjar already packaged in this repo.

Run the following to start a web server on your local machine on :8080

    $ java -jar build/uberjar/wilhelm-0.1.0-standalone.jar
    
Or, if you built the uberjar from scratch (using the build commands above), run:

    $ java -jar target/uberjar/wilhelm-0.1.0-standalone.jar

To start a development server, run:

    $ lein ring server

## When Running

You can visit http://localhost:8080/index.html to get a basic front end that wraps the application

You can visit http://localhost:8080/movies/now-playing to get a list of movies that are currently playing in theaters (takes 2 optional query params: limit and offset)

You can visit http://localhost:8080/movies/:movieid/cast to get the cast of a movie (where :movieid is the id of a movie. You can use id's from any movie in themoviedb.org or use one from "/movies/now-playing"
 
You can visit http://localhost:8080/movies/:movieid/average-age-of-cast to get the average age of the cast of a movie (where :movieid is id of a movie. Same as above). 
    
## Project

The purpose of this project is to fetch information about movies playing in theaters and the average age of the cast. I used the time on this project as an opportunity to explore Clojure (how to structure a project, using core.async, making and using a lazy sequence, etc). It's not something I would consider "production" ready by any stretch of the imagination (that's a given with the constraints below), but I would consider it a success just given that I finally had a chance to mess around with go blocks.
 
 (Note that this project was inspired by my attendance at Clojure/conj 2014 (11/20 - 11/22). I haven't any serious projects in Clojure but after attending the conj I found this project to be a good opportunity to experiment with the language (I attended with a colleague who runs clojure code in production. I was just attending as an enthusiast).

### Constraints

1. Rate limiting of API used.
2. No dependencies on 3rd party software (eg, no database, no caching (memcached, etc))
  
The first constraint was a trade off. Using one data source [themoviedb.org] (http://docs.themoviedb.apiary.io/) meant less complexity in the application. At the same time it raised both "single point of failure" and rate limiting issues (you could probably roll rate limiting into single point of failure). It would be worth investigating whether adding another source of data would be sufficiently worthwhile to offset the extra complexity of maintaining two separate data sources (each with their own unique rate limiting constraints).

The second constraint was a personal one. I wanted a user to be able to run this application without having to go through a complex build or setup process. Avoiding dependencies on other software also has significant trade offs. Rate limiting could likely be avoided by persisting data into a database. Managing caching would also be significantly easier with a 3rd party solution (memcached, etc). However, with this constraint I had the opportunity to come up with some interesting (albeit impractical) solutions.
 
### Things I found interesting

*Cache*
Given the constraints, I opted for an in-memory caching solution with a library provided by [core.cache](https://github.com/clojure/core.cache). I opted for using a TTL Cache (expiration seemed like a good idea). What's interesting about core.cache is that it opts for sticking with the inherent idea of immutable and persistent data structures in Clojure. Actions that could be considered mutable in another language (delete, save, etc) instead return an updated data structure representing the cache in it's entirety. Therefore manipulating the cache required the use of [swap!](https://clojuredocs.org/clojure.core/swap!), a Clojure function for atomically swapping values (of atoms. That's why you'll find a number of "@" symbols in cache.clj. It's de-referencing the value of the cache (which is held in an atom). Note that one issue with this caching solution is that all cache is wiped whenever the program is stopped/started.
 
*Rate Limiting*
I talked a little bit about the constraints of rate limiting above. The rate limit constraint (5 calls/sec) is most felt when making calls to get profile information on a given cast member of a given movie. Getting cast member profile information requires getting the credits of a movie and then running through each credit to get the profile. Most movies had a substantial cast (upwards of 15-20) and I ended up quickly hitting the rate limit when trying to make these calls on the fly. The solution I came up with was to use core.async and prime the program cache before starting the web server.

The steps of the solution are as follows:

1. Before the web server boots, make a call to themoviedb.org with to get 20 movies (the default number to retrieve). 
2. Asynchronously place each movie profile individually on a channel.
3. A consumer listening on the channel takes each movie profile, makes a call to get the credits of the movie and then places each credit individually on a second channel
4. A consumer listening on the second channel takes each credit and makes a call to get the cast member profile.
5. After about 60-90 seconds we'll have the majority of actor profiles in the cache.
6. Given that this is done asynchronously, we can still field requests while the cache is being primed.

This solution is pretty naive. If this application starts taking requests before a majority of profiles are in cache we're going to hit themoviedb.org rate limit. The application will also place items into the pipeline for any given request to movies that are now playing. While the use of a pipeline would hopefully deter channel overflow, a substantial number of requests would more than likely cause problems for the pipeline. Regardless, I found it a good opportunity to get to know core.async (and hopefully can now work on applying it in more practical solutions)

*Handling Exceptions*
I'm not a huge fan of how exceptions thrown by themoviedb.org are handled. Given that I wanted to keep each part of the application pretty self sustaining, I chose to normalize exception messages thrown when making requests to themoviedb.org. That way we could swap another api and our application would still throw the same error message for the same http status even if the new api had a totally different message (themoviedb.org's 401 message was pretty good, but what if rottentomatoes is not? Or what if it didn't throw a message at all?). I don't think try/catch is very Clojury. I ended up throwing a generic exception from http.clj which I just bubbled up to handler.clj. I figured it was better that http.clj didn't throw an exception that was specific to handler.clj (same with api.clj and movies.clj). I wanted to keep each part of the application as independent from every other part so that if I had more time I could go in and add (or swap) a new api with relative ease.

### If I had more time

1. I would have liked to explore using 3rd party software. I think it was an interesting opportunity to work around the problem of not relying on any, but it doesn't really make for a robust or practical application
2. I would have liked to find more appropriate or practical uses for core.async. I like the pipeline model but it doesn't feel practical in this scenario.
3. I would have liked to explore [Hystrix](https://github.com/Netflix/Hystrix), a library by Netflix that deals with fault tolerance. I think it might have helped with handling exceptions
4. I would have liked to explore using different API's to get better data. Sometimes themoviedb.org doesn't have birthday information on the actors. Also it would help with rate limiting to distribute calls to different API's.
5. I would have liked to build in better querying for movies. What's now playing near your zipcode, in a given state, in a given country.
6. I would have like to build more options into the frontend. Right now it pulls 20 movies by default (the default number of items the api returns). It would be nice to add a "Load More" to load more movies.
7. I would have liked to rework some of the offset/limit logic. It was interesting building a lazy seq but I'm not sure how flexible it is (also I end up calling every page starting from page one, so if you're offset is large enough we'll probably have rate limiting problems).
8. I would have liked to utilize the REPL more effectively. I saw a talk at Clojure/conj (https://www.youtube.com/watch?v=0GzzFeS5cMc) that involved modifying a game on the fly. I'm not sure how I could have used it for this program, but it would have been interesting to try.
 

## License
The MIT License (MIT)
Copyright © 2014 Connor Jennings
