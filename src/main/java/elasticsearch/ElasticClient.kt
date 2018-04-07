package elasticsearch

import database.DatabaseCollection
import org.apache.http.HttpHost
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchType
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.unit.Fuzziness
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import route.card.model.CardpackModel
import route.user.model.UserModel

import java.net.InetAddress

class ElasticClient(private val elasticClient: RestHighLevelClient, private val database: DatabaseCollection) : ElasticIndexer, ElasticSearcher {

    companion object {
        const val userIndex = "user"
        const val cardpackIndex = "cardpack"
    }

    override fun indexUser(user: UserModel) {
        elasticClient.index(IndexRequest(userIndex).source("name", user.name).id(user.id).type(userIndex))
    }

    override fun indexCardpack(cardpack: CardpackModel) {
        elasticClient.index(IndexRequest(cardpackIndex).source("name", cardpack.name).id(cardpack.id).type(cardpackIndex))
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
}