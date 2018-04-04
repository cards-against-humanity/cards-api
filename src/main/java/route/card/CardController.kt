package route.card

import database.DatabaseCollection
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.bson.types.ObjectId
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import route.JsonPatchItem
import route.card.model.CardCollection
import route.card.model.CardpackModel
import route.user.model.UserCollection
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

    @RequestMapping(value = "/cardpack/{id}/cards", method = [RequestMethod.PUT])
    @ApiOperation(value = "Create cards")
    @ApiResponses(
            ApiResponse(code = 200, message = "Card successfully deleted"),
            ApiResponse(code = 400, message = "Invalid request body"),
            ApiResponse(code = 404, message = "Cardpack does not exist")
    )
    fun createCards(@RequestBody text: List<String>, @PathVariable id: String): ResponseEntity<Void> {
        database.createCards(text, id)
        return ResponseEntity.ok().build()
    }

    @RequestMapping(value = "/card/{id}", method = [RequestMethod.DELETE])
    fun deleteCard(@PathVariable id: String): ResponseEntity<Void> {
        return try {
            database.deleteCard(id)
            ResponseEntity.ok().build()
        } catch (e: Exception) {
            ResponseEntity.notFound().build()
        }
    }

    @RequestMapping(value = "/cardpack/{id}/cards", method = [RequestMethod.GET])
    @ApiOperation(value = "Get cards belonging to a cardpack")
    @ApiResponses(
            ApiResponse(code = 200, message = "Cards retrieved"),
            ApiResponse(code = 404, message = "Cardpack does not exist")
    )
    fun getCards(@PathVariable id: String): ResponseEntity<List<String>> {
        val cardpack: CardpackModel

        try {
            cardpack = database.getCardpack(id)
        } catch (e: Exception) {
            return ResponseEntity.notFound().build()
        }

        return ResponseEntity.ok(cardpack.getCards().map { card -> card.text })
    }
}
