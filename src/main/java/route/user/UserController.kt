package route.user

import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.bson.types.ObjectId
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import route.JsonPatchItem

@RestController
class UserController {
    @RequestMapping(value = "/user/{id}", method = [RequestMethod.GET])
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

    @RequestMapping(value = "/user", method = [RequestMethod.GET])
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

    @RequestMapping(value = "/user", method = [RequestMethod.PUT])
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
    @ApiResponses(
            ApiResponse(code = 200, message = "Patch succeeded"),
            ApiResponse(code = 400, message = "Invalid request body"),
            ApiResponse(code = 404, message = "User does not exist")
    )
    fun patchUser(@RequestBody patchDoc: List<JsonPatchItem>, @PathVariable id: String): ResponseEntity<Void> {
        val user: User

        try {
            user = User.get(ObjectId(id))
        } catch (e: Exception) {
            return ResponseEntity.notFound().build()
        }

        patchDoc.forEach({ doc ->
            run {
                when {
                    doc.op == "replace" && doc.path == "/name" -> user.setName(doc.value as String)
                    else -> return ResponseEntity.badRequest().build()
                }
            }
        })
        return ResponseEntity.ok().build()
    }


    @RequestMapping(value = "/user/{userId}/friends/{friendId}", method = [RequestMethod.PUT])
    @ApiOperation(value = "Add a friend")
    @ApiResponses(
            ApiResponse(code = 200, message = "Friend added"),
            ApiResponse(code = 400, message = "Users are already friends"),
            ApiResponse(code = 404, message = "One or both of the users do not exist")
    )
    fun addFriend(@PathVariable userId: String, @PathVariable friendId: String): ResponseEntity<Void> {
        var user: User?
        var friend: User?
        try {
            user = User.get(ObjectId(userId))
            friend = User.get(ObjectId(friendId))
        } catch (e: Exception) {
            return ResponseEntity.notFound().build()
        }

        return try {
            Friend.addFriend(user, friend)
            ResponseEntity.ok().build()
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }

    @RequestMapping(value = "/user/{userId}/friends/{friendId}", method = [RequestMethod.DELETE])
    @ApiOperation(value = "Remove a friend")
    @ApiResponses(
            ApiResponse(code = 200, message = "Friend removed"),
            ApiResponse(code = 400, message = "Users are not friends"),
            ApiResponse(code = 404, message = "One or both of the users do not exist")
    )
    fun removeFriend(@PathVariable userId: String, @PathVariable friendId: String): ResponseEntity<Void> {
        var user: User?
        var friend: User?
        try {
            user = User.get(ObjectId(userId))
            friend = User.get(ObjectId(friendId))
        } catch (e: Exception) {
            return ResponseEntity.notFound().build()
        }

        return try {
            Friend.removeFriend(user, friend)
            ResponseEntity.ok().build()
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }

    @RequestMapping(value = "/user/{id}/friends", method = [RequestMethod.GET])
    @ApiOperation(value = "Get friends")
    @ApiResponses(
            ApiResponse(code = 200, message = "Friends retrieved"),
            ApiResponse(code = 404, message = "User does not exist")
    )
    fun getFriends(@PathVariable id: String): ResponseEntity<List<User>> {
        return try {
            val user = User.get(ObjectId(id))
            ResponseEntity.ok(Friend.getFriends(user))
        } catch (e: Exception) {
            ResponseEntity.notFound().build()
        }
    }

    @RequestMapping(value = "/user/{id}/friends/requests/sent", method = [RequestMethod.GET])
    @ApiOperation(value = "Get friend requests sent to other users")
    @ApiResponses(
            ApiResponse(code = 200, message = "Friend requests retrieved"),
            ApiResponse(code = 404, message = "User does not exist")
    )
    fun getFriendRequestsSent(@PathVariable id: String): ResponseEntity<List<User>> {
        return try {
            val user = User.get(ObjectId(id))
            ResponseEntity.ok(Friend.getSentRequests(user))
        } catch (e: Exception) {
            ResponseEntity.notFound().build()
        }
    }

    @RequestMapping(value = "/user/{id}/friends/requests/received", method = [RequestMethod.GET])
    @ApiOperation(value = "Get friend requests received from other users")
    @ApiResponses(
            ApiResponse(code = 200, message = "Friend requests retrieved"),
            ApiResponse(code = 404, message = "User does not exist")
    )
    fun getFriendRequestsReceived(@PathVariable id: String): ResponseEntity<List<User>> {
        return try {
            val user = User.get(ObjectId(id))
            ResponseEntity.ok(Friend.getReceivedRequests(user))
        } catch (e: Exception) {
            ResponseEntity.notFound().build()
        }
    }
}
