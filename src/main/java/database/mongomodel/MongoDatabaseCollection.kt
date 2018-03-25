package database.mongomodel


import com.mongodb.client.MongoDatabase
import database.DatabaseCollection
import route.card.model.CardCollection
import route.user.model.FriendCollection
import route.user.model.UserCollection

class MongoDatabaseCollection(
        private val mongoDatabase: MongoDatabase,
        private val mongoUserCollection: MongoUserCollection = MongoUserCollection(mongoDatabase.getCollection("user")),
        private val mongoFriendCollection: MongoFriendCollection = MongoFriendCollection(mongoDatabase.getCollection("friend"), mongoUserCollection),
        private val mongoCardCollection: MongoCardCollection = MongoCardCollection(mongoDatabase.getCollection("cardpack"), mongoDatabase.getCollection("card"), mongoUserCollection)
) : UserCollection by mongoUserCollection, FriendCollection by mongoFriendCollection, CardCollection by mongoCardCollection, DatabaseCollection