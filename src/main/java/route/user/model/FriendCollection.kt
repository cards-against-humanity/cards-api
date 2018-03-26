package route.user.model

interface FriendCollection {
    @Throws(IllegalArgumentException::class) fun addFriend(frienderId: String, friendeeId: String)
    @Throws(IllegalArgumentException::class) fun removeFriend(frienderId: String, friendeeId: String)
    @Throws(IllegalArgumentException::class) fun areFriends(userOneId: String, userTwoId: String): Boolean
    @Throws(IllegalArgumentException::class) fun getFriends(userId: String): List<UserModel>
    @Throws(IllegalArgumentException::class) fun getSentRequests(userId: String): List<UserModel>
    @Throws(IllegalArgumentException::class) fun getReceivedRequests(userId: String): List<UserModel>
}