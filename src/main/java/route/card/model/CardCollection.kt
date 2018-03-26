package route.card.model

interface CardCollection {
    fun createCardpack(name: String, userId: String): CardpackModel
    fun createCard(text: String, cardpackId: String): CardModel
    fun createCards(textList: List<String>, cardpackId: String): List<CardModel>
    fun deleteCardpack(id: String)
    fun deleteCard(id: String)
    fun deleteCards(ids: List<String>)
    fun getCardpack(id: String): CardpackModel
    fun getCardpacks(userId: String): List<CardpackModel>
    fun getCard(id: String): CardModel
}