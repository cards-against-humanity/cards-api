import com.fasterxml.jackson.databind.ObjectMapper
import com.mongodb.MongoClient
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import route.user.User
import java.util.*

class UserEndpointTest {
//    private var client = OkHttpClient()
//    private var userOne: User? = null
//    private var userTwo: User? = null
//
//    companion object {
//        @BeforeAll
//        @JvmStatic
//        fun initialize() {
//            database.Instance.mongo = MongoClient("localhost").getDatabase("appNameTest")
//            server.Main.main(arrayOf<String>())
//            waitForServer()
//        }
//
//        private fun waitForServer() {
//            while(true) {
//                try {
//                    val request = Request.Builder().url("http://localhost:8080").build()
//                    OkHttpClient().newCall(request).execute()
//                    break
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//                Thread.yield()
//            }
//        }
//    }
//
//    @BeforeEach
//    fun reset() {
//        database.Instance.resetMongo()
//        userOne = User.create("4321", "google", "Quinn")
//        userTwo = User.create("1234", "google", "Charlie")
//    }
//
//    @Test
//    fun getUserById() {
//        val request = Request.Builder()
//                .url("http://localhost:8080/user/" + userOne!!.id)
//                .build()
//
//        val response = client.newCall(request).execute()
//        val result = ObjectMapper().readValue(response.body()!!.string(), HashMap::class.java)
//        println(result)
//        assert(result["id"] == userOne!!.id)
//        assert(result["name"] == userOne!!.name)
//        assert(result["oAuthId"] == userOne!!.oAuthId)
//        assert(result["oAuthProvider"] == userOne!!.oAuthProvider)
//        assert(result.size == 4)
//    }
}
