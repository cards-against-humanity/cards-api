package database.mongomodel

import com.mongodb.client.MongoCollection
import org.bson.Document
import org.bson.types.ObjectId
import route.card.JsonBlackCard
import route.card.JsonWhiteCard
import route.card.model.*
import route.user.model.UserCollection
import route.user.model.UserModel

class MongoCardCollection(private val mongoCollectionCardpacks: MongoCollection<Document>, private val mongoCollectionWhiteCards: MongoCollection<Document>, private val mongoCollectionBlackCards: MongoCollection<Document>, private val userCollection: UserCollection) : CardCollection {
    override fun createCardpack(name: String, userId: String): CardpackModel {
        val user = userCollection.getUser(userId)
        val id = ObjectId()
        mongoCollectionCardpacks.insertOne(Document()
                .append("_id", id)
                .append("name", name)
                .append("ownerId", userId)
        )
        return MongoCardpackModel(id.toHexString(), name, user, ArrayList(), ArrayList(), mongoCollectionCardpacks)
    }

    override fun createWhiteCard(card: JsonWhiteCard): WhiteCardModel {
        this.getCardpack(card.cardpackId)
        val id = ObjectId()
        mongoCollectionWhiteCards.insertOne(Document()
                .append("_id", id)
                .append("text", card.text)
                .append("cardpackId", card.cardpackId)
        )
        return MongoWhiteCardModel(id.toHexString(), card.text, card.cardpackId, mongoCollectionWhiteCards)
    }

    override fun createBlackCard(card: JsonBlackCard): BlackCardModel {
        this.getCardpack(card.cardpackId)
        val id = ObjectId()
        mongoCollectionBlackCards.insertOne(Document()
                .append("_id", id)
                .append("text", card.text)
                .append("answerFields", card.answerFields)
                .append("cardpackId", card.cardpackId)
        )
        return MongoBlackCardModel(id.toHexString(), card.text, card.answerFields, card.cardpackId, mongoCollectionBlackCards)
    }

    override fun createWhiteCards(cards: List<JsonWhiteCard>): List<WhiteCardModel> {
        // TODO - Improve efficiency and assert atomicity
        return cards.map { card -> createWhiteCard(card) }
    }

    override fun createBlackCards(cards: List<JsonBlackCard>): List<BlackCardModel> {
        // TODO - Improve efficiency and assert atomicity
        return cards.map { card -> createBlackCard(card) }
    }

    override fun deleteCardpack(id: String) {
        try {
            mongoCollectionCardpacks.deleteOne(Document("_id", ObjectId(id)))
            mongoCollectionWhiteCards.deleteMany(Document("cardpackId", id))
            mongoCollectionBlackCards.deleteMany(Document("cardpackId", id))
        } catch (e: Exception) {
            throw Exception("Cardpack does not exist with id: $id")
        }
    }

    override fun deleteWhiteCard(id: String) {
        try {
            val deletedCount = mongoCollectionWhiteCards.deleteOne(Document("_id", ObjectId(id))).deletedCount
            if (deletedCount == 0L) {
                throw Exception()
            }
        } catch (e: Exception) {
            throw Exception("Card does not exist with id: $id")
        }
    }

    override fun deleteBlackCard(id: String) {
        try {
            val deletedCount = mongoCollectionBlackCards.deleteOne(Document("_id", ObjectId(id))).deletedCount
            if (deletedCount == 0L) {
                throw Exception()
            }
        } catch (e: Exception) {
            throw Exception("Card does not exist with id: $id")
        }
    }

    override fun deleteWhiteCards(ids: List<String>) {
        // TODO - Assert atomicity
        try {
            mongoCollectionWhiteCards.deleteMany(Document("\$or", ids.map { id -> Document("_id", ObjectId(id)) }))
        } catch (e: Exception) {
            throw Exception("One or more card ids is invalid")
        }
    }

    override fun deleteBlackCards(ids: List<String>) {
        // TODO - Assert atomicity
        try {
            mongoCollectionBlackCards.deleteMany(Document("\$or", ids.map { id -> Document("_id", ObjectId(id)) }))
        } catch (e: Exception) {
            throw Exception("One or more card ids is invalid")
        }
    }

    override fun getCardpack(id: String): CardpackModel {
        try {
            val cardpackDoc = mongoCollectionCardpacks.find(Document("_id", ObjectId(id))).first()
            val whiteCards = mongoCollectionWhiteCards.find(Document("cardpackId", id)).map { cardDoc -> MongoWhiteCardModel(cardDoc, mongoCollectionWhiteCards) }.toList()
            val blackCards = mongoCollectionBlackCards.find(Document("cardpackId", id)).map { cardDoc -> MongoBlackCardModel(cardDoc, mongoCollectionBlackCards) }.toList()
            return MongoCardpackModel(id, cardpackDoc["name"] as String, userCollection.getUser(cardpackDoc["ownerId"] as String), whiteCards, blackCards, mongoCollectionCardpacks)
        } catch (e: Exception) {
            throw Exception("Cardpack does not exist with id: $id")
        }
    }

    override fun getCardpacks(userId: String): List<CardpackModel> {
        val user = userCollection.getUser(userId)
        val cardpackDocs = mongoCollectionCardpacks.find(Document("ownerId", userId)).toList()
        return cardpackDocs.map { cardpackDoc ->
            val id = cardpackDoc["_id"] as ObjectId
            val whiteCards = mongoCollectionWhiteCards.find(Document("cardpackId", id.toHexString())).map { cardDoc -> MongoWhiteCardModel(cardDoc, mongoCollectionWhiteCards) }.toList()
            val blackCards = mongoCollectionBlackCards.find(Document("cardpackId", id.toHexString())).map { cardDoc -> MongoBlackCardModel(cardDoc, mongoCollectionBlackCards) }.toList()
            MongoCardpackModel(id.toHexString(), cardpackDoc["name"] as String, user, whiteCards, blackCards, mongoCollectionCardpacks)
        }
    }

    private class MongoCardpackModel(
            override val id: String,
            override var name: String,
            override val owner: UserModel,
            override val whiteCards: List<WhiteCardModel>,
            override val blackCards: List<BlackCardModel>,
            private val mongoCollectionCardpacks: MongoCollection<Document>) : CardpackModel {

        override fun setName(name: String): CardpackModel {
            mongoCollectionCardpacks.updateOne(Document("_id", ObjectId(this.id)), Document("\$set", Document("name", name)))
            this.name = name
            return this
        }
    }

    private class MongoWhiteCardModel(override val id: String, override var text: String, override val cardpackId: String, val mongoCollectionWhiteCards: MongoCollection<Document>) : WhiteCardModel {
        constructor(json: Document, mongoCollectionWhiteCards: MongoCollection<Document>) : this((json["_id"] as ObjectId).toHexString(), json["text"] as String, json["cardpackId"] as String, mongoCollectionWhiteCards)

        override fun setText(text: String): WhiteCardModel {
            mongoCollectionWhiteCards.updateOne(Document("_id", ObjectId(this.id)), Document("\$set", Document("text", text)))
            this.text = text
            return this
        }
    }

    private class MongoBlackCardModel(override val id: String, override var text: String, override val answerFields: Int, override val cardpackId: String, val mongoCollectionBlackCards: MongoCollection<Document>) : BlackCardModel {
        constructor(json: Document, mongoCollectionBlackCards: MongoCollection<Document>) : this((json["_id"] as ObjectId).toHexString(), json["text"] as String, json["answerFields"] as Int, json["cardpackId"] as String, mongoCollectionBlackCards)

        override fun setText(text: String): BlackCardModel {
            mongoCollectionBlackCards.updateOne(Document("_id", ObjectId(this.id)), Document("\$set", Document("text", text)))
            this.text = text
            return this
        }
    }
}