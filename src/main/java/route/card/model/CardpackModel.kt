package route.card.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import route.user.model.UserModel
import java.util.*

interface CardpackModel {
    val id: String @JsonProperty(value = "id") get
    val name: String @JsonProperty(value = "name") get
    val owner: UserModel @JsonProperty(value = "owner") get
    val whiteCards: List<WhiteCardModel> @JsonProperty(value = "whiteCards") get
    val blackCards: List<BlackCardModel> @JsonProperty(value = "blackCards") get
    val createdAt: Date @JsonProperty(value = "createdAt") get
    @JsonIgnore fun setName(name: String): CardpackModel
}