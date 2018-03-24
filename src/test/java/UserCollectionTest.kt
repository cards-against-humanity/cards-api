import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import database.memorymodel.MemoryUserCollection
import route.user.model.UserCollection
import kotlin.test.assertEquals

class UserCollectionTest {
    private var collections = listOf<UserCollection>()

    @BeforeEach
    fun reset() {
        collections = listOf(MemoryUserCollection())
    }

    @TestFactory
    fun createUser(): List<DynamicTest> {
        return collections.map { userCollection -> DynamicTest.dynamicTest(userCollection::class.java.toString(), {
            val user = userCollection.createUser("test_name", "1234", "google")
            assertEquals("test_name", user.name)
            assertEquals("1234", user.oAuthId)
            assertEquals("google", user.oAuthProvider)
        })}
    }

    @TestFactory
    fun createDuplicateUser(): List<DynamicTest> {
        return collections.map { userCollection -> DynamicTest.dynamicTest(userCollection::class.java.toString(), {
            userCollection.createUser("name", "1234", "google")
            val e = assertThrows(Exception::class.java) { userCollection.createUser("name", "1234", "google") }
            assertEquals("User already exists with that oAuth ID and provider", e.message)
        })}
    }

    @TestFactory
    fun getExistingUserById(): List<DynamicTest> {
        return collections.map { userCollection -> DynamicTest.dynamicTest(userCollection::class.java.toString(), {
            val userOne = userCollection.createUser("name", "1234", "google")
            val userTwo = userCollection.getUser(userOne.id)
            assert(usersAreEqual(userOne, userTwo))
        })}
    }

    @TestFactory
    fun getNonExistingUserById(): List<DynamicTest> {
        return collections.map { userCollection -> DynamicTest.dynamicTest(userCollection::class.java.toString(), {
            val id = "fake_user_id"
            val e = assertThrows(Exception::class.java) { userCollection.getUser(id) }
            assertEquals("User does not exist with id: $id", e.message)
        })}
    }

    @TestFactory
    fun getExistingUserByOAuth(): List<DynamicTest> {
        return collections.map { userCollection -> DynamicTest.dynamicTest(userCollection::class.java.toString(), {
            val userOne = userCollection.createUser("name", "1234", "google")
            val userTwo = userCollection.getUser(userOne.oAuthId, userOne.oAuthProvider)
            assert(usersAreEqual(userOne, userTwo))
        })}
    }

    @TestFactory
    fun getNonExistingUserByOAuth(): List<DynamicTest> {
        return collections.map { userCollection -> DynamicTest.dynamicTest(userCollection::class.java.toString(), {
            val oAuthId = "fake_oauth_id"
            val oAuthProvider = "fake_oauth_provider"
            val e = assertThrows(Exception::class.java) { userCollection.getUser(oAuthId, oAuthProvider) }
            assertEquals("User does not exist with oAuthId of $oAuthId and oAuthProvider of $oAuthProvider", e.message)
        })}
    }

    @TestFactory
    fun setUserName(): List<DynamicTest> {
        return collections.map { userCollection -> DynamicTest.dynamicTest(userCollection::class.java.toString(), {
            val userOne = userCollection.createUser("name", "1234", "google")
            userOne.setName("different_name")
            val userTwo = userCollection.getUser(userOne.id)
            assertEquals("different_name", userTwo.name)
        })}
    }

    @TestFactory
    fun getUsersByIdList(): List<DynamicTest> {
        return collections.map { userCollection -> DynamicTest.dynamicTest(userCollection::class.java.toString(), {
            val userIds = ArrayList<String>()
            for (i in 0..100) {
                val user = userCollection.createUser("name$i", i.toString(), "google")
                userIds.add(user.id)
            }
            val users = userCollection.getUsers(userIds)
            users.forEachIndexed { i, userModel ->
                assertEquals("name$i", userModel.name)
                assertEquals(i.toString(), userModel.oAuthId)
            }
        })}
    }
}