package route.user

import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import org.bson.Document
import org.bson.types.ObjectId

import java.util.*

object Friend {
    private val friends = database.Instance.mongo.getCollection("friends")

    init {
        friends.createIndex(Indexes.descending("senderId", "receiverId"), IndexOptions().unique(true))
        friends.createIndex(Indexes.descending("receiverId", "senderId"), IndexOptions().unique(true))
    }

    fun addFriend(friender: User, friendee: User) {
        friends.insertOne(Document("senderId", friender.id).append("receiverId", friendee.id))
    }

    fun removeFriend(friender: User, friendee: User) {
        val items = ArrayList<Document>()
        items.add(Document("senderId", friender.id).append("receiverId", friendee.id))
        items.add(Document("receiverId", friender.id).append("senderId", friendee.id))
        friends.deleteMany(Document("\$or", items))
    }

    fun areFriends(userOne: User, userTwo: User): Boolean {
        val andQuery = ArrayList<Document>()
        andQuery.add(Document("senderId", userOne.id).append("receiverId", userTwo.id))
        andQuery.add(Document("receiverId", userOne.id).append("senderId", userTwo.id))
        return friends.count(Document("\$or", andQuery)) == 2L
    }

    fun getFriends(user: User): List<User> {
        val userIdPairs = getRelatedUserIds(user)
        val userIdMap = HashMap<ObjectId, Int>()
        userIdPairs.forEach { tuple ->
            run {
                val otherUser = tuple.getOtherUserId(user)
                if (!userIdMap.containsKey(otherUser)) {
                    userIdMap[otherUser] = 1
                } else {
                    userIdMap[otherUser] = userIdMap[otherUser]!! + 1
                }
            }
        }

        val friendIds = ArrayList<ObjectId>()
        userIdMap.forEach { id, count ->
            if (count == 2) {
                friendIds.add(id)
            }
        }

        return User.get(friendIds)
    }

    private fun getRequests(user: User, userIdPairs: List<FriendTuple>): Set<ObjectId> {
        val userIdSet = HashSet<ObjectId>()
        userIdPairs.forEach { pair ->
            run {
                val otherUser = pair.getOtherUserId(user)
                if (!userIdSet.contains(otherUser)) {
                    userIdSet.add(otherUser)
                } else {
                    userIdSet.remove(otherUser)
                }
            }
        }
        return userIdSet
    }

    fun getSentRequests(user: User): List<User> {
        val userIdPairs = getRelatedUserIds(user)
        val allRequests = getRequests(user, userIdPairs)
        val sentRequests = ArrayList<ObjectId>()
        userIdPairs.forEach { pair ->
            run {
                val id = pair.receiverId
                if (allRequests.contains(id)) {
                    sentRequests.add(id)
                }
            }
        }
        return User.get(sentRequests)
    }

    fun getReceivedRequests(user: User): List<User> {
        val userIdPairs = getRelatedUserIds(user)
        val allRequests = getRequests(user, userIdPairs)
        val receivedRequests = ArrayList<ObjectId>()
        userIdPairs.forEach { pair ->
            run {
                val id = pair.senderId
                if (allRequests.contains(id)) {
                    receivedRequests.add(id)
                }
            }
        }
        return User.get(receivedRequests)
    }

    private fun getRelatedUserIds(user: User): List<FriendTuple> {
        val andQuery = ArrayList<Document>()
        andQuery.add(Document("senderId", user.id))
        andQuery.add(Document("receiverId", user.id))
        val userIdPairs = ArrayList<FriendTuple>()
        friends.find(Document("\$or", andQuery)).forEach { doc -> userIdPairs.add(FriendTuple(ObjectId(doc["senderId"] as String), ObjectId(doc["receiverId"] as String))) }
        return userIdPairs
    }

    private class FriendTuple(val senderId: ObjectId, val receiverId: ObjectId) {

        fun getOtherUserId(user: User): ObjectId {
            return when {
                senderId.toString() == user.id -> receiverId
                receiverId.toString() == user.id -> senderId
                else -> throw IllegalArgumentException("User specified does not belong to either ID")
            }
        }
    }
}
