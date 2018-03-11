import com.mongodb.MongoClient
import org.bson.Document

import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import route.user.User

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows

class UserTest {
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
        userOne = User.create("455238", "twitter", "Tommy")
        userTwo = User.create("13218", "facebook", "Stephen")
    }

    @Test
    fun getNonexistentUser() {
        assertThrows(NullPointerException::class.java) { User.get(ObjectId("5a76252d14ec8c05b81b5b00")) }
    }

    @Test
    fun create() {
        val oAuthId = "1234567890"
        val oAuthProvider = "google"
        val name = "Chuck Strange"
        User.create(oAuthId, oAuthProvider, name)
        val user = User.get(oAuthId, oAuthProvider)
        assertEquals(user.oAuthId, oAuthId)
        assertEquals(user.oAuthProvider, oAuthProvider)
        assertEquals(user.name, name)
    }

    @Test
    fun getFromIds() {
        val users = User.get(ObjectId(userOne!!.id), ObjectId(userTwo!!.id))

        assert(users[0].id == userOne!!.id)
        assert(users[0].name == userOne!!.name)
        assert(users[0].oAuthId == userOne!!.oAuthId)
        assert(users[0].oAuthProvider == userOne!!.oAuthProvider)

        assert(users[1].id == userTwo!!.id)
        assert(users[1].name == userTwo!!.name)
        assert(users[1].oAuthId == userTwo!!.oAuthId)
        assert(users[1].oAuthProvider == userTwo!!.oAuthProvider)
    }

    @Test
    fun getFromId() {
        val user = User.get(ObjectId(userOne!!.id))

        assert(user.id == userOne!!.id)
        assert(user.name == userOne!!.name)
        assert(user.oAuthId == userOne!!.oAuthId)
        assert(user.oAuthProvider == userOne!!.oAuthProvider)
    }

    @Test
    fun equalsUser() {
        val u1 = User.get(ObjectId(userOne!!.id))
        val u2 = User.get(userOne!!.oAuthId, userOne!!.oAuthProvider)
        assert(u1.equals(u2))
        assert (!u1.equals(null))
    }

    @Test
    fun equalsMap() {
        val user = User.get(userOne!!.oAuthId, userOne!!.oAuthProvider)
        val map = Document("id", user.id).append("name", user.name).append("oAuthId", user.oAuthId).append("oAuthProvider", user.oAuthProvider)
        assert(user.equals(map))
        map.append("name", user.name + "asdf")
        assert(!user.equals(map))
    }

    @Test
    fun setName() {
        val name = "Genji"
        userOne!!.setName(name)
        val userTwo = User.get(ObjectId(userOne!!.id))
        assert(userOne!!.name == name)
        assert(userTwo!!.name == name)
    }
}