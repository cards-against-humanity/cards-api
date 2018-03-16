package route.card

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

class JsonCardpack {
    @JsonProperty("name") var name: String? = null @JsonIgnore private set
}