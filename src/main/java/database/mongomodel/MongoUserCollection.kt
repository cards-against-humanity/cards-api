package database.mongomodel

import com.mongodb.client.MongoCollection
import com.mongodb.client.model.IndexOptions
import org.bson.Document
import org.bson.types.ObjectId
import route.user.model.UserCollection
import route.user.model.UserModel

class MongoUserCollection(val mongoCollection: MongoCollection<Document>) : UserCollection {
    init {
        mongoCollection.createIndex(Document("oAuthProvider", 1).append("oAuthId", 1), IndexOptions().unique(true))
    }

    override fun getUser(id: String): UserModel {
        val doc: Document
        try {
            doc = mongoCollection.find(Document("_id", ObjectId(id))).first()
        } catch (e: Exception) {
            throw Exception("User does not exist with id: $id")
        }
        val id = doc["_id"] as ObjectId
        return MongoUserModel(id.toHexString(), doc["name"] as String, doc["oAuthId"] as String, doc["oAuthProvider"] as String, mongoCollection)
    }

    override fun getUsers(ids: List<String>): List<UserModel> {
        return ids.map { id -> getUser(id) }
    }

    override fun getUser(oAuthId: String, oAuthProvider: String): UserModel {
        val doc: Document
        try {
            doc = mongoCollection.find(Document("oAuthId", oAuthId).append("oAuthProvider", oAuthProvider)).first()
        } catch (e: Exception) {
            throw Exception("User does not exist with oAuthId of $oAuthId and oAuthProvider of $oAuthProvider")
        }
        val id = doc["_id"] as ObjectId
        return MongoUserModel(id.toHexString(), doc["name"] as String, doc["oAuthId"] as String, doc["oAuthProvider"] as String, mongoCollection)
    }

    override fun createUser(name: String, oAuthId: String, oAuthProvider: String): UserModel {
        val id = ObjectId()
        val doc = Document()
                .append("_id", id)
                .append("oAuthId", oAuthId)
                .append("oAuthProvider", oAuthProvider)
                .append("name", name)
        try {
            mongoCollection.insertOne(doc)
        } catch (e: Exception) {
            throw Exception("User already exists with that oAuth ID and provider")
        }
        return getUser(id.toHexString())
    }

    private class MongoUserModel(override val id: String, override var name: String, override val oAuthId: String, override val oAuthProvider: String, private val mongoCollection: MongoCollection<Document>) : UserModel {
        override fun setName(name: String): UserModel {
            mongoCollection.updateOne(Document("_id", ObjectId(id)), Document("\$set", Document("name", name)))
            this.name = name
            return this
        }
    }
}