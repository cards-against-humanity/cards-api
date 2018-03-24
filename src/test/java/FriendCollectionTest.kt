import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import route.user.memorymodel.MemoryFriendCollection
import route.user.memorymodel.MemoryUserCollection
import route.user.model.FriendCollection
import route.user.model.UserCollection
import kotlin.test.assertEquals

class FriendCollectionTest {
    private data class CollectionGroup(
            val userCollection: UserCollection,
            val friendCollection: FriendCollection
    )

    private var collections = listOf<CollectionGroup>()

    @BeforeEach
    fun reset() {
        val memUserCollection = MemoryUserCollection()
        val memFriendCollection = MemoryFriendCollection(memUserCollection)
        collections = listOf(CollectionGroup(memUserCollection, memFriendCollection))
    }

    @TestFactory
    fun addExistingUser(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.friendCollection::class.java.toString(), {
            val userOne = collections.userCollection.createUser("name", "1234", "google")
            val userTwo = collections.userCollection.createUser("name", "4321", "google")

            assertEquals(false, collections.friendCollection.areFriends(userOne.id, userTwo.id))
            assertEquals(false, collections.friendCollection.areFriends(userTwo.id, userOne.id))
            assert(collections.friendCollection.getFriends(userOne.id).isEmpty())
            assert(collections.friendCollection.getFriends(userTwo.id).isEmpty())
            assert(collections.friendCollection.getSentRequests(userOne.id).isEmpty())
            assert(collections.friendCollection.getSentRequests(userTwo.id).isEmpty())
            assert(collections.friendCollection.getReceivedRequests(userOne.id).isEmpty())
            assert(collections.friendCollection.getReceivedRequests(userTwo.id).isEmpty())

            collections.friendCollection.addFriend(userOne.id, userTwo.id)

            assertEquals(false, collections.friendCollection.areFriends(userOne.id, userTwo.id))
            assertEquals(false, collections.friendCollection.areFriends(userTwo.id, userOne.id))
            assert(collections.friendCollection.getFriends(userOne.id).isEmpty())
            assert(collections.friendCollection.getFriends(userTwo.id).isEmpty())
            assertEquals(1, collections.friendCollection.getSentRequests(userOne.id).size)
            assert(usersAreEqual(collections.friendCollection.getSentRequests(userOne.id)[0], userTwo))
            assert(collections.friendCollection.getSentRequests(userTwo.id).isEmpty())
            assert(collections.friendCollection.getReceivedRequests(userOne.id).isEmpty())
            assertEquals(1, collections.friendCollection.getReceivedRequests(userTwo.id).size)
            assert(usersAreEqual(collections.friendCollection.getReceivedRequests(userTwo.id)[0], userOne))

            collections.friendCollection.addFriend(userTwo.id, userOne.id)

            assertEquals(true, collections.friendCollection.areFriends(userOne.id, userTwo.id))
            assertEquals(true, collections.friendCollection.areFriends(userTwo.id, userOne.id))
            assertEquals(1, collections.friendCollection.getFriends(userOne.id).size)
            assert(usersAreEqual(collections.friendCollection.getFriends(userOne.id)[0], userTwo))
            assertEquals(1, collections.friendCollection.getFriends(userTwo.id).size)
            assert(usersAreEqual(collections.friendCollection.getFriends(userTwo.id)[0], userOne))
            assert(collections.friendCollection.getSentRequests(userOne.id).isEmpty())
            assert(collections.friendCollection.getSentRequests(userTwo.id).isEmpty())
            assert(collections.friendCollection.getReceivedRequests(userOne.id).isEmpty())
            assert(collections.friendCollection.getReceivedRequests(userTwo.id).isEmpty())
        })}
    }

    @TestFactory
    fun addSelfAsFriend(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.friendCollection::class.java.toString(), {
            val userOne = collections.userCollection.createUser("name", "1234", "google")
            val e = assertThrows(Exception::class.java) { collections.friendCollection.addFriend(userOne.id, userOne.id) }
            assertEquals("Cannot add yourself as a friend", e.message)
        })}
    }

    @TestFactory
    fun sendDuplicateFriendRequest(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.friendCollection::class.java.toString(), {
            val userOne = collections.userCollection.createUser("name", "1234", "google")
            val userTwo = collections.userCollection.createUser("name", "4321", "google")

            collections.friendCollection.addFriend(userOne.id, userTwo.id)
            val e = assertThrows(Exception::class.java) { collections.friendCollection.addFriend(userOne.id, userTwo.id) }
            assertEquals("Cannot add someone you have already added", e.message)
        })}
    }

    @TestFactory
    fun addExistingFriendAgain(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.friendCollection::class.java.toString(), {
            val userOne = collections.userCollection.createUser("name", "1234", "google")
            val userTwo = collections.userCollection.createUser("name", "4321", "google")

            collections.friendCollection.addFriend(userOne.id, userTwo.id)
            collections.friendCollection.addFriend(userTwo.id, userOne.id)
            val e = assertThrows(Exception::class.java) { collections.friendCollection.addFriend(userOne.id, userTwo.id) }
            assertEquals("Cannot add someone you have already added", e.message)
        })}
    }

    @TestFactory
    fun removeExistingFriend(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.friendCollection::class.java.toString(), {
            val userOne = collections.userCollection.createUser("name", "1234", "google")
            val userTwo = collections.userCollection.createUser("name", "4321", "google")

            collections.friendCollection.addFriend(userOne.id, userTwo.id)
            collections.friendCollection.addFriend(userTwo.id, userOne.id)
            collections.friendCollection.removeFriend(userOne.id, userTwo.id)

            assertEquals(collections.friendCollection.getFriends(userOne.id).size, 0)
            assertEquals(collections.friendCollection.getFriends(userTwo.id).size, 0)
            assertEquals(collections.friendCollection.getSentRequests(userOne.id).size, 0)
            assertEquals(collections.friendCollection.getSentRequests(userTwo.id).size, 0)
            assertEquals(collections.friendCollection.getReceivedRequests(userOne.id).size, 0)
            assertEquals(collections.friendCollection.getReceivedRequests(userTwo.id).size, 0)
        })}
    }

    @TestFactory
    fun removeNonExistingFriend(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.friendCollection::class.java.toString(), {
            val userOne = collections.userCollection.createUser("name", "1234", "google")
            val userTwo = collections.userCollection.createUser("name", "4321", "google")

            val e = assertThrows(Exception::class.java) { collections.friendCollection.removeFriend(userOne.id, userTwo.id) }
            assertEquals("Users are not friends", e.message)
        })}
    }
}