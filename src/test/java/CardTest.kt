import com.mongodb.MongoClient
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import route.card.Card
import route.card.Cardpack
import route.user.memorymodel.MemoryUserCollection

class CardTest {
    private val userCollection = MemoryUserCollection()
    private var userOne = userCollection.createUser("Quinn", "4321", "google")
    private var userTwo = userCollection.createUser("Charlie", "1234", "google")
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
        userCollection.clear()
        userOne = userCollection.createUser("Quinn", "4321", "google")
        userTwo = userCollection.createUser("Charlie", "1234", "google")
        cardpackOne = Cardpack.create("cardpackOne", userOne)
        cardOne = Card.create("card", cardpackOne!!)
    }

    @Test
    fun create() {
        val text = "card_text"
        val card = Card.create(text, cardpackOne!!)
        assert(card.text == text)
        assert(card.cardpackId == cardpackOne!!.id)
    }

    @Test
    fun createMany() {
        val cardpack = Cardpack.create("cardpack", userOne)
        val strings: MutableList<String> = ArrayList()
        for (i in 0..99) {
            strings.add(i.toString())
        }
        Card.create(strings, cardpack)
        val cards = cardpack.getCards()
        assert(cards.size == 100)
        cards.forEachIndexed{ index, card ->
            run {
                assert(card.text == index.toString())
            }
        }
    }

    @Test
    fun equals() {
        assert(!cardOne!!.equals(null))
        assert(cardOne!!.equals(Card.get(ObjectId(cardOne!!.id))))
        assert(!cardOne!!.equals(Card.create("foo", cardpackOne!!)))
        assert(!cardOne!!.equals(Object()))
    }

    @Test
    fun getById() {
        assert(Card.get(ObjectId(cardOne!!.id)) == cardOne)
    }

    @Test
    fun setText() {
        val newText = "new card text"
        cardOne!!.setText(newText)
        val cardTwo = Card.get(ObjectId(cardOne!!.id))
        assert(cardOne!!.text == newText)
        assert(cardTwo.text == newText)
    }

    @Test
    fun delete() {
        Card.get(ObjectId(cardOne!!.id))
        Card.delete(ObjectId(cardOne!!.id))
        Assertions.assertThrows(Exception::class.java) { Card.get(ObjectId(cardOne!!.id)) }

        Assertions.assertThrows(Exception::class.java) { Card.delete(ObjectId(cardOne!!.id)) }
    }
}
