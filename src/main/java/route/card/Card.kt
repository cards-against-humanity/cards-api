package route.card

import com.fasterxml.jackson.annotation.JsonProperty
import org.bson.Document
import org.bson.types.ObjectId

class Card {

    @JsonProperty("_id")        var id:         String private set
    @JsonProperty("text")       var text:       String private set
    @JsonProperty("cardpackId") var cardpackId: String private set

    override fun equals(obj: Any?): Boolean {
        if (obj == null) {
            return false
        }
        if (!Card::class.java.isAssignableFrom(obj.javaClass)) {
            return false
        }
        val card = obj as Card
        return card.id == this.id
    }

    private constructor(doc: Document) {
        id = doc["_id"].toString()
        text = doc["text"].toString()
        cardpackId = doc["cardpackId"].toString()
    }

    fun setText(text: String): Card {
        cards.updateOne(Document("_id", ObjectId(this.id)), Document("\$set", Document("text", text)))
        this.text = text
        return this
    }

    companion object {
        private val cards = database.Instance.mongo.getCollection("cards")

        fun get(cardpack: Cardpack): List<Card> {
            var cardList = ArrayList<Card>()
            cards.find(Document("cardpackId", cardpack.id)).forEach(fun(doc: Document?) {
                cardList.add(Card(doc!!))
            })
            return cardList
        }

        fun get(id: ObjectId): Card {
            return Card(cards.find(Document("_id", id)).first()!!)
        }

        fun create(text: String, cardpack: Cardpack): Card {
            val id = ObjectId()
            cards.insertOne(Document()
                    .append("_id", id)
                    .append("text", text)
                    .append("cardpackId", cardpack.id)
            )
            return get(id)
        }
    }
}
