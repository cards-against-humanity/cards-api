package route.card

import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import route.user.User

class CardController {
    @RequestMapping(value = "/cardpack", method = arrayOf(RequestMethod.PUT))
    fun createCardpack(@RequestBody doc: Document): ResponseEntity<Cardpack> {
        return ResponseEntity.ok(Cardpack.create(doc["name"] as String, User.get(ObjectId(doc["userId"] as String))))
    }

    @RequestMapping(value = "/cardpack/{id}", method = arrayOf(RequestMethod.GET))
    fun getCardpack(@PathVariable id: String): ResponseEntity<Cardpack> {
        return ResponseEntity.ok(Cardpack.get(ObjectId(id)))
    }

    @RequestMapping(value = "/cardpack/{id}", method = arrayOf(RequestMethod.PATCH))
    fun patchCardpack(@RequestBody patchDoc: List<Document>, @PathVariable id: String): ResponseEntity<*>? {
        return null
    }

    @RequestMapping(value = "/cardpack/{id}", method = arrayOf(RequestMethod.DELETE))
    fun deleteCardpack(@RequestBody doc: Document, @PathVariable id: String): ResponseEntity<*>? {
        return null
    }

    @RequestMapping(value = "/cardpack/{id}", method = arrayOf(RequestMethod.PUT))
    fun createCard(@RequestBody userDoc: Document, @PathVariable id: String): ResponseEntity<Card>? {
        return null
    }

    @RequestMapping(value = "/cardpack/{id}/cards", method = arrayOf(RequestMethod.GET))
    fun getCards(@PathVariable id: String): ResponseEntity<Array<Card>>? {
        return null
    }

    @RequestMapping(value = "/cardpack/{cardpackId}/card/{cardId}", method = arrayOf(RequestMethod.PATCH))
    fun patchCard(@RequestBody patchDoc: List<Document>, @PathVariable cardpackId: String, @PathVariable cardId: String): ResponseEntity<*>? {
        return null
    }

    @RequestMapping(value = "/cardpack/{cardpackId}/card/{cardId}", method = arrayOf(RequestMethod.DELETE))
    fun deleteCard(@PathVariable cardpackId: String, @PathVariable cardId: String): ResponseEntity<*>? {
        return null
    }
}
