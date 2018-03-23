package route.user.memorymodel

import route.user.model.FriendCollection
import route.user.model.UserCollection
import route.user.model.UserModel

class MemoryFriendCollection(private val userCollection: UserCollection) : FriendCollection {
    private val friends = HashMap<String, MutableSet<String>>() as MutableMap<String, MutableSet<String>>

    override fun addFriend(frienderId: String, friendeeId: String) {
        if (friends[frienderId] == null) {
            friends[frienderId] = HashSet()
        }

        if (friends[frienderId]!!.contains(friendeeId) || frienderId == friendeeId) {
            throw Exception()
        } else {
            friends[frienderId]!!.add(friendeeId)
        }
    }

    override fun removeFriend(frienderId: String, friendeeId: String) {
        if (friends[frienderId] == null || !friends[frienderId]!!.contains(friendeeId)) {
            throw Exception()
        }
        friends[frienderId]!!.remove(friendeeId)
    }

    override fun areFriends(userOneId: String, userTwoId: String): Boolean {
        return friends[userOneId] != null && friends[userOneId]!!.contains(userTwoId) && friends[userTwoId] != null && friends[userTwoId]!!.contains(userOneId)
    }

    override fun getFriends(userId: String): List<UserModel> {
        if (friends[userId] == null) {
            return ArrayList()
        }
        return friends[userId]!!.filter { id -> friends[id] != null && friends[id]!!.contains(userId) }.map { id -> userCollection.getUser(id) }
    }

    override fun getSentRequests(userId: String): List<UserModel> {
        if (friends[userId] == null) {
            return ArrayList()
        }
        return friends[userId]!!.filter { id -> friends[id] == null || !friends[id]!!.contains(userId) }.map { id -> userCollection.getUser(id) }
    }

    override fun getReceivedRequests(userId: String): List<UserModel> {
        val friendRequests = ArrayList<UserModel>()
        friends.keys.forEach { id ->
            run {
                if (id != userId) {
                    if (friends[id] != null && friends[id]!!.contains(userId)) {
                        if (friends[userId] == null || !friends[userId]!!.contains(id)) {
                            friendRequests.add(userCollection.getUser(id))
                        }
                    }
                }
            }
        }
        return friendRequests
    }

    // TODO - Remove this method
    fun clear() {
        friends.clear()
    }
}