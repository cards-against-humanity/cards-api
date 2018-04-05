package route.card.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

interface BlackCardModel {
    val id: String @JsonProperty(value = "id") get
    val text: String @JsonProperty(value = "text") get
    val answerFields: Int @JsonProperty(value = "answerFields") get
    val cardpackId: String @JsonProperty(value = "cardpackId") get
    @JsonIgnore fun setText(text: String): BlackCardModel
}