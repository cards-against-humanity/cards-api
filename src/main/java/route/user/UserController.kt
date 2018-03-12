package route.user

import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class UserController {
    @RequestMapping(value = "/user/{id}", method = arrayOf(RequestMethod.GET))
    @ApiOperation(value = "Get a user")
    @ApiResponses(
            ApiResponse(code = 200, message = "User retrieved"),
            ApiResponse(code = 404, message = "User does not exist")
    )
    fun getUser(@PathVariable id: String): ResponseEntity<User> {
        return try {
            ResponseEntity.ok(User.get(ObjectId(id)))
        } catch (e: Exception) {
            ResponseEntity.notFound().build()
        }
    }

    @RequestMapping(value = "/user", method = arrayOf(RequestMethod.GET))
    @ApiOperation(value = "Get a user")
    @ApiResponses(
            ApiResponse(code = 200, message = "User retrieved"),
            ApiResponse(code = 400, message = "Illegal request parameters"),
            ApiResponse(code = 404, message = "User does not exist")
    )
    fun getUser(
            @RequestParam(value = "id", required = false) id: String?,
            @RequestParam(value = "oAuthProvider", required = false) oAuthProvider: String?,
            @RequestParam(value = "oAuthId", required = false) oAuthId: String?
    ): ResponseEntity<User> {
        if ((oAuthProvider == null) xor (oAuthId == null)) {
            return ResponseEntity.badRequest().build()
        } else if (id == null && oAuthProvider == null) {
            return ResponseEntity.badRequest().build()
        } else if (id != null && oAuthProvider != null) {
            return ResponseEntity.badRequest().build()
        }

        return try {
            if (oAuthId != null && oAuthProvider != null) {
                ResponseEntity.ok(User.get(oAuthId, oAuthProvider))
            } else {
                ResponseEntity.ok(User.get(ObjectId(id!!)))
            }
        } catch (e: Exception) {
            ResponseEntity.notFound().build()
        }

    }

    @RequestMapping(value = "/user", method = [(RequestMethod.PUT)])
    @ApiOperation(value = "Create a user")
    @ApiResponses(
            ApiResponse(code = 200, message = "User created"),
            ApiResponse(code = 400, message = "Invalid request body")
    )
    fun createUser(@RequestBody user: JsonUser): ResponseEntity<User> {
        return try {
            val u = User.create(user.oAuthId!!, user.oAuthProvider!!, user.name!!)
            ResponseEntity.ok(u)
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(null)
        }
    }

    @RequestMapping(value = "/user/{id}", method = [RequestMethod.PATCH])
    @ApiOperation(value = "Edit a user")
    fun patchUser(@RequestBody patchDoc: List<Document>, @PathVariable id: String): ResponseEntity<*> {
        patchDoc.forEach({ doc ->
            run {
                val op = doc["op"] as String
                val path = doc["path"] as String
                val value = doc["value"]

                if (op == "replace" && path == "/name") {
                    User.get(ObjectId(id)).setName(value as String)
                }
            }
        })
        return ResponseEntity.ok().build<Any>()
    }


    @RequestMapping(value = "/user/{userId}/friends/{friendId}", method = [RequestMethod.PUT])
    @ApiOperation(value = "Add a friend")
    fun addFriend(@PathVariable userId: String, @PathVariable friendId: String): ResponseEntity<HttpStatus> {
        Friend.addFriend(User.get(ObjectId(userId)), User.get(ObjectId(friendId)))
        return ResponseEntity(HttpStatus.CREATED)
    }

    @RequestMapping(value = "/user/{userId}/friends/{friendId}", method = [RequestMethod.DELETE])
    @ApiOperation(value = "Remove a friend")
    fun removeFriend(@PathVariable userId: String, @PathVariable friendId: String): ResponseEntity<*> {
        Friend.removeFriend(User.get(ObjectId(userId)), User.get(ObjectId(friendId)))
        return ResponseEntity.ok().build<Any>()
    }

    @RequestMapping(value = "/user/{id}/friends", method = [RequestMethod.GET])
    @ApiOperation(value = "Get friends")
    fun getFriends(@PathVariable id: String): ResponseEntity<List<User>> {
        return ResponseEntity.ok(Friend.getFriends(User.get(ObjectId(id))))
    }

    @RequestMapping(value = "/user/{id}/friends/requests/sent", method = [RequestMethod.GET])
    @ApiOperation(value = "Get friend requests sent to other users")
    fun getFriendRequestsSent(@PathVariable id: String): ResponseEntity<List<User>> {
        return ResponseEntity.ok(Friend.getSentRequests(User.get(ObjectId(id))))
    }

    @RequestMapping(value = "/user/{id}/friends/requests/received", method = [RequestMethod.GET])
    @ApiOperation(value = "Get friend requests received from other users")
    fun getFriendRequestsReceived(@PathVariable id: String): ResponseEntity<List<User>> {
        return ResponseEntity.ok(Friend.getReceivedRequests(User.get(ObjectId(id))))
    }
}
