package route.search

import elasticsearch.ElasticSearcher
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import route.card.model.CardpackModel
import route.user.model.UserModel

@RestController
class SearchController(private val searcher: ElasticSearcher) {

    @RequestMapping(value = "/search/users", method = [RequestMethod.GET])
    @ApiOperation(value = "Get list of users by name")
    @ApiResponses(
            ApiResponse(code = 200, message = "User search succeeded"),
            ApiResponse(code = 400, message = "Missing search query")
    )
    fun searchUsers(@RequestParam(value = "query", required = true) query: String): ResponseEntity<List<UserModel>> {
        return ResponseEntity.ok(searcher.searchUsers(query))
    }

    @RequestMapping(value = "/search/cardpacks", method = [RequestMethod.GET])
    @ApiOperation(value = "Get list of cardpacks by name")
    @ApiResponses(
            ApiResponse(code = 200, message = "User search succeeded"),
            ApiResponse(code = 400, message = "Missing search query")
    )
    fun searchCardpacks(@RequestParam(value = "query", required = true) query: String): ResponseEntity<List<CardpackModel>> {
        return ResponseEntity.ok(searcher.searchCardpacks(query))
    }
}