# Cards API

A dockerizable API for cards against humanity

## How to use it

### Building the Docker Image

All dependencies, including Java and Maven, are handled within docker build stages. All that is needed is the source code and docker itself. To build it, simply pull the repo, navigate to the main project folder in the command line, and run ```docker build .```

### Settings

| Environment Variable | Required | Default | Description |
| --- | --- | --- | --- |
| MONGO_HOST | NO | localhost | Mongodb host |
| MONGO_PORT | NO | 27017 | Mongodb port |
| MONGO_DATABASE | NO | cardsOnline | The name of the mongodb database to connect to |
| ELASTICSEARCH_HOST | NO | localhost | Elasticsearch host |
| ELASTICSEARCH_PORT | NO | 9200 | Elasticsearch port |
| ALLOWED_CORS_ORIGIN | NO | http://locahost | String containing the allowed CORS origins |

To set an environment variable, the following syntax must be followed: ```[VARIABLE_NAME]=[VALUE]```
