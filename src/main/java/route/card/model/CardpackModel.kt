package route.card.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

interface CardpackModel {
    val id: String @JsonProperty(value = "id") get
    val name: String @JsonProperty(value = "name") get
    val ownerId: String @JsonProperty(value = "ownerId") get
    @JsonIgnore fun setName(name: String): CardpackModel
    @JsonIgnore fun getCards(): List<CardModel>
}