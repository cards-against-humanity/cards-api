package database.memorymodel

import route.card.JsonBlackCard
import route.card.JsonWhiteCard
import route.card.model.BlackCardModel
import route.card.model.CardCollection
import route.card.model.CardpackModel
import route.card.model.WhiteCardModel
import route.user.model.UserCollection
import route.user.model.UserModel

class MemoryCardCollection(private val userCollection: UserCollection) : CardCollection {
    private var cardpackId = 0
    private var whiteCardId = 0
    private var blackCardId = 0
    private val cardpacks: MutableMap<String, MutableList<CardpackModel>> = HashMap()

    override fun createCardpack(name: String, userId: String): CardpackModel {
        val user = userCollection.getUser(userId)
        if (cardpacks[userId] == null) {
            cardpacks[userId] = ArrayList()
        }
        val cardpack = MemoryCardpackModel(this.cardpackId.toString(), name, user)
        cardpacks[userId]!!.add(cardpack)
        this.cardpackId++
        return cardpack
    }

    override fun createWhiteCard(cardData: JsonWhiteCard, cardpackId: String): WhiteCardModel {
        val cardpack = this.getCardpack(cardpackId) as MemoryCardpackModel
        val card = MemoryWhiteCardModel(this.whiteCardId.toString(), cardData.text, cardpackId)
        cardpack.whiteCards.add(card)
        this.whiteCardId++
        return card
    }

    override fun createWhiteCards(cardDataList: List<JsonWhiteCard>, cardpackId: String): List<WhiteCardModel> {
        // TODO - Test for atomicity
        return cardDataList.map { cardData -> this.createWhiteCard(cardData, cardpackId) }
    }

    override fun createBlackCard(cardData: JsonBlackCard, cardpackId: String): BlackCardModel {
        val cardpack = this.getCardpack(cardpackId) as MemoryCardpackModel
        val card = MemoryBlackCardModel(this.blackCardId.toString(), cardData.text, cardData.answerFields, cardpackId)
        cardpack.blackCards.add(card)
        this.blackCardId++
        return card
    }

    override fun createBlackCards(cardDataList: List<JsonBlackCard>, cardpackId: String): List<BlackCardModel> {
        // TODO - Test for atomicity
        return cardDataList.map { cardData -> this.createBlackCard(cardData, cardpackId) }
    }

    override fun deleteCardpack(cardpackId: String) {
        if (cardpacks[cardpackId] == null) {
            throw Exception("Cardpack does not exist with id: $cardpackId")
        }
        cardpacks.remove(cardpackId)
    }

    override fun deleteWhiteCard(cardId: String) {
        for (entry in cardpacks) {
            val cardpackList = entry.value
            for (cardpack in cardpackList) {
                cardpack as MemoryCardpackModel
                for (card in cardpack.whiteCards) {
                    if (card.id == cardId) {
                        cardpack.whiteCards.remove(card)
                        return
                    }
                }
            }
        }
        throw Exception("Card does not exist with id: $cardId")
    }

    override fun deleteWhiteCards(ids: List<String>) {
        ids.forEach { id ->
            if (!whiteCardExists(id)){
                throw Exception("One or more card ids is invalid")
            }
        }
        ids.forEach { id -> this.deleteWhiteCard(id) }
    }

    override fun deleteBlackCard(cardId: String) {
        for (entry in cardpacks) {
            val cardpackList = entry.value
            for (cardpack in cardpackList) {
                cardpack as MemoryCardpackModel
                for (card in cardpack.blackCards) {
                    if (card.id == cardId) {
                        cardpack.blackCards.remove(card)
                        return
                    }
                }
            }
        }
        throw Exception("Card does not exist with id: $cardId")
    }

    override fun deleteBlackCards(ids: List<String>) {
        ids.forEach { id ->
            if (!blackCardExists(id)){
                throw Exception("One or more card ids is invalid")
            }
        }
        ids.forEach { id -> this.deleteBlackCard(id) }
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

    private fun whiteCardExists(id: String): Boolean {
        for (entry in cardpacks) {
            val cardpackList = entry.value
            for (cardpack in cardpackList) {
                cardpack as MemoryCardpackModel
                for (card in cardpack.whiteCards) {
                    if (card.id == id) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun blackCardExists(id: String): Boolean {
        for (entry in cardpacks) {
            val cardpackList = entry.value
            for (cardpack in cardpackList) {
                cardpack as MemoryCardpackModel
                for (card in cardpack.blackCards) {
                    if (card.id == id) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private class MemoryCardpackModel(override val id: String, override var name: String, override val owner: UserModel) : CardpackModel {
        override val whiteCards: MutableList<WhiteCardModel> = ArrayList()
        override val blackCards: MutableList<BlackCardModel> = ArrayList()

        override fun setName(name: String): CardpackModel {
            this.name = name
            return this
        }
    }

    private class MemoryWhiteCardModel(override val id: String, override var text: String, override val cardpackId: String) : WhiteCardModel {
        override fun setText(text: String): WhiteCardModel {
            this.text = text
            return this
        }
    }

    private class MemoryBlackCardModel(override val id: String, override var text: String, override val answerFields: Int, override val cardpackId: String) : BlackCardModel {
        override fun setText(text: String): BlackCardModel {
            this.text = text
            return this
        }
    }
}