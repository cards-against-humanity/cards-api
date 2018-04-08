package route.card

import database.DatabaseCollection
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import route.JsonPatchItem
import route.card.model.*
import route.user.model.UserModel

@RestController
class CardController(private val database: DatabaseCollection) {
    @RequestMapping(value = "/{userId}/cardpack", method = [RequestMethod.PUT])
    @ApiOperation(value = "Create a cardpack")
    @ApiResponses(
            ApiResponse(code = 200, message = "Cardpack created"),
            ApiResponse(code = 400, message = "Invalid request body"),
            ApiResponse(code = 404, message = "User does not exist")
    )
    fun createCardpack(@PathVariable userId: String, @RequestBody doc: JsonCardpack): ResponseEntity<CardpackModel> {
        var user: UserModel

        try {
            user = database.getUser(userId)
        } catch (e: Exception) {
            return ResponseEntity.notFound().build()
        }

        return try {
            val cardpack = database.createCardpack(doc.name!!, user.id)
            ResponseEntity.ok(cardpack)
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }

    @RequestMapping(value = "/cardpack/{id}", method = [RequestMethod.GET])
    @ApiOperation(value = "Get a cardpack")
    @ApiResponses(
            ApiResponse(code = 200, message = "Cardpack retrieved"),
            ApiResponse(code = 404, message = "Cardpack does not exist")
    )
    fun getCardpack(@PathVariable id: String): ResponseEntity<CardpackModel> {
        return try {
            val cardpack = database.getCardpack(id)
            ResponseEntity.ok(cardpack)
        } catch (e: Exception) {
            ResponseEntity.notFound().build()
        }
    }

    @RequestMapping(value = "/{userId}/cardpacks", method = [RequestMethod.GET])
    @ApiOperation(value = "Get all cardpacks belonging to a certain user")
    @ApiResponses(
            ApiResponse(code = 200, message = "Cardpacks retrieved"),
            ApiResponse(code = 404, message = "User does not exist")
    )
    fun getCardpacksByUser(@PathVariable userId: String): ResponseEntity<List<CardpackModel>> {
        return try {
            val cardpacks = database.getCardpacks(userId)
            ResponseEntity.ok(cardpacks)
        } catch (e: Exception) {
            ResponseEntity.notFound().build()
        }
    }

    @RequestMapping(value = "/cardpack/{id}", method = [RequestMethod.PATCH])
    @ApiOperation(value = "Edit a cardpack")
    @ApiResponses(
            ApiResponse(code = 200, message = "Patch succeeded"),
            ApiResponse(code = 400, message = "Invalid request body"),
            ApiResponse(code = 404, message = "Cardpack does not exist")
    )
    fun patchCardpack(@RequestBody patchDoc: List<JsonPatchItem>, @PathVariable id: String): ResponseEntity<Void> {
        val cardpack: CardpackModel

        try {
            cardpack = database.getCardpack(id)
        } catch (e: Exception) {
            return ResponseEntity.notFound().build()
        }

        patchDoc.forEach({ doc ->
            run {
                when {
                    doc.op == "replace" && doc.path == "/name" -> cardpack.setName(doc.value as String)
                    else -> return ResponseEntity.badRequest().build()
                }
            }
        })
        return ResponseEntity.ok().build()
    }

    @RequestMapping(value = "/cardpack/{id}", method = [RequestMethod.DELETE])
    @ApiOperation(value = "Delete a cardpack")
    @ApiResponses(
            ApiResponse(code = 200, message = "Cardpack successfully deleted"),
            ApiResponse(code = 404, message = "Cardpack does not exist")
    )
    fun deleteCardpack(@PathVariable id: String): ResponseEntity<Void> {
        return try {
            database.deleteCardpack(id)
            ResponseEntity.ok().build()
        } catch (e: Exception) {
            ResponseEntity.notFound().build()
        }
    }

    @RequestMapping(value = "/cardpack/{id}/cards/white", method = [RequestMethod.PUT])
    @ApiOperation(value = "Create white cards")
    @ApiResponses(
            ApiResponse(code = 200, message = "Cards successfully created"),
            ApiResponse(code = 400, message = "Invalid request body"),
            ApiResponse(code = 404, message = "Cardpack does not exist")
    )
    fun createWhiteCards(@RequestBody cards: List<JsonWhiteCard>, @PathVariable id: String): ResponseEntity<Void> {
        try {
            database.getCardpack(id)
        } catch (e: Exception) {
            return ResponseEntity.notFound().build()
        }
        database.createWhiteCards(cards, id)
        return ResponseEntity.ok().build()
    }

    @RequestMapping(value = "/cardpack/{id}/cards/black", method = [RequestMethod.PUT])
    @ApiOperation(value = "Create black cards")
    @ApiResponses(
            ApiResponse(code = 200, message = "Cards successfully created"),
            ApiResponse(code = 400, message = "Invalid request body"),
            ApiResponse(code = 404, message = "Cardpack does not exist")
    )
    fun createBlackCards(@RequestBody cards: List<JsonBlackCard>, @PathVariable id: String): ResponseEntity<Void> {
        try {
            database.getCardpack(id)
        } catch (e: Exception) {
            return ResponseEntity.notFound().build()
        }
        database.createBlackCards(cards, id)
        return ResponseEntity.ok().build()
    }

    @RequestMapping(value = "/cards/white/{id}", method = [RequestMethod.DELETE])
    @ApiOperation(value = "Delete white card")
    @ApiResponses(
            ApiResponse(code = 200, message = "Cards successfully created"),
            ApiResponse(code = 404, message = "Card does not exist")
    )
    fun deleteWhiteCard(@PathVariable id: String): ResponseEntity<Void> {
        return try {
            database.deleteWhiteCard(id)
            ResponseEntity.ok().build()
        } catch (e: Exception) {
            ResponseEntity.notFound().build()
        }
    }

    @RequestMapping(value = "/cards/black/{id}", method = [RequestMethod.DELETE])
    @ApiOperation(value = "Delete black card")
    @ApiResponses(
            ApiResponse(code = 200, message = "Cards successfully created"),
            ApiResponse(code = 404, message = "Card does not exist")
    )
    fun deleteBlackCard(@PathVariable id: String): ResponseEntity<Void> {
        return try {
            database.deleteBlackCard(id)
            ResponseEntity.ok().build()
        } catch (e: Exception) {
            ResponseEntity.notFound().build()
        }
    }
}