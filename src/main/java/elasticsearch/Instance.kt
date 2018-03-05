package elasticsearch

import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient

import java.net.InetAddress

object Instance {
    private var instance: RestHighLevelClient? = null

    fun set(address: InetAddress, port: Int) {
        val host = HttpHost(address.hostAddress, port)
        instance = RestHighLevelClient(RestClient.builder(host))
    }

    fun get(): RestHighLevelClient {
        if (instance == null) {
            throw NullPointerException("Client object has not been set")
        }
        return instance!!
    }
}
