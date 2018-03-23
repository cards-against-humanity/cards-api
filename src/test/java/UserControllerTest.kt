import com.fasterxml.jackson.databind.ObjectMapper
import org.bson.Document
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import route.user.UserController

import java.util.ArrayList

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import route.user.memorymodel.MemoryFriendCollection
import route.user.memorymodel.MemoryUserCollection

class UserControllerTest {

    private val userCollection = MemoryUserCollection()
    private val friendCollection = MemoryFriendCollection(userCollection)

    private val mockMvc = MockMvcBuilders.standaloneSetup(UserController(userCollection, friendCollection)).build()

    private var userOne = userCollection.createUser("Quinn", "4321", "google")
    private var userTwo = userCollection.createUser("Charlie", "1234", "google")

    @BeforeEach
    fun reset() {
        userCollection.clear()
        friendCollection.clear()
        userOne = userCollection.createUser("Quinn", "4321", "google")
        userTwo = userCollection.createUser("Charlie", "1234", "google")
    }

    @Test
    @Throws(Exception::class)
    fun getUserById() {
        var getReq: MockHttpServletRequestBuilder
        val result: ResultActions

        getReq = get("/user/" + userOne.id)
        result = mockMvc.perform(getReq).andExpect(status().isOk)
        assert(resEquals(result, userOne))

        getReq = get("/user/" + userOne.id + "asdf")
        mockMvc.perform(getReq).andExpect(status().isNotFound)
    }

    @Test
    @Throws(Exception::class)
    fun getUserGeneric() {
        var getReq: MockHttpServletRequestBuilder
        var result: ResultActions

        getReq = get("/user").param("id", userTwo.id)
        result = mockMvc.perform(getReq).andExpect(status().isOk)
        assert(resEquals(result, userTwo))

        getReq = get("/user").param("oAuthId", userTwo.oAuthId).param("oAuthProvider", userTwo.oAuthProvider)
        result = mockMvc.perform(getReq).andExpect(status().isOk)
        assert(resEquals(result, userTwo))

        getReq = get("/user")
        mockMvc.perform(getReq).andExpect(status().isBadRequest)

        getReq = get("/user").param("oAuthId", userTwo.oAuthId)
        mockMvc.perform(getReq).andExpect(status().isBadRequest)

        getReq = get("/user").param("oAuthProvider", userTwo.oAuthProvider)
        mockMvc.perform(getReq).andExpect(status().isBadRequest)

        getReq = get("/user").param("oAuthId", userTwo.oAuthId).param("oAuthProvider", userTwo.oAuthProvider).param("id", userTwo.id)
        mockMvc.perform(getReq).andExpect(status().isBadRequest)

        getReq = get("/user").param("id", "thisisafakeid")
        mockMvc.perform(getReq).andExpect(status().isNotFound)

        getReq = get("/user").param("oAuthId", userTwo.oAuthId).param("oAuthProvider", "fakeoauthprovider")
        mockMvc.perform(getReq).andExpect(status().isNotFound)
    }

    @Test
    @Throws(Exception::class)
    fun putUser() {
        var putReq: MockHttpServletRequestBuilder
        val result: ResultActions
        val userDoc: Document = Document()
                .append("name", "Hulk Hogan")
                .append("oAuthId", "123456")
                .append("oAuthProvider", "google")

        putReq = put("/user").contentType(MediaType.APPLICATION_JSON).content(userDoc.toJson())
        result = mockMvc.perform(putReq).andExpect(status().isOk)
        val createdUser = userCollection.getUser(toMap(result)["id"] as String)
        resEquals(result, createdUser)

        putReq = put("/user").contentType(MediaType.APPLICATION_JSON).content(userDoc.toJson())
        mockMvc.perform(putReq).andExpect(status().isBadRequest)
    }

    @Test
    @Throws(Exception::class)
    fun addFriend() {
        var putReq: MockHttpServletRequestBuilder

        putReq = put("/user/" + userOne.id + "/friends/" + userTwo.id)
        mockMvc.perform(putReq).andExpect(status().isOk)

        putReq = put("/user/" + userOne.id + "/friends/" + userTwo.id)
        mockMvc.perform(putReq).andExpect(status().isBadRequest)

        putReq = put("/user/" + "thisisafakeid" + "/friends/" + userTwo.id)
        mockMvc.perform(putReq).andExpect(status().isNotFound)

        putReq = put("/user/" + userOne.id + "/friends/" + userOne.id)
        mockMvc.perform(putReq).andExpect(status().isBadRequest)
    }

    @Test
    @Throws(Exception::class)
    fun removeFriend() {
        var deleteReq: MockHttpServletRequestBuilder

        friendCollection.addFriend(userOne.id, userTwo.id)
        deleteReq = delete("/user/" + userOne.id + "/friends/" + userTwo.id)
        mockMvc.perform(deleteReq).andExpect(status().isOk)

        deleteReq = delete("/user/" + userOne.id + "/friends/" + userOne.id)
        mockMvc.perform(deleteReq).andExpect(status().isBadRequest)

        deleteReq = delete("/user/" + "thisisafakeid" + "/friends/" + userTwo.id)
        mockMvc.perform(deleteReq).andExpect(status().isNotFound)
    }

    @Test
    @Throws(Exception::class)
    fun getFriends() {
        var getReq: MockHttpServletRequestBuilder
        var result: ResultActions

        getReq = get("/user/" + userOne.id + "/friends")
        result = mockMvc.perform(getReq).andExpect(status().isOk)
        assert(toList(result).isEmpty())

        getReq = get("/user/" + userTwo.id + "/friends")
        result = mockMvc.perform(getReq).andExpect(status().isOk)
        assert(toList(result).isEmpty())

        friendCollection.addFriend(userOne.id, userTwo.id)

        getReq = get("/user/" + userOne.id + "/friends")
        result = mockMvc.perform(getReq).andExpect(status().isOk)
        assert(toList(result).isEmpty())

        getReq = get("/user/" + userTwo.id + "/friends")
        result = mockMvc.perform(getReq).andExpect(status().isOk)
        assert(toList(result).isEmpty())

        friendCollection.addFriend(userTwo.id, userOne.id)

        getReq = get("/user/" + userOne.id + "/friends")
        result = mockMvc.perform(getReq).andExpect(status().isOk)
        assert(toList(result).size == 1)
        assert(userEquals(userTwo, toList(result)[0]))

        getReq = get("/user/" + userTwo.id + "/friends")
        result = mockMvc.perform(getReq).andExpect(status().isOk)
        assert(toList(result).size == 1)
        assert(userEquals(userOne, toList(result)[0]))

        friendCollection.removeFriend(userOne.id, userTwo.id)

        getReq = get("/user/" + userOne.id + "/friends")
        result = mockMvc.perform(getReq).andExpect(status().isOk)
        assert(toList(result).isEmpty())

        getReq = get("/user/" + userTwo.id + "/friends")
        result = mockMvc.perform(getReq).andExpect(status().isOk)
        assert(toList(result).isEmpty())


        getReq = get("/user/" + "thisisafakeid" + "/friends")
        mockMvc.perform(getReq).andExpect(status().isNotFound)
    }

    @Test
    @Throws(Exception::class)
    fun getFriendRequests() {
        var getReq: MockHttpServletRequestBuilder
        var result: ResultActions

        getReq = get("/user/" + userOne.id + "/friends/requests/sent")
        result = mockMvc.perform(getReq).andExpect(status().isOk)
        assert(toList(result).isEmpty())
        getReq = get("/user/" + userOne.id + "/friends/requests/received")
        result = mockMvc.perform(getReq).andExpect(status().isOk)
        assert(toList(result).isEmpty())

        getReq = get("/user/" + userTwo.id + "/friends/requests/sent")
        result = mockMvc.perform(getReq).andExpect(status().isOk)
        assert(toList(result).isEmpty())
        getReq = get("/user/" + userTwo.id + "/friends/requests/received")
        result = mockMvc.perform(getReq).andExpect(status().isOk)
        assert(toList(result).isEmpty())

        friendCollection.addFriend(userOne.id, userTwo.id)

        getReq = get("/user/" + userOne.id + "/friends/requests/sent")
        result = mockMvc.perform(getReq).andExpect(status().isOk)
        assert(toList(result).size == 1)
        assert(userEquals(userTwo, toList(result)[0]))
        getReq = get("/user/" + userOne.id + "/friends/requests/received")
        result = mockMvc.perform(getReq).andExpect(status().isOk)
        assert(toList(result).isEmpty())

        getReq = get("/user/" + userTwo.id + "/friends/requests/sent")
        result = mockMvc.perform(getReq).andExpect(status().isOk)
        assert(toList(result).isEmpty())
        getReq = get("/user/" + userTwo.id + "/friends/requests/received")
        result = mockMvc.perform(getReq).andExpect(status().isOk)
        assert(toList(result).size == 1)
        assert(userEquals(userOne, toList(result)[0]))

        friendCollection.addFriend(userTwo.id, userOne.id)

        getReq = get("/user/" + userOne.id + "/friends/requests/sent")
        result = mockMvc.perform(getReq).andExpect(status().isOk)
        assert(toList(result).isEmpty())
        getReq = get("/user/" + userOne.id + "/friends/requests/received")
        result = mockMvc.perform(getReq).andExpect(status().isOk)
        assert(toList(result).isEmpty())

        getReq = get("/user/" + userTwo.id + "/friends/requests/sent")
        result = mockMvc.perform(getReq).andExpect(status().isOk)
        assert(toList(result).isEmpty())
        getReq = get("/user/" + userTwo.id + "/friends/requests/received")
        result = mockMvc.perform(getReq).andExpect(status().isOk)
        assert(toList(result).isEmpty())


        getReq = get("/user/" + "thisisafakeid" + "/friends/requests/sent")
        mockMvc.perform(getReq).andExpect(status().isNotFound)
        getReq = get("/user/" + "thisisafakeid" + "/friends/requests/received")
        mockMvc.perform(getReq).andExpect(status().isNotFound)
    }

    @Test
    @Throws(Exception::class)
    fun patchUser() {
        var patchReq: MockHttpServletRequestBuilder
        var patchList: List<Document>

        patchReq = patch("/user/" + userOne.id).contentType(MediaType.APPLICATION_JSON).content(Document("foo", "bar").toJson())
        mockMvc.perform(patchReq).andExpect(status().isBadRequest)

        patchList = ArrayList()
        patchReq = patch("/user/" + "fakeuserid").contentType(MediaType.APPLICATION_JSON).content(ObjectMapper().writeValueAsString(patchList))
        mockMvc.perform(patchReq).andExpect(status().isNotFound)

        patchList = ArrayList()
        patchList.add(Document("foo", "bar"))
        patchReq = patch("/user/" + userOne.id).contentType(MediaType.APPLICATION_JSON).content(ObjectMapper().writeValueAsString(patchList))
        mockMvc.perform(patchReq).andExpect(status().isBadRequest)
        assert(userCollection.getUser(userOne.id).name == userOne.name)

        patchList = ArrayList()
        patchList.add(Document("op", "replace").append("path", "/fakepath"))
        patchReq = patch("/user/" + userOne.id).contentType(MediaType.APPLICATION_JSON).content(ObjectMapper().writeValueAsString(patchList))
        mockMvc.perform(patchReq).andExpect(status().isBadRequest)
        assert(userCollection.getUser(userOne.id).name == userOne.name)

        patchList = ArrayList()
        patchList.add(Document("op", "replace").append("path", "/name").append("value", "newName"))
        patchReq = patch("/user/" + userOne.id).contentType(MediaType.APPLICATION_JSON).content(ObjectMapper().writeValueAsString(patchList))
        mockMvc.perform(patchReq).andExpect(status().isOk)
        assert(userCollection.getUser(userOne.id).name == "newName")
    }
}