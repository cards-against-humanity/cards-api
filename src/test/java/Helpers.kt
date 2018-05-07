import com.fasterxml.jackson.databind.ObjectMapper
import com.mongodb.MongoClient
import com.mongodb.ServerAddress
import com.mongodb.client.MongoCollection
import org.bson.Document
import org.springframework.test.web.servlet.ResultActions
import route.card.model.BlackCardModel
import route.card.model.WhiteCardModel
import route.card.model.CardpackModel
import route.user.model.UserModel
import java.net.InetAddress
import java.util.ArrayList
import java.util.HashMap

@Throws(Exception::class)
fun toMap(result: ResultActions): Map<String, Any> {
    return ObjectMapper().readValue(result.andReturn().response.contentAsString, HashMap::class.java) as Map<String, Any>
}

@Throws(Exception::class)
fun toList(result: ResultActions): List<Any> {
    return ObjectMapper().readValue(result.andReturn().response.contentAsString, ArrayList::class.java) as List<Any>
}

@Throws(Exception::class)
fun resEquals(result: ResultActions, user: UserModel): Boolean {
    val map = toMap(result)
    return user.id == map["id"]
}

@Throws(Exception::class)
fun resEquals(result: ResultActions, cardpack: CardpackModel): Boolean {
    val map = toMap(result)
    return cardpack.id == map["id"]
}

@Throws(Exception::class)
fun resEquals(result: ResultActions, cardpacks: List<CardpackModel>): Boolean {
    val list = toList(result) as List<Map<String, String>>
    if (list.size != cardpacks.size) {
        return false
    }
    cardpacks.forEachIndexed { i, cardpack ->
        run {
            if (cardpack.id != list[i]["id"]) {
                return false
            }
        }
    }
    return true
}

fun userEquals(user: UserModel, obj: Any): Boolean {
    return try {
        obj as Map<String, String>
        user.id == obj["id"]
    } catch (e: Exception) {
        false
    }
}

fun assertCardpackEquals(cardpackOne: CardpackModel, cardpackTwo: CardpackModel) {
    if (cardpackOne.whiteCards.size != cardpackTwo.whiteCards.size) {
        throw Exception("Cardpack one has ${cardpackOne.whiteCards.size} white cards, but cardpack two has ${cardpackTwo.whiteCards.size}")
    } else if (cardpackOne.blackCards.size != cardpackTwo.blackCards.size) {
        throw Exception("Cardpack one has ${cardpackOne.blackCards.size} black cards, but cardpack two has ${cardpackTwo.blackCards.size}")
    }
    cardpackOne.whiteCards.forEachIndexed { i, card ->
        if (!whiteCardEquals(card, cardpackTwo.whiteCards[i])) {
            throw Exception("Failed at white card at index $i")
        }
    }
    cardpackOne.blackCards.forEachIndexed { i, card ->
        if (!blackCardEquals(card, cardpackTwo.blackCards[i])) {
            throw Exception("Failed at black card at index $i")
        }
    }
    if (cardpackOne.id != cardpackTwo.id) {
        throw Exception("Cardpacks do not have matching ids")
    }
    if (cardpackOne.name != cardpackTwo.name) {
        throw Exception("Cardpacks do not have matching names")
    }
    if (!usersAreEqual(cardpackOne.owner, cardpackTwo.owner)) {
        throw Exception("Cardpacks do not have the same owners")
    }
}

fun whiteCardEquals(cardOne: WhiteCardModel, cardTwo: WhiteCardModel): Boolean {
    return cardOne.id == cardTwo.id && cardOne.text == cardTwo.text && cardOne.cardpackId == cardTwo.cardpackId
}

fun blackCardEquals(cardOne: BlackCardModel, cardTwo: BlackCardModel): Boolean {
    return cardOne.id == cardTwo.id && cardOne.text == cardTwo.text && cardOne.cardpackId == cardTwo.cardpackId && cardOne.answerFields == cardTwo.answerFields
}

@Throws(Exception::class)
fun resEquals(result: ResultActions, obj: Any): Boolean {
    return obj == toMap(result)
}

fun usersAreEqual(userOne: UserModel, userTwo: UserModel): Boolean {
    return userOne.id == userTwo.id &&
            userOne.name == userTwo.name
}


val mongoClient = MongoClient(ServerAddress(InetAddress.getLoopbackAddress())).getDatabase("appTest")

fun getTestMongoCollection(name: String): MongoCollection<Document> {
    return mongoClient.getCollection(name)
}

fun resetTestMongo() {
    mongoClient.drop()
}