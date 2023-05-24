# Simple News Aggregator System

The Simple News Aggregator System is a system that aggregates news from various sources, including news websites, Telegram API, Twitter API, etc. The system uses Kafka as an event broker to receive and process incoming news updates, and it stores data in a Mongo database. The backend server is implemented in Scala, while the frontend server is implemented using Node.js with Nextj and React.

## Architecture

![alt text](https://github.com/kabishev/news-tracker/blob/master/docs/newstracker.png?raw=true)

## How to run

Docker should be installed.

Build scala project docker images by sbt from the root directory

```
sbt "project core" Docker/publishLocal
sbt "project clients" Docker/publishLocal
```

Build frontend docker image
run the script from the docker folder

```
./build-images
```

Change docker/docker-compose.yml

* set DEEPL_AUTH_KEY by Authentication Key for DeepL API (free subscriptions can be used but it need to change DEEPL_BASE_URI to free DeepL API)
* set YAHOO_API_KEY be API key of <https://rapidapi.com/apidojo/api/yahoo-finance1> free subscription

Run:

```
docker-compose up
```

and open

```
http://localhost/
```

you should see the following page
![alt text](https://github.com/kabishev/news-tracker/blob/master/docs/demo.png?raw=true)
