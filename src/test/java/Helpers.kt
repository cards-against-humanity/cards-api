import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.test.web.servlet.ResultActions
import route.card.model.CardpackModel
import route.user.model.UserModel
import java.util.ArrayList
import java.util.HashMap

@Throws(Exception::class)
fun toMap(result: ResultActions): Map<String, Any> {
    return ObjectMapper().readValue(result.andReturn().response.contentAsString, HashMap::class.java) as Map<String, Any>
}

@Throws(Exception::class)
fun toList(result: ResultActions): List<Any> {
    return ObjectMapper().readValue(result.andReturn().response.contentAsString, ArrayList::class.java)
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

fun userEquals(user: UserModel, obj: Any): Boolean {
    return try {
        obj as Map<String, String>
        user.id == obj["id"]
    } catch (e: Exception) {
        false
    }
}

fun cardpackEquals(cardpackOne: CardpackModel, cardpackTwo: CardpackModel): Boolean {
    return cardpackOne.id == cardpackTwo.id && cardpackOne.name == cardpackTwo.name && cardpackOne.ownerId == cardpackTwo.ownerId
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