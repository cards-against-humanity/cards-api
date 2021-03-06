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
import org.junit.jupiter.api.Assertions.assertThrows
import java.net.SocketException
import java.util.*
import kotlin.test.assertEquals

class ElasticClientTest {

    private var database: DatabaseCollection = MemoryDatabaseCollection()
    private val elasticRestClient = RestHighLevelClient(RestClient.builder(HttpHost("localhost", 9200)))
    private var elasticClient = ElasticClient(elasticRestClient, database, 1000)

    @BeforeEach
    fun reset() {
        database = MemoryDatabaseCollection()
        try {
            elasticRestClient.indices().delete(DeleteIndexRequest(ElasticClient.userIndex))
        } catch (e: Exception) { }
        try {
        elasticRestClient.indices().delete(DeleteIndexRequest(ElasticClient.userAutocompleteIndex))
        } catch (e: Exception) { }
        try {
            elasticRestClient.indices().delete(DeleteIndexRequest(ElasticClient.cardpackIndex))
        } catch (e: Exception) { }
        try {
            elasticRestClient.indices().delete(DeleteIndexRequest(ElasticClient.cardpackAutocompleteIndex))
        } catch (e: Exception) { }
        elasticClient = ElasticClient(elasticRestClient, database, 1000)
    }

    @Test
    fun connectionTimeout() {
        val startTime = Date().time
        val e = assertThrows(SocketException::class.java) { ElasticClient(RestHighLevelClient(RestClient.builder(HttpHost("localhost", 12345))), database, 5000) }
        assertEquals("Could not connect to Elasticsearch", e.message)
        assert(Date().time - startTime > 5000)
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
        Thread.sleep(2000)

        val searchedUsers = elasticClient.searchUsers("Tomm")
        val userIds = searchedUsers.map { user -> user.id }.toSet()
        assertEquals(3, userIds.size)
        assert(userIds.contains(userOne.id))
        assert(userIds.contains(userThree.id))
        assert(userIds.contains(userFour.id))
    }

    @Test
    fun userSearchNoIndexedData() {
        val searchedUsers = elasticClient.searchUsers("Tommy")
        assertEquals(0, searchedUsers.size)
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

    @Test
    fun cardpackSearchNoIndexedData() {
        val searchedCardpacks = elasticClient.searchCardpacks("cardpackOne")
        assertEquals(0, searchedCardpacks.size)
    }

    @Test
    fun userAutocomplete() {
        val userOne = database.createUser("Tommy Volk", "1234", "google")
        val userTwo = database.createUser("Charlie Strange", "4321", "google")
        val userThree = database.createUser("Tommy Strange", "12", "google")
        val userFour = database.createUser("Tommy Kohnen", "34", "google")
        elasticClient.indexUser(userOne)
        elasticClient.indexUser(userTwo)
        elasticClient.indexUser(userThree)
        elasticClient.indexUser(userFour)
        Thread.sleep(2000)

        var searchedUserNames: List<String>

        searchedUserNames = elasticClient.autoCompleteUserSearch("T")
        assertEquals(3, searchedUserNames.size)
        assert(searchedUserNames.contains("Tommy Volk"))
        assert(searchedUserNames.contains("Tommy Strange"))
        assert(searchedUserNames.contains("Tommy Kohnen"))

        searchedUserNames = elasticClient.autoCompleteUserSearch("V")
        assertEquals(1, searchedUserNames.size)
        assert(searchedUserNames.contains("Tommy Volk"))

        searchedUserNames = elasticClient.autoCompleteUserSearch("Str")
        assertEquals(2, searchedUserNames.size)
        assert(searchedUserNames.contains("Tommy Strange"))
        assert(searchedUserNames.contains("Charlie Strange"))
    }

    @Test
    fun userAutocompleteNoIndexedData() {
        val cardpackNames = elasticClient.autoCompleteUserSearch("a")
        assertEquals(0, cardpackNames.size)
    }

    @Test
    fun cardpackAutocomplete() {
        val user = database.createUser("Tommy", "1234", "google")
        val cardpackOne = database.createCardpack("asdf", user.id)
        val cardpackTwo = database.createCardpack("aaaa", user.id)
        val cardpackThree = database.createCardpack("fdfd", user.id)
        val cardpackFour = database.createCardpack("rrew", user.id)
        elasticClient.indexCardpack(cardpackOne)
        elasticClient.indexCardpack(cardpackTwo)
        elasticClient.indexCardpack(cardpackThree)
        elasticClient.indexCardpack(cardpackFour)
        Thread.sleep(2000)

        val cardpackNames = elasticClient.autoCompleteCardpackSearch("a")
        assertEquals(2, cardpackNames.size)
        assert(cardpackNames.contains("asdf"))
        assert(cardpackNames.contains("aaaa"))
    }

    @Test
    fun cardpackAutocompleteNoIndexedData() {
        val cardpackNames = elasticClient.autoCompleteCardpackSearch("a")
        assertEquals(0, cardpackNames.size)
    }
}