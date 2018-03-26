package route.user.model

interface UserCollection {
    @Throws(IllegalArgumentException::class) fun getUser(id: String): UserModel
    @Throws(IllegalArgumentException::class) fun getUsers(ids: List<String>): List<UserModel>
    @Throws(IllegalArgumentException::class) fun getUser(oAuthId: String, oAuthProvider: String): UserModel
    @Throws(IllegalArgumentException::class) fun createUser(name: String, oAuthId: String, oAuthProvider: String): UserModel
}