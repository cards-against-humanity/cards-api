package route.user

import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import database.DatabaseCollection
import route.JsonPatchItem
import route.user.model.UserModel

@RestController
class UserController(private val database: DatabaseCollection) {

    @RequestMapping(value = "/user/{id}", method = [RequestMethod.GET])
    @ApiOperation(value = "Get a user")
    @ApiResponses(
            ApiResponse(code = 200, message = "User retrieved"),
            ApiResponse(code = 404, message = "User does not exist")
    )
    fun getUser(@PathVariable id: String): ResponseEntity<UserModel> {
        return try {
            ResponseEntity.ok(database.getUser(id))
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
    ): ResponseEntity<UserModel> {
        if ((oAuthProvider == null) xor (oAuthId == null)) {
            return ResponseEntity.badRequest().build()
        } else if (id == null && oAuthProvider == null) {
            return ResponseEntity.badRequest().build()
        } else if (id != null && oAuthProvider != null) {
            return ResponseEntity.badRequest().build()
        }

        return try {
            if (oAuthId != null && oAuthProvider != null) {
                ResponseEntity.ok(database.getUser(oAuthId, oAuthProvider))
            } else {
                ResponseEntity.ok(database.getUser(id!!))
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
    fun createUser(@RequestBody user: JsonUser): ResponseEntity<UserModel> {
        return try {
            ResponseEntity.ok(database.createUser(user.name!!, user.oAuthId!!, user.oAuthProvider!!))
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
        val user: UserModel

        try {
            user = database.getUser(id)
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
        val user: UserModel
        val friend: UserModel
        try {
            user = database.getUser(userId)
            friend = database.getUser(friendId)
        } catch (e: Exception) {
            return ResponseEntity.notFound().build()
        }

        return try {
            database.addFriend(user.id, friend.id)
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
        val user: UserModel
        val friend: UserModel

        try {
            user = database.getUser(userId)
            friend = database.getUser(friendId)
        } catch (e: Exception) {
            return ResponseEntity.notFound().build()
        }

        return try {
            database.removeFriend(user.id, friend.id)
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
    fun getFriends(@PathVariable id: String): ResponseEntity<List<UserModel>> {
        return try {
            val user = database.getUser(id)
            ResponseEntity.ok(database.getFriends(user.id))
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
    fun getFriendRequestsSent(@PathVariable id: String): ResponseEntity<List<UserModel>> {
        return try {
            val user = database.getUser(id)
            ResponseEntity.ok(database.getSentRequests(user.id))
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
    fun getFriendRequestsReceived(@PathVariable id: String): ResponseEntity<List<UserModel>> {
        return try {
            val user = database.getUser(id)
            ResponseEntity.ok(database.getReceivedRequests(user.id))
        } catch (e: Exception) {
            ResponseEntity.notFound().build()
        }
    }
}
