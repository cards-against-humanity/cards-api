package route.user.model

import com.fasterxml.jackson.annotation.JsonProperty

interface UserModel {
    val id: String            @JsonProperty(value = "id") get
    val name: String          @JsonProperty(value = "name") get
    val oAuthId: String       @JsonProperty(value = "oAuthId") get
    val oAuthProvider: String @JsonProperty(value = "oAuthProvider") get
    fun setName(name: String): UserModel
}