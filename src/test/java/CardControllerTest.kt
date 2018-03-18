import com.fasterxml.jackson.databind.ObjectMapper
import com.mongodb.MongoClient
import org.bson.Document
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertThrows
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
import route.card.Card
import route.card.Cardpack
import java.util.ArrayList

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

    @Test
    fun getCardpack() {
        var getReq: MockHttpServletRequestBuilder
        val result: ResultActions
        val cardpack = Cardpack.create("cardpack", userOne!!)

        getReq = get("/cardpack/${cardpack.id}")
        result = mockMvc.perform(getReq).andExpect(status().isOk)
        assert(resEquals(result, cardpack))

        getReq = get("/cardpack/fakecardpackid")
        mockMvc.perform(getReq).andExpect(status().isNotFound)
    }

    @Test
    fun patchCardpack() {
        var patchReq: MockHttpServletRequestBuilder
        var patchList: MutableList<Document>
        val cardpack = Cardpack.create("cardpackOne", userOne!!)

        patchReq = patch("/cardpack/" + cardpack.id).contentType(MediaType.APPLICATION_JSON).content(Document("foo", "bar").toJson())
        mockMvc.perform(patchReq).andExpect(status().isBadRequest)

        patchList = ArrayList()
        patchReq = patch("/cardpack/" + "fakeuserid").contentType(MediaType.APPLICATION_JSON).content(ObjectMapper().writeValueAsString(patchList))
        mockMvc.perform(patchReq).andExpect(status().isNotFound)

        patchList = ArrayList()
        patchList.add(Document("foo", "bar"))
        patchReq = patch("/cardpack/" + cardpack.id).contentType(MediaType.APPLICATION_JSON).content(ObjectMapper().writeValueAsString(patchList))
        mockMvc.perform(patchReq).andExpect(status().isBadRequest)
        assert(Cardpack.get(ObjectId(cardpack.id)) == cardpack)

        patchList = ArrayList()
        patchList.add(Document("op", "replace").append("path", "/fakepath"))
        patchReq = patch("/cardpack/" + cardpack.id).contentType(MediaType.APPLICATION_JSON).content(ObjectMapper().writeValueAsString(patchList))
        mockMvc.perform(patchReq).andExpect(status().isBadRequest)
        assert(Cardpack.get(ObjectId(cardpack.id)) == cardpack)

        patchList = ArrayList()
        patchList.add(Document("op", "replace").append("path", "/name").append("value", "newName"))
        patchReq = patch("/cardpack/" + cardpack.id).contentType(MediaType.APPLICATION_JSON).content(ObjectMapper().writeValueAsString(patchList))
        mockMvc.perform(patchReq).andExpect(status().isOk)
        assert(Cardpack.get(ObjectId(cardpack.id)).name == "newName")
    }

    @Test
    fun deleteCardpack() {
        val cardpack = Cardpack.create("cardpack", userOne!!)
        var deleteReq: MockHttpServletRequestBuilder

        deleteReq = delete("/cardpack/" + cardpack.id)
        mockMvc.perform(deleteReq).andExpect(status().isOk)

        assertThrows(Exception::class.java) { Cardpack.get(ObjectId(cardpack.id)) }

        deleteReq = delete("/cardpack/" + "fake_cardpack_id")
        mockMvc.perform(deleteReq).andExpect(status().isNotFound)
    }

    @Test
    fun createCard() {
        val cardpack = Cardpack.create("cardpack", userOne!!)
        var putReq: MockHttpServletRequestBuilder

        putReq = put("/cardpack/" + cardpack.id + "/cards").contentType(MediaType.APPLICATION_JSON).content(ObjectMapper().writeValueAsString(arrayListOf("card1", "card2", "card3")))
        mockMvc.perform(putReq).andExpect(status().isOk)
        assert(cardpack.getCards().size == 3)
        assert(cardpack.getCards()[0].text == "card1")
        assert(cardpack.getCards()[1].text == "card2")
        assert(cardpack.getCards()[2].text == "card3")

        putReq = put("/cardpack/" + cardpack.id + "/cards").contentType(MediaType.APPLICATION_JSON).content(ObjectMapper().writeValueAsString(Document("foo", "bar")))
        mockMvc.perform(putReq).andExpect(status().isBadRequest)
    }

    @Test
    fun getCards() {
        val cardpack = Cardpack.create("cardpack", userOne!!)
        Card.Companion.create(arrayListOf("foo", "bar"), cardpack)

        val result = mockMvc.perform(get("/cardpack/" + cardpack.id + "/cards"))
        val cardTexts = toList(result) as List<String>
        assert(cardTexts.size == 2)
        assert(cardTexts[0] == "foo")
        assert(cardTexts[1] == "bar")

        mockMvc.perform(get("/cardpack/fake_id/cards")).andExpect(status().isNotFound)
    }
}
