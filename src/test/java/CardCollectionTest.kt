import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import route.card.memorymodel.MemoryCardCollection
import route.user.memorymodel.MemoryUserCollection
import route.user.model.UserCollection

class CardCollectionTest {
    private data class CollectionGroup(
            val userCollection: UserCollection,
            val cardCollection: MemoryCardCollection
    )

    private var collections = listOf<CollectionGroup>()

    @BeforeEach
    fun reset() {
        val memUserCollection = MemoryUserCollection()
        val memCardCollection = MemoryCardCollection(memUserCollection)
        collections = listOf(CollectionGroup(memUserCollection, memCardCollection))
    }

    @TestFactory
    fun createCardpackForExistingUser(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
            val user = collections.userCollection.createUser("user", "1234", "google")
            val cardpack = collections.cardCollection.createCardpack("cardpack_name", user.id)
            assert(cardpackEquals(collections.cardCollection.getCardpack(cardpack.id), cardpack))
        })}
    }

    @TestFactory
    fun createCardpackForNonExistingUser(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
            val e = assertThrows(Exception::class.java) { collections.cardCollection.createCardpack("cardpack_name", "fake_user_id") }
            assertEquals("User does not exist with id: fake_user_id", e.message)
        })}
    }

    @TestFactory
    fun createCardForExistingCardpack(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
            val user = collections.userCollection.createUser("user", "1234", "google")
            val cardpack = collections.cardCollection.createCardpack("cardpack_name", user.id)
            assertEquals(0, cardpack.getCards().size)
            val card = collections.cardCollection.createCard("card_text", cardpack.id)
            assertEquals(1, cardpack.getCards().size)
            assertEquals("card_text", cardpack.getCards()[0].text)
        })}
    }

    @TestFactory
    fun createCardForNonExistingCardpack(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
            val e = assertThrows(Exception::class.java) { collections.cardCollection.createCard("card_text", "fake_cardpack_id") }
            assertEquals("Cardpack does not exist with id: fake_cardpack_id", e.message)
        })}
    }

    @TestFactory
    fun createCardsForExistingCardpack(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
            val user = collections.userCollection.createUser("user", "1234", "google")
            val cardpack = collections.cardCollection.createCardpack("cardpack_name", user.id)
            assertEquals(0, cardpack.getCards().size)
            val cards = collections.cardCollection.createCards(listOf("card_0", "card_1", "card_2"), cardpack.id)
            assertEquals(3, cardpack.getCards().size)
            assertEquals("card_0", cardpack.getCards()[0].text)
            assertEquals("card_1", cardpack.getCards()[1].text)
            assertEquals("card_2", cardpack.getCards()[2].text)
        })}
    }

    @TestFactory
    fun createCardsForNonExistingCardpack(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
            val e = assertThrows(Exception::class.java) { collections.cardCollection.createCards(listOf("card_0", "card_1", "card_2"), "fake_cardpack_id") }
            assertEquals("Cardpack does not exist with id: fake_cardpack_id", e.message)
        })}
    }

    @TestFactory
    fun deleteExistingCardpack(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
            val user = collections.userCollection.createUser("user", "1234", "google")
            val cardpack = collections.cardCollection.createCardpack("cardpack_name", user.id)
            assertEquals(1, collections.cardCollection.getCardpacks(user.id).size)
            collections.cardCollection.deleteCardpack(cardpack.id)
            assertEquals(0, collections.cardCollection.getCardpacks(user.id).size)
        })}
    }

    @TestFactory
    fun deleteNonExistingCardpack(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
            val e = assertThrows(Exception::class.java) { collections.cardCollection.deleteCardpack("fake_cardpack_id") }
            assertEquals("Cardpack does not exist with id: fake_cardpack_id", e.message)
        })}
    }

    @TestFactory
    fun removeExistingCardsWhenDeletingCardpack(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
            val user = collections.userCollection.createUser("user", "1234", "google")
            val cardpack = collections.cardCollection.createCardpack("cardpack_name", user.id)
            val card = collections.cardCollection.createCard("card_text", cardpack.id)
            collections.cardCollection.deleteCardpack(cardpack.id)
            val e = assertThrows(Exception::class.java) { collections.cardCollection.getCard(card.id) }
            assertEquals("Card does not exist with id: ${card.id}", e.message)
        })}
    }

    @TestFactory
    fun deleteExistingCard(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
            val user = collections.userCollection.createUser("user", "1234", "google")
            val cardpack = collections.cardCollection.createCardpack("cardpack_name", user.id)
            val card = collections.cardCollection.createCard("card_text", cardpack.id)
            collections.cardCollection.deleteCard(card.id)
            assertEquals(0, cardpack.getCards().size)
            val e = assertThrows(Exception::class.java) { collections.cardCollection.deleteCard(card.id) }
            assertEquals("Card does not exist with id: ${card.id}", e.message)
        })}
    }

    @TestFactory
    fun deleteNonExistingCard(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
            val e = assertThrows(Exception::class.java) { collections.cardCollection.deleteCard("fake_card_id") }
            assertEquals("Card does not exist with id: fake_card_id", e.message)
        })}
    }

    @TestFactory
    fun deleteExistingCards(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
            val user = collections.userCollection.createUser("user", "1234", "google")
            val cardpack = collections.cardCollection.createCardpack("cardpack_name", user.id)
            val cards = collections.cardCollection.createCards(listOf("card_0", "card_1", "card_2"), cardpack.id)
            collections.cardCollection.deleteCards(cardpack.getCards().map { card -> card.id })
            assertEquals(0, cardpack.getCards().size)
        })}
    }

    @TestFactory
    fun deleteNonExistingCards(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
            val e = assertThrows(Exception::class.java) { collections.cardCollection.deleteCards(listOf("fake", "id")) }
            assertEquals("Card does not exist with id: fake", e.message)
        })}
    }

    @TestFactory
    fun deleteExistingCardsAtomicity(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
            val user = collections.userCollection.createUser("user", "1234", "google")
            val cardpack = collections.cardCollection.createCardpack("cardpack_name", user.id)
            val cards = collections.cardCollection.createCards(listOf("card_0", "card_1", "card_2"), cardpack.id)
            val e = assertThrows(Exception::class.java) { collections.cardCollection.deleteCards(listOf(cards[0].id, "fake_id", cards[1].id, cards[2].id)) }
            assertEquals("Card does not exist with id: fake_id", e.message)
            assertEquals(3, cardpack.getCards().size)
        })}
    }

    @TestFactory
    fun getExistingCardpack(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
            val user = collections.userCollection.createUser("user", "1234", "google")
            val cardpack = collections.cardCollection.createCardpack("cardpack_name", user.id)
            assert(cardpackEquals(cardpack, collections.cardCollection.getCardpack(cardpack.id)))
        })}
    }

    @TestFactory
    fun getNonExistingCardpack(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
            val e = assertThrows(Exception::class.java) { collections.cardCollection.getCardpack("fake_cardpack_id") }
            assertEquals("Cardpack does not exist with id: fake_cardpack_id", e.message)
        })}
    }

    @TestFactory
    fun getExistingCardpacks(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
            val userOne = collections.userCollection.createUser("user", "1234", "google")
            val userTwo = collections.userCollection.createUser("user", "4321", "google")
            val cardpackOne = collections.cardCollection.createCardpack("cardpack_name", userOne.id)
            val cardpackTwo = collections.cardCollection.createCardpack("cardpack_name", userTwo.id)
            val cardpackThree = collections.cardCollection.createCardpack("cardpack_name", userOne.id)
            val cardpackFour = collections.cardCollection.createCardpack("cardpack_name", userTwo.id)

            val userOneCardpacks = collections.cardCollection.getCardpacks(userOne.id)
            assert(cardpackEquals(cardpackOne, userOneCardpacks[0]))
            assert(cardpackEquals(cardpackThree, userOneCardpacks[1]))

            val userTwoCardpacks = collections.cardCollection.getCardpacks(userTwo.id)
            assert(cardpackEquals(cardpackTwo, userTwoCardpacks[0]))
            assert(cardpackEquals(cardpackFour, userTwoCardpacks[1]))
        })}
    }

    @TestFactory
    fun getNonExistingCardpacks(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
            val e = assertThrows(Exception::class.java) { collections.cardCollection.getCardpacks("fake_user_id") }
            assertEquals("User does not exist with id: fake_user_id", e.message)
        })}
    }

    @TestFactory
    fun getExistingCard(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
            val user = collections.userCollection.createUser("user", "1234", "google")
            val cardpack = collections.cardCollection.createCardpack("cardpack_name", user.id)
            val card = collections.cardCollection.createCard("card_text", cardpack.id)
            assert(cardEquals(card, collections.cardCollection.getCard(card.id)))
        })}
    }

    @TestFactory
    fun getNonExistingCard(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
            val e = assertThrows(Exception::class.java) { collections.cardCollection.getCard("fake_card_id") }
            assertEquals("Card does not exist with id: fake_card_id", e.message)
        })}
    }

    @TestFactory
    fun setCardpackName(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
            val user = collections.userCollection.createUser("user", "1234", "google")
            val cardpack = collections.cardCollection.createCardpack("cardpack_name", user.id)
            cardpack.setName("new_cardpack_name")
            assertEquals("new_cardpack_name", cardpack.name)
            assertEquals("new_cardpack_name", collections.cardCollection.getCardpack(cardpack.id).name)
        })}
    }

    @TestFactory
    fun setCardText(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
            val user = collections.userCollection.createUser("user", "1234", "google")
            val cardpack = collections.cardCollection.createCardpack("cardpack_name", user.id)
            val card = collections.cardCollection.createCard("card_text", cardpack.id)
            card.setText("new_card_text")
            assertEquals("new_card_text", card.text)
            assertEquals("new_card_text", collections.cardCollection.getCard(card.id).text)
        })}
    }
}