package route.card

import com.fasterxml.jackson.annotation.JsonProperty

data class JsonWhiteCard (
    @JsonProperty("text") val text: String
)