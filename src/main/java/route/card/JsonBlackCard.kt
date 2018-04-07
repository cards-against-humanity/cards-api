package route.card

import com.fasterxml.jackson.annotation.JsonProperty

data class JsonBlackCard (
        @JsonProperty("text") val text: String,
        @JsonProperty("answerFields") var answerFields: Int
)