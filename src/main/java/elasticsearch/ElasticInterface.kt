package elasticsearch

interface ElasticIndexer {
    fun index(type: String, id: String, data: Map<String, Any>)
}

interface ElasticSearcher {
    fun search(type: String, query: String)
}