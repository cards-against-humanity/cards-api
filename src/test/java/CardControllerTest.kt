import com.mongodb.MongoClient
import org.bson.Document
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import route.card.CardController
import route.user.User

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import route.card.Cardpack

class CardControllerTest {
    private val mockMvc = MockMvcBuilders.standaloneSetup(CardController()).build()

    private var userOne: User? = null
    private var userTwo: User? = null

    companion object {

        @BeforeAll
        @JvmStatic
        fun initialize() {
            database.Instance.mongo = MongoClient("localhost").getDatabase("appNameTest")
        }
    }

    @BeforeEach
    fun reset() {
        database.Instance.resetMongo()
        userOne = User.create("4321", "google", "Quinn")
        userTwo = User.create("1234", "google", "Charlie")
    }

    @Test
    fun createCardpack() {
        var putReq: MockHttpServletRequestBuilder
        val result: ResultActions
        var cardpacks: List<Cardpack>

        cardpacks = Cardpack.Companion.get(userOne!!)
        assert(cardpacks.isEmpty())

        putReq = put("/${userOne!!.id}/cardpack").contentType(MediaType.APPLICATION_JSON).content(Document("name", "cardpackOne").toJson())
        result = mockMvc.perform(putReq).andExpect(status().isOk)

        cardpacks = Cardpack.Companion.get(userOne!!)
        assert(cardpacks.size == 1)
        assert(resEquals(result, cardpacks[0]))

        putReq = put("/fakeuserid/cardpack").contentType(MediaType.APPLICATION_JSON).content(Document("name", "cardpackOne").toJson())
        mockMvc.perform(putReq).andExpect(status().isNotFound)

        putReq = put("/${userOne!!.id}/cardpack").contentType(MediaType.APPLICATION_JSON).content(Document("foo", "bar").toJson())
        mockMvc.perform(putReq).andExpect(status().isBadRequest)
    }
}
