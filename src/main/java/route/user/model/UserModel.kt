package route.user.model

import com.fasterxml.jackson.databind.annotation.JsonSerialize

@JsonSerialize(using = UserModelSerializer::class)
interface UserModel {
    val id: String
    val name: String
    val oAuthId: String
    val oAuthProvider: String
    fun setName(name: String): UserModel
}