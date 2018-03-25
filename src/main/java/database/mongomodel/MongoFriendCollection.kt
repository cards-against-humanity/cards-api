package database.mongomodel

import com.mongodb.client.MongoCollection
import com.mongodb.client.model.IndexOptions
import org.bson.Document
import route.user.model.FriendCollection
import route.user.model.UserCollection
import route.user.model.UserModel

class MongoFriendCollection(val mongoCollection: MongoCollection<Document>, val userCollection: UserCollection) : FriendCollection {
    init {
        mongoCollection.createIndex(Document("senderId", 1).append("receiverId", 1), IndexOptions().unique(true))
    }

    override fun addFriend(frienderId: String, friendeeId: String) {
        if (frienderId == friendeeId) {
            throw Exception("Cannot add yourself as a friend")
        }
        try {
            mongoCollection.insertOne(Document("senderId", frienderId).append("receiverId", friendeeId))
        } catch (e: Exception) {
            throw Exception("Cannot add someone you have already added")
        }
    }

    override fun removeFriend(frienderId: String, friendeeId: String) {
        val items = ArrayList<Document>()
        items.add(Document("senderId", frienderId).append("receiverId", friendeeId))
        items.add(Document("receiverId", frienderId).append("senderId", friendeeId))
        val deletedCount = mongoCollection.deleteMany(Document("\$or", items)).deletedCount
        if (deletedCount == 0L) {
            throw Exception("Users are not friends")
        }
    }

    override fun areFriends(userOneId: String, userTwoId: String): Boolean {
        val andQuery = ArrayList<Document>()
        andQuery.add(Document("senderId", userOneId).append("receiverId", userTwoId))
        andQuery.add(Document("receiverId", userOneId).append("senderId", userTwoId))
        return mongoCollection.count(Document("\$or", andQuery)) == 2L
    }

    override fun getFriends(userId: String): List<UserModel> {
        val friendData = getFriendDataForUser(userId)
        val friendIds: MutableList<String> = ArrayList()
        friendData.allOtherUserIds.forEach { id ->
            run {
                if (friendData.friendRequests.contains(Pair(userId, id)) && friendData.friendRequests.contains(Pair(id, userId))) {
                    friendIds.add(id)
                }
            }
        }
        return userCollection.getUsers(friendIds)
    }

    override fun getSentRequests(userId: String): List<UserModel> {
        val friendData = getFriendDataForUser(userId)
        val friendIds: MutableList<String> = ArrayList()
        friendData.allOtherUserIds.forEach { id ->
            run {
                if (friendData.friendRequests.contains(Pair(userId, id)) && !friendData.friendRequests.contains(Pair(id, userId))) {
                    friendIds.add(id)
                }
            }
        }
        return userCollection.getUsers(friendIds)
    }

    override fun getReceivedRequests(userId: String): List<UserModel> {
        val friendData = getFriendDataForUser(userId)
        val friendIds: MutableList<String> = ArrayList()
        friendData.allOtherUserIds.forEach { id ->
            run {
                if (!friendData.friendRequests.contains(Pair(userId, id)) && friendData.friendRequests.contains(Pair(id, userId))) {
                    friendIds.add(id)
                }
            }
        }
        return userCollection.getUsers(friendIds)
    }

    private fun getFriendDataForUser(userId: String): FriendData {
        val idPairSet = mongoCollection.find(Document("\$or", listOf(Document("senderId", userId), Document("receiverId", userId)))).map { doc -> Pair(doc["senderId"] as String, doc["receiverId"] as String) }.toSet()
        val idSet: MutableSet<String> = HashSet()
        idPairSet.forEach { idPair -> idSet.add(idPair.first); idSet.add(idPair.second) }
        idSet.remove(userId)
        return FriendData(idPairSet, idSet)
    }

    private data class FriendData(val friendRequests: Set<Pair<String, String>>, val allOtherUserIds: Set<String>)
}