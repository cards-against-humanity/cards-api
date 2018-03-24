import com.fasterxml.jackson.databind.ObjectMapper
import org.bson.Document
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import route.card.CardController

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import route.card.memorymodel.MemoryCardCollection
import route.card.model.CardCollection
import route.card.model.CardpackModel
import route.user.memorymodel.MemoryUserCollection
import route.user.model.UserCollection
import java.util.ArrayList
import kotlin.test.assertEquals

class CardControllerTest {
    private var userCollection: UserCollection = MemoryUserCollection()
    private var cardCollection: CardCollection = MemoryCardCollection(userCollection)

    private var mockMvc = MockMvcBuilders.standaloneSetup(CardController(userCollection, cardCollection)).build()

    private var userOne = userCollection.createUser("Quinn", "4321", "google")
    private var userTwo = userCollection.createUser("Charlie", "1234", "google")

    @BeforeEach
    fun reset() {
        userCollection = MemoryUserCollection()
        cardCollection = MemoryCardCollection(userCollection)
        mockMvc = MockMvcBuilders.standaloneSetup(CardController(userCollection, cardCollection)).build()
        userOne = userCollection.createUser("Quinn", "4321", "google")
        userTwo = userCollection.createUser("Charlie", "1234", "google")
    }

    @Test
    fun createCardpack() {
        var putReq: MockHttpServletRequestBuilder
        val result: ResultActions
        var cardpacks: List<CardpackModel>

        cardpacks = cardCollection.getCardpacks(userOne.id)
        assert(cardpacks.isEmpty())

        putReq = put("/${userOne.id}/cardpack").contentType(MediaType.APPLICATION_JSON).content(Document("name", "cardpackOne").toJson())
        result = mockMvc.perform(putReq).andExpect(status().isOk)

        cardpacks = cardCollection.getCardpacks(userOne.id)
        assert(cardpacks.size == 1)
        assert(resEquals(result, cardpacks[0]))

        putReq = put("/fakeuserid/cardpack").contentType(MediaType.APPLICATION_JSON).content(Document("name", "cardpackOne").toJson())
        mockMvc.perform(putReq).andExpect(status().isNotFound)

        putReq = put("/${userOne.id}/cardpack").contentType(MediaType.APPLICATION_JSON).content(Document("foo", "bar").toJson())
        mockMvc.perform(putReq).andExpect(status().isBadRequest)
    }

    @Test
    fun getCardpack() {
        var getReq: MockHttpServletRequestBuilder
        val result: ResultActions
        val cardpack = cardCollection.createCardpack("cardpack", userOne.id)

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
        val cardpack = cardCollection.createCardpack("cardpackOne", userOne.id)

        patchReq = patch("/cardpack/" + cardpack.id).contentType(MediaType.APPLICATION_JSON).content(Document("foo", "bar").toJson())
        mockMvc.perform(patchReq).andExpect(status().isBadRequest)

        patchList = ArrayList()
        patchReq = patch("/cardpack/" + "fakeuserid").contentType(MediaType.APPLICATION_JSON).content(ObjectMapper().writeValueAsString(patchList))
        mockMvc.perform(patchReq).andExpect(status().isNotFound)

        patchList = ArrayList()
        patchList.add(Document("foo", "bar"))
        patchReq = patch("/cardpack/" + cardpack.id).contentType(MediaType.APPLICATION_JSON).content(ObjectMapper().writeValueAsString(patchList))
        mockMvc.perform(patchReq).andExpect(status().isBadRequest)
        assert(cardpackEquals(cardCollection.getCardpack(cardpack.id), cardpack))

        patchList = ArrayList()
        patchList.add(Document("op", "replace").append("path", "/fakepath"))
        patchReq = patch("/cardpack/" + cardpack.id).contentType(MediaType.APPLICATION_JSON).content(ObjectMapper().writeValueAsString(patchList))
        mockMvc.perform(patchReq).andExpect(status().isBadRequest)
        assert(cardpackEquals(cardCollection.getCardpack(cardpack.id), cardpack))

        patchList = ArrayList()
        patchList.add(Document("op", "replace").append("path", "/name").append("value", "newName"))
        patchReq = patch("/cardpack/" + cardpack.id).contentType(MediaType.APPLICATION_JSON).content(ObjectMapper().writeValueAsString(patchList))
        mockMvc.perform(patchReq).andExpect(status().isOk)
        assertEquals("newName", cardCollection.getCardpack(cardpack.id).name)
    }

    @Test
    fun deleteCardpack() {
        val cardpack = cardCollection.createCardpack("cardpack", userOne.id)
        var deleteReq: MockHttpServletRequestBuilder

        deleteReq = delete("/cardpack/" + cardpack.id)
        mockMvc.perform(deleteReq).andExpect(status().isOk)

        assertThrows(Exception::class.java) { cardCollection.getCardpack(cardpack.id) }

        deleteReq = delete("/cardpack/" + "fake_cardpack_id")
        mockMvc.perform(deleteReq).andExpect(status().isNotFound)
    }

    @Test
    fun createCard() {
        val cardpack = cardCollection.createCardpack("cardpack", userOne.id)
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
        val cardpack = cardCollection.createCardpack("cardpack", userOne.id)
        cardCollection.createCards(arrayListOf("foo", "bar"), cardpack.id)

        val result = mockMvc.perform(get("/cardpack/" + cardpack.id + "/cards"))
        val cardTexts = toList(result) as List<String>
        assert(cardTexts.size == 2)
        assert(cardTexts[0] == "foo")
        assert(cardTexts[1] == "bar")

        mockMvc.perform(get("/cardpack/fake_id/cards")).andExpect(status().isNotFound)
    }

    @Test
    fun deleteCard() {
        val cardpack = cardCollection.createCardpack("cardpack", userOne.id)
        cardCollection.createCards(arrayListOf("foo", "bar"), cardpack.id)
        assert(cardpack.getCards().size == 2)
        mockMvc.perform(delete("/card/${cardpack.getCards()[0].id}"))
        assert(cardpack.getCards().size == 1)
        assert(cardpack.getCards()[0].text == "bar")

        mockMvc.perform(delete("/card/fake_id")).andExpect(status().isNotFound)
    }
}
