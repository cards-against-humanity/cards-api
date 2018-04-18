package elasticsearch

import route.card.model.CardpackModel
import route.user.model.UserModel

interface ElasticIndexer {
    fun indexUser(user: UserModel)
    fun indexCardpack(cardpack: CardpackModel)
    fun unindexCardpack(cardpackId: String)
}

interface ElasticSearcher {
    fun searchUsers(query: String): List<UserModel>
    fun searchCardpacks(query: String): List<CardpackModel>
}

interface ElasticAutoCompleter {
    fun autoCompleteUserSearch(query: String): List<String>
    fun autoCompleteCardpackSearch(query: String): List<String>
}