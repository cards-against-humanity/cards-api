package route.card.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

interface CardModel {
    val id: String @JsonProperty(value = "id") get
    val text: String @JsonProperty(value = "text") get
    val cardpackId: String @JsonProperty(value = "cardpackId") get
    @JsonIgnore fun setText(text: String): CardModel
}