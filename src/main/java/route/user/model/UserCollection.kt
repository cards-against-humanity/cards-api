package route.user.model

interface UserCollection {
    fun getUser(id: String): UserModel
    fun getUsers(ids: List<String>): List<UserModel>
    fun getUser(oAuthId: String, oAuthProvider: String): UserModel
    fun createUser(name: String, oAuthId: String, oAuthProvider: String): UserModel
}