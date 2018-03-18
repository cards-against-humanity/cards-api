package route.card

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.bson.types.ObjectId
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import route.JsonPatchItem
import route.user.User

@RestController
class CardController {
    @RequestMapping(value = "/{userId}/cardpack", method = [RequestMethod.PUT])
    @ApiOperation(value = "Create a cardpack")
    @ApiResponses(
            ApiResponse(code = 200, message = "Cardpack created"),
            ApiResponse(code = 400, message = "Invalid request body"),
            ApiResponse(code = 404, message = "User does not exist")
    )
    fun createCardpack(@PathVariable userId: String, @RequestBody doc: JsonCardpack): ResponseEntity<Cardpack> {
        var user: User

        try {
            user = User.get(ObjectId(userId))
        } catch (e: Exception) {
            return ResponseEntity.notFound().build()
        }

        return try {
            val cardpack = Cardpack.create(doc.name!!, user)
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
    fun getCardpack(@PathVariable id: String): ResponseEntity<Cardpack> {
        return try {
            val cardpack = Cardpack.get(ObjectId(id))
            ResponseEntity.ok(cardpack)
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
        val cardpack: Cardpack

        try {
            cardpack = Cardpack.get(ObjectId(id))
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
            Cardpack.delete(ObjectId(id))
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
        Card.create(text, Cardpack.get(ObjectId(id)))
        return ResponseEntity.ok().build()
    }

//    @RequestMapping(value = "/card/{id}", method = [RequestMethod.DELETE])
//    fun deleteCard(@PathVariable id: String): ResponseEntity<*> {
//        // TODO - Delete card
//        return ResponseEntity.ok(null)
//    }

    @RequestMapping(value = "/cardpack/{id}/cards", method = [RequestMethod.GET])
    @ApiOperation(value = "Get cards belonging to a cardpack")
    @ApiResponses(
            ApiResponse(code = 200, message = "Cards retrieved"),
            ApiResponse(code = 404, message = "Cardpack does not exist")
    )
    fun getCards(@PathVariable id: String): ResponseEntity<List<String>> {
        val cardpack: Cardpack

        try {
            cardpack = Cardpack.get(ObjectId(id))
        } catch (e: Exception) {
            return ResponseEntity.notFound().build()
        }

        return ResponseEntity.ok(cardpack.getCards().map { card -> card.text })
    }
}
