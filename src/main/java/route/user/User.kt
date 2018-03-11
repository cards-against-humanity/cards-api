package route.user

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.mongodb.client.model.IndexOptions
import org.bson.Document
import org.bson.types.ObjectId

import java.util.Arrays

class User {

    @JsonProperty("id")            var id:            String @JsonIgnore private set
    @JsonProperty("name")          var name:          String @JsonIgnore private set
    @JsonProperty("oAuthId")       var oAuthId:       String @JsonIgnore private set
    @JsonProperty("oAuthProvider") var oAuthProvider: String @JsonIgnore private set

    private constructor(doc: Document) {
        id = doc["_id"].toString()
        name = doc["name"].toString()
        oAuthId = doc["oAuthId"].toString()
        oAuthProvider = doc["oAuthProvider"].toString()
    }

    override fun equals(obj: Any?): Boolean {
        if (obj == null) {
            return false
        }
        if (obj is User) {
            return obj.id == this.id
        } else if (obj is Map<*, *>) {
            return obj["id"] == id && obj["name"] == name && obj["oAuthId"] == oAuthId && obj["oAuthProvider"] == oAuthProvider
        }
        return false
    }

    fun setName(name: String): User {
        users.updateOne(Document("_id", ObjectId(this.id)), Document("\$set", Document("name", name)))
        this.name = name
        return this
    }

    companion object {
        private val users = database.Instance.mongo.getCollection("users")

        init {
            users.createIndex(Document("oAuthProvider", 1).append("oAuthId", 1), IndexOptions().unique(true))
        }

        fun get(userId: ObjectId): User {
            return User(users.find(Document("_id", userId)).first()!!)
        }

        fun get(userIds: List<ObjectId>): List<User> {
            return userIds.indices.map { get(userIds[it]) }
        }

        fun get(vararg userIds: ObjectId): List<User> {
            return get(Arrays.asList(*userIds))
        }

        fun get(oAuthId: String, oAuthProvider: String): User {
            return User(users.find(Document()
                    .append("oAuthId", oAuthId)
                    .append("oAuthProvider", oAuthProvider)
            ).first())
        }

        fun create(oAuthId: String, oAuthProvider: String, name: String): User {
            val id = ObjectId()
            users.insertOne(Document()
                    .append("_id", id)
                    .append("oAuthId", oAuthId)
                    .append("oAuthProvider", oAuthProvider)
                    .append("name", name)
            )
            return get(id)
        }
    }
}
