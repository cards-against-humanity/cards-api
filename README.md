# Cards API

# Mongo Kubernetes Replica Set Sidecar

This project is as a PoC to setup a mongo replica set using Kubernetes. It should handle resizing of any type and be
resilient to the various conditions both mongo and kubernetes can find themselves in.

## How to use it

... TODO add description

### Settings

| Environment Variable | Required | Default | Description |
| --- | --- | --- | --- |
| MONGO_HOST | NO | mongodb | Mongodb host |
| MONGO_PORT | NO | 27017 | Mongodb port |
| MONGO_DATABASE | NO | appName | The name of the mongodb database to connect to |
| ELASTICSEARCH_HOST | NO | elasticsearch | Elasticsearch host |
| ELASTICSEARCH_PORT | NO | 9200 | Elasticsearch port |
| ALLOWED_CORS_ORIGIN | NO | http://locahost | String containing the allowed CORS origins |
