import database.DatabaseCollection
import elasticsearch.ElasticClient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import database.memorymodel.MemoryDatabaseCollection
import org.apache.http.HttpHost
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import kotlin.test.assertEquals

class ElasticClientTest {

    private var database: DatabaseCollection = MemoryDatabaseCollection()
    private val elasticRestClient = RestHighLevelClient(RestClient.builder(HttpHost("localhost", 9200)))
    private var elasticClient = ElasticClient(elasticRestClient, database)

    @BeforeEach
    fun reset() {
        database = MemoryDatabaseCollection()
        try {
            elasticRestClient.indices().delete(DeleteIndexRequest(ElasticClient.userIndex))
        } catch (e: Exception) { }
        try {
            elasticRestClient.indices().delete(DeleteIndexRequest(ElasticClient.cardpackIndex))
        } catch (e: Exception) { }
        elasticRestClient.indices().create(CreateIndexRequest(ElasticClient.userIndex))
        elasticRestClient.indices().create(CreateIndexRequest(ElasticClient.cardpackIndex))
        elasticClient = ElasticClient(elasticRestClient, database)
    }

    @Test
    fun indexUser() {
        val user = database.createUser("Tommy", "1234", "google")
        elasticClient.indexUser(user)
        val elasticUserData = elasticRestClient.get(GetRequest(ElasticClient.userIndex).type(ElasticClient.userIndex).id(user.id))
        assert(elasticUserData.isExists)
        assertEquals("Tommy", elasticUserData.source["name"] as String)
    }

    @Test
    fun indexCardpack() {
        val user = database.createUser("Tommy", "1234", "google")
        val cardpack = database.createCardpack("Tommy's Cardpack", user.id)
        elasticClient.indexCardpack(cardpack)
        val elasticUserData = elasticRestClient.get(GetRequest(ElasticClient.cardpackIndex).type(ElasticClient.cardpackIndex).id(cardpack.id))
        assert(elasticUserData.isExists)
        assertEquals("Tommy's Cardpack", elasticUserData.source["name"] as String)
    }

    @Test
    fun unindexExistingCardpack() {
        val user = database.createUser("Tommy", "1234", "google")
        val cardpack = database.createCardpack("Tommy's Cardpack", user.id)
        elasticClient.indexCardpack(cardpack)
        elasticClient.unindexCardpack(cardpack.id)
        val elasticUserData = elasticRestClient.get(GetRequest(ElasticClient.cardpackIndex).type(ElasticClient.cardpackIndex).id(cardpack.id))
        assert(!elasticUserData.isExists)
    }

    @Test
    fun exactUserSearch() {
        val userOne = database.createUser("Tommy", "1234", "google")
        val userTwo = database.createUser("Charlie", "4321", "google")
        elasticClient.indexUser(userOne)
        elasticClient.indexUser(userTwo)
        Thread.sleep(1000)

        val searchedUsers = elasticClient.searchUsers("Tommy")
        assertEquals(1, searchedUsers.size)
        assert(usersAreEqual(userOne, searchedUsers[0]))
    }

    @Test
    fun partialUserSearch() {
        val userOne = database.createUser("Tommy", "1234", "google")
        val userTwo = database.createUser("Charlie", "4321", "google")
        elasticClient.indexUser(userOne)
        elasticClient.indexUser(userTwo)
        Thread.sleep(1000)

        val searchedUsers = elasticClient.searchUsers("Tomm")
        assertEquals(1, searchedUsers.size)
        assert(usersAreEqual(userOne, searchedUsers[0]))
    }

    @Test
    fun advancedUserSearch() {
        val userOne = database.createUser("Tommy", "1234", "google")
        val userTwo = database.createUser("Charlie", "4321", "google")
        val userThree = database.createUser("Tommi", "12", "google")
        val userFour = database.createUser("Tommu", "34", "google")
        elasticClient.indexUser(userOne)
        elasticClient.indexUser(userTwo)
        elasticClient.indexUser(userThree)
        elasticClient.indexUser(userFour)
        Thread.sleep(1000)

        val searchedUsers = elasticClient.searchUsers("Tomm")
        val userIds = searchedUsers.map { user -> user.id }.toSet()
        assertEquals(3, userIds.size)
        assert(userIds.contains(userOne.id))
        assert(userIds.contains(userThree.id))
        assert(userIds.contains(userFour.id))
    }

    @Test
    fun exactCardpackSearch() {
        val user = database.createUser("Tommy", "1234", "google")
        val cardpackOne = database.createCardpack("cardpackOne", user.id)
        val cardpackTwo = database.createCardpack("cardpackTwo", user.id)
        elasticClient.indexCardpack(cardpackOne)
        elasticClient.indexCardpack(cardpackTwo)
        Thread.sleep(1000)

        val searchedCardpacks = elasticClient.searchCardpacks("cardpackOne")
        assertEquals(1, searchedCardpacks.size)
        assertCardpackEquals(cardpackOne, searchedCardpacks[0])
    }

    @Test
    fun partialCardpackSearch() {
        val user = database.createUser("Tommy", "1234", "google")
        val cardpackOne = database.createCardpack("cardpackOne", user.id)
        val cardpackTwo = database.createCardpack("cardpackTwo", user.id)
        elasticClient.indexCardpack(cardpackOne)
        elasticClient.indexCardpack(cardpackTwo)
        Thread.sleep(1000)

        val searchedCardpacks = elasticClient.searchCardpacks("cardpackOn")
        assertEquals(1, searchedCardpacks.size)
        assertCardpackEquals(cardpackOne, searchedCardpacks[0])
    }

    @Test
    fun advancedCardpackSearch() {
        val user = database.createUser("Tommy", "1234", "google")
        val cardpackOne = database.createCardpack("cardpackA", user.id)
        val cardpackTwo = database.createCardpack("cardpackB", user.id)
        val cardpackThree = database.createCardpack("cardpackC", user.id)
        val cardpackFour = database.createCardpack("cardpackD", user.id)
        elasticClient.indexCardpack(cardpackOne)
        elasticClient.indexCardpack(cardpackTwo)
        elasticClient.indexCardpack(cardpackThree)
        elasticClient.indexCardpack(cardpackFour)
        Thread.sleep(2000)

        val searchedCardpacks = elasticClient.searchCardpacks("cardpack")
        val cardpackIds = searchedCardpacks.map { cardpack -> cardpack.id }.toSet()
        assertEquals(4, cardpackIds.size)
        assert(cardpackIds.contains(cardpackOne.id))
        assert(cardpackIds.contains(cardpackTwo.id))
        assert(cardpackIds.contains(cardpackThree.id))
        assert(cardpackIds.contains(cardpackFour.id))
    }
}