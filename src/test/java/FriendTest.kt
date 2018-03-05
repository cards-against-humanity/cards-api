import com.mongodb.MongoClient

import com.mongodb.MongoWriteException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import route.user.Friend
import route.user.User

import org.junit.jupiter.api.Assertions.assertThrows

class FriendTest {
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
    fun noDuplicateRequests() {
        Friend.addFriend(userOne!!, userTwo!!)
        assertThrows(MongoWriteException::class.java) { Friend.addFriend(userOne!!, userTwo!!) }
    }

    @Test
    fun friendRequests() {
        var userOneRequestsSent = Friend.getSentRequests(userOne!!)
        var userTwoRequestsSent = Friend.getSentRequests(userTwo!!)
        var userOneRequestsReceived = Friend.getReceivedRequests(userOne!!)
        var userTwoRequestsReceived = Friend.getReceivedRequests(userTwo!!)
        assert(userOneRequestsSent.isEmpty())
        assert(userTwoRequestsSent.isEmpty())
        assert(userOneRequestsReceived.isEmpty())
        assert(userTwoRequestsReceived.isEmpty())

        Friend.addFriend(userOne!!, userTwo!!)

        userOneRequestsSent = Friend.getSentRequests(userOne!!)
        userTwoRequestsSent = Friend.getSentRequests(userTwo!!)
        userOneRequestsReceived = Friend.getReceivedRequests(userOne!!)
        userTwoRequestsReceived = Friend.getReceivedRequests(userTwo!!)
        assert(userOneRequestsSent.size == 1)
        assert(userTwoRequestsSent.isEmpty())
        assert(userOneRequestsReceived.isEmpty())
        assert(userTwoRequestsReceived.size == 1)
        assert(userOneRequestsSent[0] == userTwo)
        assert(userTwoRequestsReceived[0] == userOne)

        Friend.addFriend(userTwo!!, userOne!!)

        userOneRequestsSent = Friend.getSentRequests(userOne!!)
        userTwoRequestsSent = Friend.getSentRequests(userTwo!!)
        userOneRequestsReceived = Friend.getReceivedRequests(userOne!!)
        userTwoRequestsReceived = Friend.getReceivedRequests(userTwo!!)
        assert(userOneRequestsSent.size == 0)
        assert(userTwoRequestsSent.size == 0)
        assert(userOneRequestsReceived.size == 0)
        assert(userTwoRequestsReceived.size == 0)
    }

    @Test
    fun friends() {
        var userOneFriends = Friend.getFriends(userOne!!)
        var userTwoFriends = Friend.getFriends(userTwo!!)
        assert(userOneFriends.size == 0)
        assert(userTwoFriends.size == 0)
        Friend.addFriend(userOne!!, userTwo!!)
        Friend.addFriend(userTwo!!, userOne!!)
        userOneFriends = Friend.getFriends(userOne!!)
        userTwoFriends = Friend.getFriends(userTwo!!)
        assert(userOneFriends.size == 1)
        assert(userTwoFriends.size == 1)
        assert(userOneFriends[0] == userTwo)
        assert(userTwoFriends[0] == userOne)
    }

    @Test
    fun areFriends() {
        assert(!Friend.areFriends(userOne!!, userTwo!!))
        Friend.addFriend(userOne!!, userTwo!!)
        assert(!Friend.areFriends(userOne!!, userTwo!!))
        Friend.addFriend(userTwo!!, userOne!!)
        assert(Friend.areFriends(userOne!!, userTwo!!))
    }

    @Test
    fun removeFriends() {
        assert(Friend.getSentRequests(userOne!!).isEmpty())
        assert(Friend.getReceivedRequests(userTwo!!).isEmpty())
        Friend.addFriend(userOne!!, userTwo!!)
        assert(Friend.getSentRequests(userOne!!).size == 1)
        assert(Friend.getReceivedRequests(userTwo!!).size == 1)
        Friend.removeFriend(userOne!!, userTwo!!)
        assert(Friend.getSentRequests(userOne!!).isEmpty())
        assert(Friend.getReceivedRequests(userTwo!!).isEmpty())
        Friend.addFriend(userOne!!, userTwo!!)
        Friend.addFriend(userTwo!!, userOne!!)
        Friend.removeFriend(userTwo!!, userOne!!)
        assert(Friend.getSentRequests(userOne!!).isEmpty())
        assert(Friend.getReceivedRequests(userTwo!!).isEmpty())
    }
}
