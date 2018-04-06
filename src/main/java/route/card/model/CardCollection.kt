package route.card.model

import route.card.JsonBlackCard
import route.card.JsonWhiteCard

interface CardCollection {
    fun createCardpack(name: String, userId: String): CardpackModel
    fun deleteCardpack(id: String)
    fun getCardpack(id: String): CardpackModel
    fun getCardpacks(userId: String): List<CardpackModel>
    fun createWhiteCard(card: JsonWhiteCard, cardpackId: String): WhiteCardModel
    fun createWhiteCards(cards: List<JsonWhiteCard>, cardpackId: String): List<WhiteCardModel>
    fun createBlackCard(card: JsonBlackCard, cardpackId: String): BlackCardModel
    fun createBlackCards(cards: List<JsonBlackCard>, cardpackId: String): List<BlackCardModel>
    fun deleteWhiteCard(id: String)
    fun deleteWhiteCards(ids: List<String>)
    fun deleteBlackCard(id: String)
    fun deleteBlackCards(ids: List<String>)
}