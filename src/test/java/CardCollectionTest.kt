import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import database.memorymodel.MemoryCardCollection
import database.memorymodel.MemoryUserCollection
import database.mongomodel.MongoCardCollection
import database.mongomodel.MongoUserCollection
import route.card.JsonBlackCard
import route.card.JsonWhiteCard
import route.card.model.CardCollection
import route.user.model.UserCollection

class CardCollectionTest {
    private data class CollectionGroup(
            val userCollection: UserCollection,
            val cardCollection: CardCollection
    )

    private var collections = listOf<CollectionGroup>()

    @BeforeEach
    fun reset() {
        resetTestMongo()

        val memUserCollection = MemoryUserCollection()
        val memCardCollection = MemoryCardCollection(memUserCollection)

        val mongoUserCollection = MongoUserCollection(getTestMongoCollection("users"))
        val mongoCardCollection = MongoCardCollection(getTestMongoCollection("cardpacks"), getTestMongoCollection("whitecards"), getTestMongoCollection("blackcards"), mongoUserCollection)
        collections = listOf(CollectionGroup(memUserCollection, memCardCollection), CollectionGroup(mongoUserCollection, mongoCardCollection))
    }

    @TestFactory
    fun createCardpackForExistingUser(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
            val user = collections.userCollection.createUser("user", "1234", "google")
            val cardpack = collections.cardCollection.createCardpack("cardpack_name", user.id)
            assertCardpackEquals(collections.cardCollection.getCardpack(cardpack.id), cardpack)
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
    fun createWhiteCardForExistingCardpack(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
            val user = collections.userCollection.createUser("user", "1234", "google")
            val cardpack = collections.cardCollection.createCardpack("cardpack_name", user.id)
            assertEquals(0, collections.cardCollection.getCardpack(cardpack.id).whiteCards.size)
            val card = collections.cardCollection.createWhiteCard(JsonWhiteCard("card_text"), cardpack.id)
            assertEquals(1, collections.cardCollection.getCardpack(cardpack.id).whiteCards.size)
            assertEquals("card_text", collections.cardCollection.getCardpack(cardpack.id).whiteCards[0].text)
        })}
    }

    @TestFactory
    fun createWhiteCardForNonExistingCardpack(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
            val e = assertThrows(Exception::class.java) { collections.cardCollection.createWhiteCard(JsonWhiteCard("card_text"), "fake_cardpack_id") }
            assertEquals("Cardpack does not exist with id: fake_cardpack_id", e.message)
        })}
    }

    @TestFactory
    fun createBlackCardForExistingCardpack(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
            val user = collections.userCollection.createUser("user", "1234", "google")
            val cardpack = collections.cardCollection.createCardpack("cardpack_name", user.id)
            assertEquals(0, collections.cardCollection.getCardpack(cardpack.id).blackCards.size)
            val card = collections.cardCollection.createBlackCard(JsonBlackCard("card_text", 4), cardpack.id)
            assertEquals(1, collections.cardCollection.getCardpack(cardpack.id).blackCards.size)
            assertEquals("card_text", collections.cardCollection.getCardpack(cardpack.id).blackCards[0].text)
        })}
    }

    @TestFactory
    fun createBlackCardForNonExistingCardpack(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
            val e = assertThrows(Exception::class.java) { collections.cardCollection.createBlackCard(JsonBlackCard("card_text", 4), "fake_cardpack_id") }
            assertEquals("Cardpack does not exist with id: fake_cardpack_id", e.message)
        })}
    }

    @TestFactory
    fun createWhiteCardsForExistingCardpack(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
            val user = collections.userCollection.createUser("user", "1234", "google")
            var cardpack = collections.cardCollection.createCardpack("cardpack_name", user.id)
            assertEquals(0, cardpack.whiteCards.size)
            assertEquals(0, collections.cardCollection.getCardpack(cardpack.id).whiteCards.size)
            val cards = collections.cardCollection.createWhiteCards(listOf(JsonWhiteCard("card_0"), JsonWhiteCard("card_1"), JsonWhiteCard("card_2")), cardpack.id)

            cardpack = collections.cardCollection.getCardpack(cardpack.id)
            assertEquals(3, cardpack.whiteCards.size)
            assertEquals("card_0", cardpack.whiteCards[0].text)
            assertEquals("card_1", cardpack.whiteCards[1].text)
            assertEquals("card_2", cardpack.whiteCards[2].text)
        })}
    }

    @TestFactory
    fun createBlackCardsForExistingCardpack(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
            val user = collections.userCollection.createUser("user", "1234", "google")
            var cardpack = collections.cardCollection.createCardpack("cardpack_name", user.id)
            assertEquals(0, cardpack.blackCards.size)
            assertEquals(0, collections.cardCollection.getCardpack(cardpack.id).blackCards.size)
            val cards = collections.cardCollection.createBlackCards(listOf(JsonBlackCard("card_0", 1), JsonBlackCard("card_1", 2), JsonBlackCard("card_2", 3)), cardpack.id)

            cardpack = collections.cardCollection.getCardpack(cardpack.id)
            assertEquals(3, cardpack.blackCards.size)
            assertEquals("card_0", cardpack.blackCards[0].text)
            assertEquals("card_1", cardpack.blackCards[1].text)
            assertEquals("card_2", cardpack.blackCards[2].text)
        })}
    }

    @TestFactory
    fun createWhiteCardsForNonExistingCardpack(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
            val e = assertThrows(Exception::class.java) { collections.cardCollection.createWhiteCards(listOf(JsonWhiteCard("card_0"), JsonWhiteCard("card_1"), JsonWhiteCard("card_2")), "fake_cardpack_id") }
            assertEquals("Cardpack does not exist with id: fake_cardpack_id", e.message)
        })}
    }

    @TestFactory
    fun createBlackCardsForNonExistingCardpack(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
            val e = assertThrows(Exception::class.java) { collections.cardCollection.createBlackCards(listOf(JsonBlackCard("card_0", 1), JsonBlackCard("card_1", 1), JsonBlackCard("card_2", 1)), "fake_cardpack_id") }
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

//    @TestFactory
//    fun removeExistingWhiteCardsWhenDeletingCardpack(): List<DynamicTest> {
//        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
//            val user = collections.userCollection.createUser("user", "1234", "google")
//            val cardpack = collections.cardCollection.createCardpack("cardpack_name", user.id)
//            val card = collections.cardCollection.createWhiteCard(JsonWhiteCard("card_text", cardpack.id))
//            collections.cardCollection.deleteCardpack(cardpack.id)
//            val e = assertThrows(Exception::class.java) { collections.cardCollection.getCard(card.id) }
//            assertEquals("Card does not exist with id: ${card.id}", e.message)
//        })}
//    }
//
//    @TestFactory
//    fun removeExistingBlackCardsWhenDeletingCardpack(): List<DynamicTest> {
//        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
//            val user = collections.userCollection.createUser("user", "1234", "google")
//            val cardpack = collections.cardCollection.createCardpack("cardpack_name", user.id)
//            val card = collections.cardCollection.createBlackCard(JsonBlackCard("card_text", 2, cardpack.id))
//            collections.cardCollection.deleteCardpack(cardpack.id)
//            val e = assertThrows(Exception::class.java) { collections.cardCollection.getCard(card.id) }
//            assertEquals("Card does not exist with id: ${card.id}", e.message)
//        })}
//    }

    @TestFactory
    fun deleteExistingWhiteCard(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
            val user = collections.userCollection.createUser("user", "1234", "google")
            val cardpack = collections.cardCollection.createCardpack("cardpack_name", user.id)
            val card = collections.cardCollection.createWhiteCard(JsonWhiteCard("card_text"), cardpack.id)
            collections.cardCollection.deleteWhiteCard(card.id)
            assertEquals(0, collections.cardCollection.getCardpack(cardpack.id).whiteCards.size)
            assertEquals(0, cardpack.whiteCards.size)
            val e = assertThrows(Exception::class.java) { collections.cardCollection.deleteWhiteCard(card.id) }
            assertEquals("Card does not exist with id: ${card.id}", e.message)
        })}
    }

    @TestFactory
    fun deleteNonExistingWhiteCard(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
            val e = assertThrows(Exception::class.java) { collections.cardCollection.deleteWhiteCard("fake_card_id") }
            assertEquals("Card does not exist with id: fake_card_id", e.message)
        })}
    }

    @TestFactory
    fun deleteExistingBlackCard(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
            val user = collections.userCollection.createUser("user", "1234", "google")
            val cardpack = collections.cardCollection.createCardpack("cardpack_name", user.id)
            val card = collections.cardCollection.createBlackCard(JsonBlackCard("card_text", 1), cardpack.id)
            collections.cardCollection.deleteBlackCard(card.id)
            assertEquals(0, collections.cardCollection.getCardpack(cardpack.id).blackCards.size)
            assertEquals(0, cardpack.blackCards.size)
            val e = assertThrows(Exception::class.java) { collections.cardCollection.deleteBlackCard(card.id) }
            assertEquals("Card does not exist with id: ${card.id}", e.message)
        })}
    }

    @TestFactory
    fun deleteNonExistingBlackCard(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
            val e = assertThrows(Exception::class.java) { collections.cardCollection.deleteBlackCard("fake_card_id") }
            assertEquals("Card does not exist with id: fake_card_id", e.message)
        })}
    }

    @TestFactory
    fun deleteExistingWhiteCards(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
            val user = collections.userCollection.createUser("user", "1234", "google")
            val cardpack = collections.cardCollection.createCardpack("cardpack_name", user.id)
            val cards = collections.cardCollection.createWhiteCards(listOf(JsonWhiteCard("card_0"), JsonWhiteCard("card_1"), JsonWhiteCard("card_2")), cardpack.id)
            collections.cardCollection.deleteWhiteCards(cards.map { card -> card.id })
            assertEquals(0, collections.cardCollection.getCardpack(cardpack.id).whiteCards.size)
            assertEquals(0, cardpack.whiteCards.size)
        })}
    }

    @TestFactory
    fun deleteExistingBlackCards(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
            val user = collections.userCollection.createUser("user", "1234", "google")
            val cardpack = collections.cardCollection.createCardpack("cardpack_name", user.id)
            val cards = collections.cardCollection.createBlackCards(listOf(JsonBlackCard("card_0", 1), JsonBlackCard("card_1", 1), JsonBlackCard("card_2", 1)), cardpack.id)
            collections.cardCollection.deleteBlackCards(cards.map { card -> card.id })
            assertEquals(0, collections.cardCollection.getCardpack(cardpack.id).blackCards.size)
            assertEquals(0, cardpack.blackCards.size)
        })}
    }

    @TestFactory
    fun deleteNonExistingWhiteCards(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
            val e = assertThrows(Exception::class.java) { collections.cardCollection.deleteWhiteCards(listOf("fake", "id")) }
            assertEquals("One or more card ids is invalid", e.message)
        })}
    }

    @TestFactory
    fun deleteNonExistingBlackCards(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
            val e = assertThrows(Exception::class.java) { collections.cardCollection.deleteBlackCards(listOf("fake", "id")) }
            assertEquals("One or more card ids is invalid", e.message)
        })}
    }

    @TestFactory
    fun createWhiteCardsAtomicity(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
            // TODO - Implement
        })}
    }

    @TestFactory
    fun createBlackCardsAtomicity(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
            // TODO - Implement
        })}
    }

    @TestFactory
    fun deleteExistingWhiteCardsAtomicity(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
            val user = collections.userCollection.createUser("user", "1234", "google")
            val cardpack = collections.cardCollection.createCardpack("cardpack_name", user.id)
            val cards = collections.cardCollection.createWhiteCards(listOf(JsonWhiteCard("card_0"), JsonWhiteCard("card_1"), JsonWhiteCard("card_2")), cardpack.id)
            val e = assertThrows(Exception::class.java) { collections.cardCollection.deleteWhiteCards(listOf(cards[0].id, "fake_id", cards[1].id, cards[2].id)) }
            assertEquals("One or more card ids is invalid", e.message)
            assertEquals(3, collections.cardCollection.getCardpack(cardpack.id).whiteCards.size)
        })}
    }

    @TestFactory
    fun deleteExistingBlackCardsAtomicity(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
            val user = collections.userCollection.createUser("user", "1234", "google")
            val cardpack = collections.cardCollection.createCardpack("cardpack_name", user.id)
            val cards = collections.cardCollection.createBlackCards(listOf(JsonBlackCard("card_0", 1), JsonBlackCard("card_1", 1), JsonBlackCard("card_2", 1)), cardpack.id)
            val e = assertThrows(Exception::class.java) { collections.cardCollection.deleteBlackCards(listOf(cards[0].id, "fake_id", cards[1].id, cards[2].id)) }
            assertEquals("One or more card ids is invalid", e.message)
            assertEquals(3, collections.cardCollection.getCardpack(cardpack.id).blackCards.size)
        })}
    }

    @TestFactory
    fun getExistingCardpack(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
            val user = collections.userCollection.createUser("user", "1234", "google")
            var cardpack = collections.cardCollection.createCardpack("cardpack_name", user.id)
            collections.cardCollection.createWhiteCard(JsonWhiteCard("white_card_text"), cardpack.id)
            collections.cardCollection.createBlackCard(JsonBlackCard("black_card_text", 1), cardpack.id)
            cardpack = collections.cardCollection.getCardpack(cardpack.id)
            assertEquals(1, cardpack.whiteCards.size)
            assertEquals(1, cardpack.blackCards.size)
            assertEquals("white_card_text", cardpack.whiteCards[0].text)
            assertEquals("black_card_text", cardpack.blackCards[0].text)
            assertEquals(cardpack.id, cardpack.id)
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
            collections.cardCollection.createWhiteCard(JsonWhiteCard("white_card_text"), cardpackOne.id)
            collections.cardCollection.createBlackCard(JsonBlackCard("black_card_text", 1), cardpackOne.id)

            val userOneCardpacks = collections.cardCollection.getCardpacks(userOne.id)
            assertEquals(cardpackOne.id, userOneCardpacks[0].id)
            assertEquals(cardpackThree.id, userOneCardpacks[1].id)
            assertEquals(1, userOneCardpacks[0].whiteCards.size)
            assertEquals(1, userOneCardpacks[0].blackCards.size)
            assertEquals("white_card_text", userOneCardpacks[0].whiteCards[0].text)
            assertEquals("black_card_text", userOneCardpacks[0].blackCards[0].text)

            val userTwoCardpacks = collections.cardCollection.getCardpacks(userTwo.id)
            assertEquals(cardpackTwo.id, userTwoCardpacks[0].id)
            assertEquals(cardpackFour.id, userTwoCardpacks[1].id)
        })}
    }

    @TestFactory
    fun getNonExistingCardpacks(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
            val e = assertThrows(Exception::class.java) { collections.cardCollection.getCardpacks("fake_user_id") }
            assertEquals("User does not exist with id: fake_user_id", e.message)
        })}
    }

//    @TestFactory
//    fun getExistingCard(): List<DynamicTest> {
//        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
//            val user = collections.userCollection.createUser("user", "1234", "google")
//            val cardpack = collections.cardCollection.createCardpack("cardpack_name", user.id)
//            val card = collections.cardCollection.createCard("card_text", cardpack.id)
//            assert(cardEquals(card, collections.cardCollection.getCard(card.id)))
//        })}
//    }
//
//    @TestFactory
//    fun getNonExistingCard(): List<DynamicTest> {
//        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
//            val e = assertThrows(Exception::class.java) { collections.cardCollection.getCard("fake_card_id") }
//            assertEquals("Card does not exist with id: fake_card_id", e.message)
//        })}
//    }

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
    fun setWhiteCardText(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
            val user = collections.userCollection.createUser("user", "1234", "google")
            val cardpack = collections.cardCollection.createCardpack("cardpack_name", user.id)
            val card = collections.cardCollection.createWhiteCard(JsonWhiteCard("card_text"), cardpack.id)
            card.setText("new_card_text")
            assertEquals("new_card_text", card.text)
            assertEquals("new_card_text", collections.cardCollection.getCardpack(cardpack.id).whiteCards[0].text)
        })}
    }

    @TestFactory
    fun setBlackCardText(): List<DynamicTest> {
        return collections.map { collections -> DynamicTest.dynamicTest(collections.cardCollection::class.java.toString(), {
            val user = collections.userCollection.createUser("user", "1234", "google")
            val cardpack = collections.cardCollection.createCardpack("cardpack_name", user.id)
            val card = collections.cardCollection.createBlackCard(JsonBlackCard("card_text", 4), cardpack.id)
            card.setText("new_card_text")
            assertEquals("new_card_text", card.text)
            assertEquals("new_card_text", collections.cardCollection.getCardpack(cardpack.id).blackCards[0].text)
        })}
    }
}