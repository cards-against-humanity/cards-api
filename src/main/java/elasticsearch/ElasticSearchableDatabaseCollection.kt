package elasticsearch

import database.DatabaseCollection
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import route.card.model.CardpackModel
import route.user.model.UserModel
import java.net.InetAddress

class ElasticSearchableDatabaseCollection(private val superCollection: DatabaseCollection, elasticsearchHost: InetAddress, elasticsearchPort: Int) : DatabaseCollection by superCollection, ElasticSearcher, SearchableDatabaseCollection {
    private val indexer = ElasticClient(RestHighLevelClient(RestClient.builder(HttpHost(elasticsearchHost.hostAddress, elasticsearchPort))), superCollection)

    // TODO - Update name of users and cardpacks when they are changed

    override fun searchUsers(query: String): List<UserModel> {
        return indexer.searchUsers(query)
    }

    override fun searchCardpacks(query: String): List<CardpackModel> {
        return indexer.searchCardpacks(query)
    }

    override fun createUser(name: String, oAuthId: String, oAuthProvider: String): UserModel {
        val userModel = superCollection.createUser(name, oAuthId, oAuthProvider)
        indexer.indexUser(userModel)
        return userModel
    }

    override fun createCardpack(name: String, userId: String): CardpackModel {
        val cardpackModel = superCollection.createCardpack(name, userId)
        indexer.indexCardpack(cardpackModel)
        return cardpackModel
    }

    override fun deleteCardpack(id: String) {
        indexer.unindexCardpack(id)
        superCollection.deleteCardpack(id)
    }

    override fun autoCompleteUserSearch(query: String): List<String> {
        return indexer.autoCompleteUserSearch(query)
    }

    override fun autoCompleteCardpackSearch(query: String): List<String> {
        return indexer.autoCompleteCardpackSearch(query)
    }
}