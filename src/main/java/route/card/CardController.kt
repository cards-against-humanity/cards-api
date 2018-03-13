package route.card

import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import route.user.User

@RestController
class CardController {
    @RequestMapping(value = "/cardpack", method = [RequestMethod.PUT])
    fun createCardpack(@RequestBody doc: Document): ResponseEntity<Cardpack> {
        val cardpackName = doc["name"] as String
        val user = User.get(ObjectId(doc["userId"] as String))
        val cardpack = Cardpack.create(cardpackName, user)
        return ResponseEntity.created(null).body(cardpack)
    }

    @RequestMapping(value = "/cardpack/{id}", method = [RequestMethod.GET])
    fun getCardpack(@PathVariable id: String): ResponseEntity<Cardpack> {
        return ResponseEntity.ok(Cardpack.get(ObjectId(id)))
    }

    @RequestMapping(value = "/cardpack/{id}", method = [RequestMethod.PATCH])
    fun patchCardpack(@RequestBody patchDoc: List<Document>, @PathVariable id: String): ResponseEntity<*>? {
        return null
    }

    @RequestMapping(value = "/cardpack/{id}", method = [RequestMethod.DELETE])
    fun deleteCardpack(@RequestBody doc: Document, @PathVariable id: String): ResponseEntity<*>? {
        return null
    }

    @RequestMapping(value = "/cardpack/{id}", method = [RequestMethod.PUT])
    fun createCard(@RequestBody userDoc: Document, @PathVariable id: String): ResponseEntity<Card>? {
        // TODO - Allow single card or array
        return null
    }

    @RequestMapping(value = "/card/{id}", method = [RequestMethod.DELETE])
    fun deleteCard(@PathVariable id: String): ResponseEntity<*> {
        // TODO - Delete card
        return ResponseEntity.ok(null)
    }

    @RequestMapping(value = "/cardpack/{id}/cards", method = [RequestMethod.GET])
    fun getCards(@PathVariable id: String): ResponseEntity<List<Card>> {
        return ResponseEntity.ok(Cardpack.get(ObjectId(id)).getCards())
    }

    @RequestMapping(value = "/cardpack/{cardpackId}/card/{cardId}", method = [RequestMethod.PATCH])
    fun patchCard(@RequestBody patchDoc: List<Document>, @PathVariable cardpackId: String, @PathVariable cardId: String): ResponseEntity<*>? {
        return null
    }

    @RequestMapping(value = "/cardpack/{cardpackId}/card/{cardId}", method = [RequestMethod.DELETE])
    fun deleteCard(@PathVariable cardpackId: String, @PathVariable cardId: String): ResponseEntity<*>? {
        return null
    }
}
