import com.mongodb.MongoClient
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import route.card.Card
import route.card.Cardpack
import route.user.User

class CardTest {
    private var userOne: User? = null
    private var userTwo: User? = null
    private var cardpackOne: Cardpack? = null
    private var cardOne: Card? = null

    companion object {
        @BeforeAll @JvmStatic
        fun initialize() {
            database.Instance.mongo = MongoClient("localhost").getDatabase("appNameTest")
        }
    }

    @BeforeEach
    fun reset() {
        database.Instance.resetMongo()
        userOne = User.create("4321", "google", "Quinn")
        userTwo = User.create("1234", "google", "Charlie")
        cardpackOne = Cardpack.create("cardpackOne", userOne!!)
        cardOne = Card.create("card", cardpackOne!!)
    }

    @Test
    fun equals() {
        assert(!cardOne!!.equals(null))
        assert(cardOne!!.equals(Card.get(ObjectId(cardOne!!.id))))
        assert(!cardOne!!.equals(Card.create("foo", cardpackOne!!)))
        assert(!cardOne!!.equals(Object()))
    }

    @Test
    fun setText() {
        val newText = "new card text"
        cardOne!!.setText(newText)
        val cardTwo = Card.get(ObjectId(cardOne!!.id))
        assert(cardOne!!.text == newText)
        assert(cardTwo.text == newText)
    }
}
