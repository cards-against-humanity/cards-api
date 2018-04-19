package elasticsearch

import database.DatabaseCollection
import org.apache.http.entity.BasicHttpEntity
import org.apache.http.message.BasicHeader
import org.elasticsearch.ElasticsearchStatusException
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.unit.Fuzziness
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import route.card.model.CardpackModel
import route.user.model.UserModel
import java.net.SocketException
import java.util.*


class ElasticClient(private val elasticClient: RestHighLevelClient, private val database: DatabaseCollection, timeoutDuration: Int) : ElasticIndexer, ElasticSearcher, ElasticAutoCompleter {
    companion object {
        const val userIndex = "user"
        const val userAutocompleteIndex = "userautocomplete"
        const val cardpackIndex = "cardpack"
        const val cardpackAutocompleteIndex = "cardpackautocomplete"
    }

    init {
        createIndices(timeoutDuration)
    }

    override fun indexUser(user: UserModel) {
        elasticClient.index(IndexRequest(userIndex).source("name", user.name).id(user.id).type(userIndex))
        elasticClient.index(IndexRequest(userAutocompleteIndex).source("name", user.name).id(user.id).type(userAutocompleteIndex))
    }

    override fun indexCardpack(cardpack: CardpackModel) {
        elasticClient.index(IndexRequest(cardpackIndex).source("name", cardpack.name).id(cardpack.id).type(cardpackIndex))
        elasticClient.index(IndexRequest(cardpackAutocompleteIndex).source("name", cardpack.name).id(cardpack.id).type(cardpackAutocompleteIndex))
    }

    override fun unindexCardpack(cardpackId: String) {
        elasticClient.delete(DeleteRequest(cardpackIndex).type(cardpackIndex).id(cardpackId))
    }

    override fun searchUsers(query: String): List<UserModel> {
        val searchSourceBuilder = SearchSourceBuilder().query(QueryBuilders.multiMatchQuery(query, "name").fuzziness(Fuzziness.AUTO))
        val searchRequest = SearchRequest(userIndex).source(searchSourceBuilder)
        val res = elasticClient.search(searchRequest)
        val userIds = res.hits.hits.map { hit -> hit.id }
        return database.getUsers(userIds)
    }

    override fun searchCardpacks(query: String): List<CardpackModel> {
        val searchSourceBuilder = SearchSourceBuilder().query(QueryBuilders.multiMatchQuery(query, "name").fuzziness(Fuzziness.AUTO))
        val searchRequest = SearchRequest(cardpackIndex).source(searchSourceBuilder)
        val res = elasticClient.search(searchRequest)
        val cardpackIds = res.hits.hits.map { hit -> hit.id }
        return cardpackIds.map { id -> database.getCardpack(id) } // TODO - Optimize this line
    }

    override fun autoCompleteUserSearch(query: String): List<String> {
        val searchSourceBuilder = SearchSourceBuilder().query(QueryBuilders.matchQuery("name", query))
        val searchRequest = SearchRequest(userAutocompleteIndex).source(searchSourceBuilder)
        val res = elasticClient.search(searchRequest)
        val userIds = res.hits.hits.map { hit -> hit.id }
        return database.getUsers(userIds).map { user -> user.name }
    }

    override fun autoCompleteCardpackSearch(query: String): List<String> {
        val searchSourceBuilder = SearchSourceBuilder().query(QueryBuilders.matchQuery("name", query))
        val searchRequest = SearchRequest(cardpackAutocompleteIndex).source(searchSourceBuilder)
        val res = elasticClient.search(searchRequest)
        val cardpackIds = res.hits.hits.map { hit -> hit.id }
        return cardpackIds.map { id -> database.getCardpack(id).name } // TODO - Make this more efficient
    }

    private fun createIndices(timeoutDuration: Int) {
        waitForConnection(timeoutDuration)
        try {
            createAutocompleteIndex(userAutocompleteIndex)
        } catch (e: Exception) { }
        try {
            createAutocompleteIndex(cardpackAutocompleteIndex)
        } catch (e: Exception) { }
        try {
            elasticClient.indices().create(CreateIndexRequest(userIndex))
        } catch (e: ElasticsearchStatusException) { println(e.message) }
        try {
            elasticClient.indices().create(CreateIndexRequest(cardpackIndex))
        } catch (e: Exception) { }
    }

    private fun waitForConnection(timeoutDuration: Int) {
        if (timeoutDuration <= 0 ) {
            throw Exception("Timeout duration must be positive")
        }
        val startTime = Date()
        while (true) {
            if (Date().time - startTime.time > timeoutDuration) {
                throw SocketException("Could not connect to Elasticsearch")
            }
            try {
                if (elasticClient.ping()) {
                    break
                }
            } catch (e: Exception) { }
            Thread.yield()
        }
    }

    private fun createAutocompleteIndex(index: String) {
        // TODO - Check if index exists and add error handling
        var httpEntity = BasicHttpEntity()
        httpEntity.content = (
                        "{" +
                        "    \"settings\": {" +
                        "        \"analysis\": {" +
                        "            \"filter\": {" +
                        "                \"autocomplete_filter\": { " +
                        "                    \"type\":     \"edge_ngram\"," +
                        "                    \"min_gram\": 1," +
                        "                    \"max_gram\": 20" +
                        "                }" +
                        "            }," +
                        "            \"analyzer\": {" +
                        "                \"autocomplete\": {" +
                        "                    \"type\":      \"custom\"," +
                        "                    \"tokenizer\": \"standard\"," +
                        "                    \"filter\": [" +
                        "                        \"lowercase\"," +
                        "                        \"autocomplete_filter\" " +
                        "                    ]" +
                        "                }" +
                        "            }" +
                        "        }" +
                        "    }," +
                        "    \"mappings\": {" +
                        "        \"$index\": {" +
                        "            \"properties\": {" +
                        "                \"name\": {" +
                        "                    \"type\": \"text\"," +
                        "                    \"analyzer\": \"autocomplete\", " +
                        "                    \"search_analyzer\": \"standard\" " +
                        "                }" +
                        "            }" +
                        "        }" +
                        "    }" +
                        "}"
                ).byteInputStream()
        httpEntity.contentType = BasicHeader("Content-Type", "application/json")
        elasticClient.lowLevelClient.performRequest("PUT", "/$index", HashMap<String, String>(), httpEntity)
    }
}