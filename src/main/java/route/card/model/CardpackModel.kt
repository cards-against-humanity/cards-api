package route.card.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import route.user.model.UserModel

interface CardpackModel {
    val id: String @JsonProperty(value = "id") get
    val name: String @JsonProperty(value = "name") get
    val owner: UserModel @JsonProperty(value = "owner") get
    @JsonIgnore fun setName(name: String): CardpackModel
    @JsonIgnore fun getCards(): List<CardModel>
}