package route.user

import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.function.Consumer

@RestController
class UserController {
    @RequestMapping(value = "/user/{id}", method = arrayOf(RequestMethod.GET))
    fun getUser(@PathVariable id: String): User {
        return User.get(ObjectId(id))
    }

    @RequestMapping(value = "/user", method = arrayOf(RequestMethod.GET))
    fun getUser(
            @RequestParam(value = "id", required = false) id: String?,
            @RequestParam(value = "oauthprovider", required = false) oAuthProvider: String?,
            @RequestParam(value = "oauthid", required = false) oAuthId: String?): ResponseEntity<User> {
        println(oAuthId + ", " + oAuthProvider)
        if ((oAuthProvider == null) xor (oAuthId == null)) {
            return ResponseEntity.badRequest().build()
        } else if (id == null && oAuthProvider == null) {
            return ResponseEntity.badRequest().build()
        } else if (id != null && oAuthProvider != null) {
            return ResponseEntity.badRequest().build()
        }

        return try {
            if (oAuthProvider != null) {
                ResponseEntity.ok(User.get(oAuthId!!, oAuthProvider))
            } else {
                ResponseEntity.ok(User.get(ObjectId(id!!)))
            }
        } catch (e: Exception) {
            ResponseEntity.notFound().build()
        }

    }

    @RequestMapping(value = "/user", method = arrayOf(RequestMethod.PUT))
    fun createUser(@RequestBody userDoc: Document): ResponseEntity<User> {
        return try {
            val oAuthId = userDoc["oAuthId"] as String
            val oAuthProvider = userDoc["oAuthProvider"] as String
            val name = userDoc["name"] as String
            ResponseEntity.ok(User.create(oAuthId, oAuthProvider, name))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(null)
        }

    }

    @RequestMapping(value = "/user/{id}", method = arrayOf(RequestMethod.PATCH))
    fun patchUser(@RequestBody patchDoc: List<Document>, @PathVariable id: String): ResponseEntity<*> {
        patchDoc.forEach({ doc ->
            run {
                val op = doc.get("op") as String
                val path = doc.get("path") as String
                val value = doc.get("value")

                if (op == "replace" && path == "/name") {
                    User.get(ObjectId(id)).setName(value as String)
                }
            }
        })
        return ResponseEntity.ok().build<Any>()
    }


    @RequestMapping(value = "/user/{userId}/friends/{friendId}", method = arrayOf(RequestMethod.PUT))
    fun addFriend(@PathVariable userId: String, @PathVariable friendId: String): ResponseEntity<HttpStatus> {
        Friend.addFriend(User.get(ObjectId(userId)), User.get(ObjectId(friendId)))
        return ResponseEntity(HttpStatus.CREATED)
    }

    @RequestMapping(value = "/user/{userId}/friends/{friendId}", method = arrayOf(RequestMethod.DELETE))
    fun removeFriend(@PathVariable userId: String, @PathVariable friendId: String): ResponseEntity<*> {
        Friend.removeFriend(User.get(ObjectId(userId)), User.get(ObjectId(friendId)))
        return ResponseEntity.ok().build<Any>()
    }

    @RequestMapping(value = "/user/{id}/friends", method = arrayOf(RequestMethod.GET))
    fun getFriends(@PathVariable id: String): ResponseEntity<List<User>> {
        return ResponseEntity.ok(Friend.getFriends(User.get(ObjectId(id))))
    }

    @RequestMapping(value = "/user/{id}/friends/requests/sent", method = arrayOf(RequestMethod.GET))
    fun getFriendRequestsSent(@PathVariable id: String): ResponseEntity<List<User>> {
        return ResponseEntity.ok(Friend.getSentRequests(User.get(ObjectId(id))))
    }

    @RequestMapping(value = "/user/{id}/friends/requests/received", method = arrayOf(RequestMethod.GET))
    fun getFriendRequestsReceived(@PathVariable id: String): ResponseEntity<List<User>> {
        return ResponseEntity.ok(Friend.getReceivedRequests(User.get(ObjectId(id))))
    }
}
