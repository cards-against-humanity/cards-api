package route.card.model

import route.card.JsonBlackCard
import route.card.JsonWhiteCard

interface CardCollection {
    fun createCardpack(name: String, userId: String): CardpackModel
    fun deleteCardpack(id: String)
    fun getCardpack(id: String): CardpackModel
    fun getCardpacks(userId: String): List<CardpackModel>
    fun createWhiteCard(card: JsonWhiteCard): WhiteCardModel
    fun createWhiteCards(cards: List<JsonWhiteCard>): List<WhiteCardModel>
    fun createBlackCard(card: JsonBlackCard): BlackCardModel
    fun createBlackCards(cards: List<JsonBlackCard>): List<BlackCardModel>
    fun deleteWhiteCard(id: String)
    fun deleteWhiteCards(ids: List<String>)
    fun deleteBlackCard(id: String)
    fun deleteBlackCards(ids: List<String>)
}