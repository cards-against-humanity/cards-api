package elasticsearch

import org.apache.http.HttpHost
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient

import java.net.InetAddress

class ElasticClient : ElasticIndexer, ElasticSearcher {
    companion object {
        private var instance: RestHighLevelClient? = null
        private var indexName: String? = null

        fun set(address: InetAddress, port: Int, indexName: String) {
            val host = HttpHost(address.hostAddress, port)
            instance = RestHighLevelClient(RestClient.builder(host))
            this.indexName = indexName
        }
    }

    override fun index(type: String, id: String, data: Map<String, Any>) {
        instance!!.index(IndexRequest(indexName, type, id).source(data))
    }

    override fun search(type: String, query: String) {
        println(instance!!.search(SearchRequest(query).types(type)).toString())
    }
}