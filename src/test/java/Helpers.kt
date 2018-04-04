import com.fasterxml.jackson.databind.ObjectMapper
import com.mongodb.MongoClient
import com.mongodb.ServerAddress
import com.mongodb.client.MongoCollection
import org.bson.Document
import org.springframework.test.web.servlet.ResultActions
import route.card.model.CardModel
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

fun cardpackEquals(cardpackOne: CardpackModel, cardpackTwo: CardpackModel): Boolean {
    return cardpackOne.id == cardpackTwo.id && cardpackOne.name == cardpackTwo.name && cardpackOne.owner.id == cardpackTwo.owner.id
}

fun cardEquals(cardOne: CardModel, cardTwo: CardModel): Boolean {
    return cardOne.id == cardTwo.id && cardOne.text == cardTwo.text && cardOne.cardpackId == cardTwo.cardpackId
}

@Throws(Exception::class)
fun resEquals(result: ResultActions, obj: Any): Boolean {
    return obj == toMap(result)
}

fun usersAreEqual(userOne: UserModel, userTwo: UserModel): Boolean {
    return userOne.id == userTwo.id &&
            userOne.name == userTwo.name &&
            userOne.oAuthId == userTwo.oAuthId &&
            userOne.oAuthProvider == userTwo.oAuthProvider
}


val mongoClient = MongoClient(ServerAddress(InetAddress.getLoopbackAddress())).getDatabase("appTest")

fun getTestMongoCollection(name: String): MongoCollection<Document> {
    return mongoClient.getCollection(name)
}

fun resetTestMongo() {
    mongoClient.drop()
}