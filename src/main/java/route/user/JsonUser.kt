package route.user

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

class JsonUser {
    @JsonProperty("name")          var name:          String? = null @JsonIgnore private set
    @JsonProperty("oAuthId")       var oAuthId:       String? = null @JsonIgnore private set
    @JsonProperty("oAuthProvider") var oAuthProvider: String? = null @JsonIgnore private set
}