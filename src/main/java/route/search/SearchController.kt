package route.search

import elasticsearch.ElasticAutoCompleter
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
class SearchController(private val searcher: ElasticSearcher, private val autoCompleter: ElasticAutoCompleter) {

    @RequestMapping(value = "/user/search", method = [RequestMethod.GET])
    @ApiOperation(value = "Get list of users by name")
    @ApiResponses(
            ApiResponse(code = 200, message = "User search succeeded"),
            ApiResponse(code = 400, message = "Missing search query")
    )
    fun searchUsers(@RequestParam(value = "query", required = true) query: String): ResponseEntity<List<UserModel>> {
        return ResponseEntity.ok(searcher.searchUsers(query))
    }

    @RequestMapping(value = "/cardpack/search", method = [RequestMethod.GET])
    @ApiOperation(value = "Get list of cardpacks by name")
    @ApiResponses(
            ApiResponse(code = 200, message = "User search succeeded"),
            ApiResponse(code = 400, message = "Missing search query")
    )
    fun searchCardpacks(@RequestParam(value = "query", required = true) query: String): ResponseEntity<List<CardpackModel>> {
        return ResponseEntity.ok(searcher.searchCardpacks(query))
    }

    @RequestMapping(value = "/user/search/autocomplete", method = [RequestMethod.GET])
    @ApiOperation(value = "Get list of users by name")
    @ApiResponses(
            ApiResponse(code = 200, message = "User search succeeded"),
            ApiResponse(code = 400, message = "Missing search query")
    )
    fun userAutocomplete(@RequestParam(value = "query", required = true) query: String): ResponseEntity<List<String>> {
        return ResponseEntity.ok(autoCompleter.autoCompleteUserSearch(query))
    }

    @RequestMapping(value = "/cardpack/search/autocomplete", method = [RequestMethod.GET])
    @ApiOperation(value = "Get list of cardpacks by name")
    @ApiResponses(
            ApiResponse(code = 200, message = "User search succeeded"),
            ApiResponse(code = 400, message = "Missing search query")
    )
    fun cardpackAutocomplete(@RequestParam(value = "query", required = true) query: String): ResponseEntity<List<String>> {
        return ResponseEntity.ok(autoCompleter.autoCompleteUserSearch(query))
    }

}