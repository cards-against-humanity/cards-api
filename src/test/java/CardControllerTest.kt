import com.fasterxml.jackson.databind.ObjectMapper
import database.DatabaseCollection
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
import database.memorymodel.MemoryCardCollection
import route.card.model.CardCollection
import route.card.model.CardpackModel
import database.memorymodel.*
import route.card.JsonBlackCard
import route.card.JsonWhiteCard
import route.user.model.UserCollection
import java.util.ArrayList
import kotlin.test.assertEquals

class CardControllerTest {
    private var database: DatabaseCollection = MemoryDatabaseCollection()

    private var mockMvc = MockMvcBuilders.standaloneSetup(CardController(database)).build()

    private var userOne = database.createUser("Quinn", "4321", "google")
    private var userTwo = database.createUser("Charlie", "1234", "google")

    @BeforeEach
    fun reset() {
        database = MemoryDatabaseCollection()
        mockMvc = MockMvcBuilders.standaloneSetup(CardController(database)).build()
        userOne = database.createUser("Quinn", "4321", "google")
        userTwo = database.createUser("Charlie", "1234", "google")
    }

    @Test
    fun createCardpack() {
        var putReq: MockHttpServletRequestBuilder
        val result: ResultActions
        var cardpacks: List<CardpackModel>

        cardpacks = database.getCardpacks(userOne.id)
        assert(cardpacks.isEmpty())

        putReq = put("/${userOne.id}/cardpack").contentType(MediaType.APPLICATION_JSON).content(Document("name", "cardpackOne").toJson())
        result = mockMvc.perform(putReq).andExpect(status().isOk)

        cardpacks = database.getCardpacks(userOne.id)
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
        val cardpack = database.createCardpack("cardpack", userOne.id)

        getReq = get("/cardpack/${cardpack.id}")
        result = mockMvc.perform(getReq).andExpect(status().isOk)
        assert(resEquals(result, cardpack))

        getReq = get("/cardpack/fakecardpackid")
        mockMvc.perform(getReq).andExpect(status().isNotFound)
    }

    @Test
    fun getCardpacksByUser() {
        var getReq: MockHttpServletRequestBuilder
        var result: ResultActions
        val cardpackOne = database.createCardpack("cardpackOne", userOne.id)
        val cardpackTwo = database.createCardpack("cardpackTwo", userOne.id)

        getReq = get("/${userOne.id}/cardpacks/")
        result = mockMvc.perform(getReq).andExpect(status().isOk)
        assert(resEquals(result, listOf(cardpackOne, cardpackTwo)))

        getReq = get("/${userTwo.id}/cardpacks/")
        result = mockMvc.perform(getReq).andExpect(status().isOk)
        assert(resEquals(result, listOf()))

        getReq = get("/${"fake_user_id"}/cardpacks")
        mockMvc.perform(getReq).andExpect(status().isNotFound)
    }

    @Test
    fun patchCardpack() {
        var patchReq: MockHttpServletRequestBuilder
        var patchList: MutableList<Document>
        val cardpack = database.createCardpack("cardpackOne", userOne.id)

        patchReq = patch("/cardpack/" + cardpack.id).contentType(MediaType.APPLICATION_JSON).content(Document("foo", "bar").toJson())
        mockMvc.perform(patchReq).andExpect(status().isBadRequest)

        patchList = ArrayList()
        patchReq = patch("/cardpack/" + "fakeuserid").contentType(MediaType.APPLICATION_JSON).content(ObjectMapper().writeValueAsString(patchList))
        mockMvc.perform(patchReq).andExpect(status().isNotFound)

        patchList = ArrayList()
        patchList.add(Document("foo", "bar"))
        patchReq = patch("/cardpack/" + cardpack.id).contentType(MediaType.APPLICATION_JSON).content(ObjectMapper().writeValueAsString(patchList))
        mockMvc.perform(patchReq).andExpect(status().isBadRequest)
        assertCardpackEquals(database.getCardpack(cardpack.id), cardpack)

        patchList = ArrayList()
        patchList.add(Document("op", "replace").append("path", "/fakepath"))
        patchReq = patch("/cardpack/" + cardpack.id).contentType(MediaType.APPLICATION_JSON).content(ObjectMapper().writeValueAsString(patchList))
        mockMvc.perform(patchReq).andExpect(status().isBadRequest)
        assertCardpackEquals(database.getCardpack(cardpack.id), cardpack)

        patchList = ArrayList()
        patchList.add(Document("op", "replace").append("path", "/name").append("value", "newName"))
        patchReq = patch("/cardpack/" + cardpack.id).contentType(MediaType.APPLICATION_JSON).content(ObjectMapper().writeValueAsString(patchList))
        mockMvc.perform(patchReq).andExpect(status().isOk)
        assertEquals("newName", database.getCardpack(cardpack.id).name)
    }

    @Test
    fun deleteCardpack() {
        val cardpack = database.createCardpack("cardpack", userOne.id)
        var deleteReq: MockHttpServletRequestBuilder

        deleteReq = delete("/cardpack/" + cardpack.id)
        mockMvc.perform(deleteReq).andExpect(status().isOk)

        assertThrows(Exception::class.java) { database.getCardpack(cardpack.id) }

        deleteReq = delete("/cardpack/" + "fake_cardpack_id")
        mockMvc.perform(deleteReq).andExpect(status().isNotFound)
    }

    @Test
    fun createWhiteCards() {
        val cardpack = database.createCardpack("cardpack", userOne.id)
        var putReq: MockHttpServletRequestBuilder

        putReq = put("/cardpack/" + cardpack.id + "/cards/white").contentType(MediaType.APPLICATION_JSON).content(ObjectMapper().writeValueAsString(arrayListOf(JsonWhiteCard("card1"), JsonWhiteCard("card2"), JsonWhiteCard("card3"))))
        mockMvc.perform(putReq).andExpect(status().isOk)
        assert(database.getCardpack(cardpack.id).whiteCards.size == 3)

        putReq = put("/cardpack/" + cardpack.id + "/cards/white").contentType(MediaType.APPLICATION_JSON).content(ObjectMapper().writeValueAsString(Document("foo", "bar")))
        mockMvc.perform(putReq).andExpect(status().isBadRequest)

        putReq = put("/cardpack/" + "fake_cardpack_id" + "/cards/white").contentType(MediaType.APPLICATION_JSON).content(ObjectMapper().writeValueAsString(arrayListOf(JsonWhiteCard("card"))))
        mockMvc.perform(putReq).andExpect(status().isNotFound)
    }

    @Test
    fun createBlackCards() {
        val cardpack = database.createCardpack("cardpack", userOne.id)
        var putReq: MockHttpServletRequestBuilder

        putReq = put("/cardpack/" + cardpack.id + "/cards/black").contentType(MediaType.APPLICATION_JSON).content(ObjectMapper().writeValueAsString(arrayListOf(JsonBlackCard("card1", 1), JsonBlackCard("card2", 1), JsonBlackCard("card3", 1))))
        mockMvc.perform(putReq).andExpect(status().isOk)
        assert(database.getCardpack(cardpack.id).blackCards.size == 3)

        putReq = put("/cardpack/" + cardpack.id + "/cards/black").contentType(MediaType.APPLICATION_JSON).content(ObjectMapper().writeValueAsString(Document("foo", "bar")))
        mockMvc.perform(putReq).andExpect(status().isBadRequest)

        putReq = put("/cardpack/" + "fake_cardpack_id" + "/cards/black").contentType(MediaType.APPLICATION_JSON).content(ObjectMapper().writeValueAsString(arrayListOf(JsonBlackCard("card", 2))))
        mockMvc.perform(putReq).andExpect(status().isNotFound)
    }

    @Test
    fun deleteWhiteCard() {
        var cardpack = database.createCardpack("cardpack", userOne.id)
        database.createWhiteCards(arrayListOf(JsonWhiteCard("foo"), JsonWhiteCard("bar")), cardpack.id)
        cardpack = database.getCardpack(cardpack.id)
        mockMvc.perform(delete("/cards/white/${cardpack.whiteCards[0].id}")).andExpect(status().isOk)
        cardpack = database.getCardpack(cardpack.id)
        assert(cardpack.whiteCards.size == 1)
        assert(cardpack.whiteCards[0].text == "bar")

        mockMvc.perform(delete("/cards/white/fake_id")).andExpect(status().isNotFound)
    }

    @Test
    fun deleteBlackCard() {
        var cardpack = database.createCardpack("cardpack", userOne.id)
        database.createBlackCards(arrayListOf(JsonBlackCard("foo", 1), JsonBlackCard("bar", 1)), cardpack.id)
        cardpack = database.getCardpack(cardpack.id)
        mockMvc.perform(delete("/cards/black/${cardpack.blackCards[0].id}")).andExpect(status().isOk)
        cardpack = database.getCardpack(cardpack.id)
        assert(cardpack.blackCards.size == 1)
        assert(cardpack.blackCards[0].text == "bar")

        mockMvc.perform(delete("/cards/black/fake_id")).andExpect(status().isNotFound)
    }
}
