package database.mongomodel

import com.mongodb.client.MongoCollection
import org.bson.Document
import org.bson.types.ObjectId
import route.card.model.CardCollection
import route.card.model.CardModel
import route.card.model.CardpackModel
import route.user.model.UserCollection
import route.user.model.UserModel

class MongoCardCollection(private val mongoCollectionCardpacks: MongoCollection<Document>, private val mongoCollectionCards: MongoCollection<Document>, private val userCollection: UserCollection) : CardCollection {
    override fun createCardpack(name: String, userId: String): CardpackModel {
        val user = userCollection.getUser(userId)
        val id = ObjectId()
        mongoCollectionCardpacks.insertOne(Document()
                .append("_id", id)
                .append("name", name)
                .append("ownerId", userId)
        )
        return MongoCardpackModel(id.toHexString(), name, user, mongoCollectionCardpacks, mongoCollectionCards)
    }

    override fun createCard(text: String, cardpackId: String): CardModel {
        this.getCardpack(cardpackId)
        val id = ObjectId()
        mongoCollectionCards.insertOne(Document()
                .append("_id", id)
                .append("text", text)
                .append("cardpackId", cardpackId)
        )
        return MongoCardModel(id.toHexString(), text, cardpackId, mongoCollectionCards)
    }

    override fun createCards(textList: List<String>, cardpackId: String): List<CardModel> {
        this.getCardpack(cardpackId)
        val cardIds = textList.map { ObjectId() }
        mongoCollectionCards.insertMany(cardIds.mapIndexed { i, id -> Document("_id", id).append("text", textList[i]).append("cardpackId", cardpackId) })
        return cardIds.mapIndexed { i, id -> MongoCardModel(id.toHexString(), textList[i], cardpackId, mongoCollectionCards) }
    }

    override fun deleteCardpack(id: String) {
        try {
            val deleted = mongoCollectionCardpacks.deleteOne(Document("_id", ObjectId(id))).deletedCount == 1L
            if (!deleted) {
                throw Exception()
            }
            mongoCollectionCards.deleteMany(Document("cardpackId", id))
        } catch (e: Exception) {
            throw Exception("Cardpack does not exist with id: $id")
        }
    }

    override fun deleteCard(id: String) {
        try {
            val deletedCount = mongoCollectionCards.deleteOne(Document("_id", ObjectId(id))).deletedCount
            if (deletedCount == 0L) {
                throw Exception()
            }
        } catch (e: Exception) {
            throw Exception("Card does not exist with id: $id")
        }
    }

    override fun deleteCards(ids: List<String>) {
        val deletedCount: Long
        try {
            deletedCount = mongoCollectionCards.deleteMany(Document("\$or", ids.map { id -> Document("_id", ObjectId(id)) })).deletedCount
        } catch (e: Exception) {
            throw Exception("One or more card ids is invalid")
        }
    }

    override fun getCardpack(id: String): CardpackModel {
        try {
            val doc = mongoCollectionCardpacks.find(Document("_id", ObjectId(id))).first()
            return MongoCardpackModel(id, doc["name"] as String, userCollection.getUser(doc["ownerId"] as String), mongoCollectionCardpacks, mongoCollectionCards)
        } catch (e: Exception) {
            throw Exception("Cardpack does not exist with id: $id")
        }
    }

    override fun getCardpacks(userId: String): List<CardpackModel> {
        val user = userCollection.getUser(userId)
        val docs = mongoCollectionCardpacks.find(Document("ownerId", userId)).toList()
        return docs.map { doc ->
            val id = doc["_id"] as ObjectId
            MongoCardpackModel(id.toHexString(), doc["name"] as String, user, mongoCollectionCardpacks, mongoCollectionCards)
        }
    }

    override fun getCard(id: String): CardModel {
        try {
            val doc = mongoCollectionCards.find(Document("_id", ObjectId(id))).first()
            return MongoCardModel(id, doc["text"] as String, doc["cardpackId"] as String, mongoCollectionCards)
        } catch (e: Exception) {
            throw Exception("Card does not exist with id: $id")
        }
    }

    private class MongoCardpackModel(
            override val id: String,
            override var name: String,
            override val owner: UserModel,
            private val mongoCollectionCardpacks: MongoCollection<Document>,
            private val mongoCollectionCards: MongoCollection<Document>) : CardpackModel {

        override fun setName(name: String): CardpackModel {
            mongoCollectionCardpacks.updateOne(Document("_id", ObjectId(this.id)), Document("\$set", Document("name", name)))
            this.name = name
            return this
        }

        override fun getCards(): List<CardModel> {
            return mongoCollectionCards.find(Document("cardpackId", this.id)).toList().map { doc ->
                val id = doc["_id"] as ObjectId
                MongoCardModel(id.toHexString(), doc["text"] as String, doc["cardpackId"] as String, mongoCollectionCards)
            }
        }
    }

    private class MongoCardModel(override val id: String, override var text: String, override val cardpackId: String, val mongoCollectionCards: MongoCollection<Document>) : CardModel {
        override fun setText(text: String): CardModel {
            mongoCollectionCards.updateOne(Document("_id", ObjectId(this.id)), Document("\$set", Document("text", text)))
            this.text = text
            return this
        }
    }
}