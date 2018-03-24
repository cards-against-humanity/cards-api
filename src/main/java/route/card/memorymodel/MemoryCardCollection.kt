package route.card.memorymodel

import route.card.model.CardCollection
import route.card.model.CardModel
import route.card.model.CardpackModel
import route.user.model.UserCollection

class MemoryCardCollection(private val userCollection: UserCollection) : CardCollection {
    private var cardpackId = 0
    private var cardId = 0
    private val cardpacks: MutableMap<String, MutableList<CardpackModel>> = HashMap()
    private val cards: MutableMap<String, MutableList<CardModel>> = HashMap()

    override fun createCardpack(name: String, userId: String): CardpackModel {
        userCollection.getUser(userId)
        if (cardpacks[userId] == null) {
            cardpacks[userId] = ArrayList()
        }
        val cardpack = MemoryCardpackModel(this.cardpackId.toString(), name, userId, cards)
        cardpacks[userId]!!.add(cardpack)
        this.cardpackId++
        return cardpack
    }

    override fun createCard(text: String, cardpackId: String): CardModel {
        this.getCardpack(cardpackId)
        if (cards[cardpackId] == null) {
            cards[cardpackId] = ArrayList()
        }
        val card = MemoryCardModel(this.cardId.toString(), text, cardpackId)
        cards[cardpackId]!!.add(card)
        this.cardId++
        return card
    }

    override fun createCards(textList: List<String>, cardpackId: String): List<CardModel> {
        return textList.map { text -> this.createCard(text, cardpackId) }
    }

    override fun deleteCardpack(cardpackId: String) {
        if (cardpacks[cardpackId] == null) {
            throw Exception("Cardpack does not exist with id: $cardpackId")
        }
        cardpacks.remove(cardpackId)
        cards.remove(cardpackId)
    }

    override fun deleteCard(cardId: String) {
        for (entry in cards) {
            val cardList = entry.value
            for (card in cardList) {
                if (card.id == cardId) {
                    cardList.remove(card)
                    return
                }
            }
        }
        throw Exception("Card does not exist with id: $cardId")
    }

    override fun deleteCards(ids: List<String>) {
        ids.forEach { id -> this.getCardpack(this.getCard(id).cardpackId) }
        ids.forEach { id -> this.deleteCard(id) }
    }

    override fun getCardpack(cardpackId: String): CardpackModel {
        for (entry in cardpacks) {
            val cardpackList = entry.value
            for (cardpack in cardpackList) {
                if (cardpack.id == cardpackId) {
                    return cardpack
                }
            }
        }
        throw Exception("Cardpack does not exist with id: $cardpackId")
    }

    override fun getCardpacks(userId: String): List<CardpackModel> {
        userCollection.getUser(userId)
        return cardpacks[userId] ?: ArrayList()
    }

    override fun getCard(cardId: String): CardModel {
        for (entry in cards) {
            val cardList = entry.value
            for (card in cardList) {
                if (card.id == cardId) {
                    return card
                }
            }
        }
        throw Exception("Card does not exist with id: $cardId")
    }

    private class MemoryCardpackModel(override val id: String, override var name: String, override val ownerId: String, private val cardsByCardpack: Map<String, MutableList<CardModel>>) : CardpackModel {
        override fun setName(name: String): CardpackModel {
            this.name = name
            return this
        }

        override fun getCards(): List<CardModel> {
            return cardsByCardpack[id] ?: ArrayList()
        }
    }

    private class MemoryCardModel(override val id: String, override var text: String, override val cardpackId: String) : CardModel {
        override fun setText(text: String): CardModel {
            this.text = text
            return this
        }
    }
}