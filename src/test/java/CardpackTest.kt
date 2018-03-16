import com.mongodb.MongoClient
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import route.card.Card
import route.card.Cardpack
import route.user.User

class CardpackTest {
    private var userOne: User? = null
    private var userTwo: User? = null

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
    }

    @Test
    fun create() {
        val cardpackName = "testCardpack"
        val pack1 = Cardpack.create(cardpackName, userOne!!)
        val pack2 = Cardpack.get(ObjectId(pack1.id))
        assert(pack1.name == cardpackName)
        assert(pack2.name == cardpackName)
    }

    @Test
    fun equals() {
        val pack = Cardpack.create("pack", userOne!!)
        assert(pack.equals(Cardpack.get(ObjectId(pack.id))))
        assert(!pack.equals(null))
        assert(!pack.equals(Object()))
    }

    @Test
    fun getById() {
        val cardpack = Cardpack.create("cardpack", userOne!!)
        assert(cardpack == Cardpack.get(ObjectId(cardpack.id)))
    }

    @Test
    fun getByUser() {
        assert(Cardpack.get(userOne!!).isEmpty())
        assert(Cardpack.get(userTwo!!).isEmpty())

        Cardpack.create("pack", userOne!!)

        assert(Cardpack.get(userOne!!).size == 1)
        assert(Cardpack.get(userTwo!!).isEmpty())
        assert(Cardpack.get(userOne!!)[0].ownerId == userOne!!.id)
    }

    @Test
    fun getCards() {
        var cardpack: Cardpack
        var length: Int
        var cards: List<Card>

        cardpack = Cardpack.create("cardpack", userOne!!)
        length = 100
        for (i in 0 until length) {
            Card.create("card" + i, cardpack)
        }
        cards = cardpack.getCards()
        assert(cards.size == length)
        for (i in 0 until length) {
            assert(cards[i].text == "card" + i)
            assert(cards[i].cardpackId == cardpack.id)
        }

        cardpack = Cardpack.create("cardpack", userTwo!!)
        length = 50
        for (i in 0 until length) {
            Card.create("card" + i, cardpack)
        }
        cards = cardpack.getCards()
        assert(cards.size == length)
        for (i in 0 until length) {
            assert(cards[i].text == "card" + i)
            assert(cards[i].cardpackId == cardpack.id)
        }
    }

    @Test
    fun setName() {
        val cardpack1 = Cardpack.create("cardpack", userOne!!)
        val name = "hello world"
        cardpack1.setName(name)
        val cardpack2 = Cardpack.get(ObjectId(cardpack1.id))
        assert(cardpack1.name == name)
        assert(cardpack2.name == name)
    }

    @Test
    fun delete() {
        val cardpack = Cardpack.create("cardpack", userOne!!)
        Cardpack.get(ObjectId(cardpack.id))
        Cardpack.delete(ObjectId(cardpack.id))
        assertThrows(Exception::class.java) { Cardpack.get(ObjectId(cardpack.id)) }

        assertThrows(Exception::class.java) { Cardpack.delete(ObjectId(cardpack.id)) }
    }
}
