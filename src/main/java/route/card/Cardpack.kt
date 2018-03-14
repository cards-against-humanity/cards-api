package route.card

import com.fasterxml.jackson.annotation.JsonProperty
import org.bson.Document
import org.bson.types.ObjectId
import route.user.User

class Cardpack {

    @JsonProperty("id")        var id:         String private set
    @JsonProperty("name")       var name:       String private set
    @JsonProperty("ownerId")    var ownerId:    String private set

    override fun equals(obj: Any?): Boolean {
        if (obj == null) {
            return false
        }
        if (!Cardpack::class.java.isAssignableFrom(obj.javaClass)) {
            return false
        }
        val cardpack = obj as Cardpack
        return cardpack.id == this.id
    }

    private constructor(doc: Document) {
        id = doc["_id"].toString()
        name = doc["name"].toString()
        ownerId = doc["ownerId"].toString()
    }

    fun getCards(): List<Card> {
        return Card.get(this)
    }

    fun setName(name: String): Cardpack {
        cardpacks.updateOne(Document("_id", ObjectId(this.id)), Document("\$set", Document("name", name)))
        this.name = name
        return this
    }

    companion object {
        private val cardpacks = database.Instance.mongo.getCollection("cardpacks")

        fun get(id: ObjectId): Cardpack {
            return Cardpack(cardpacks.find(Document("_id", id)).first()!!)
        }

        fun get(user: User): List<Cardpack> {
            return cardpacks.find(Document("ownerId", user.id)).toList().map { Cardpack(it) }
        }

        fun create(name: String, owner: User): Cardpack {
            val id = ObjectId()
            cardpacks.insertOne(Document()
                    .append("_id", id)
                    .append("name", name)
                    .append("ownerId", owner.id)
            )
            return get(id)
        }
    }
}
