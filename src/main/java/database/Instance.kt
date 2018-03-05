package database

import com.mongodb.client.MongoDatabase
import org.bson.Document

object Instance {
    private var mongoInstance: MongoDatabase? = null

    var mongo: MongoDatabase
        get() {
            if (mongoInstance == null) {
                throw NullPointerException("Database instance has not been set")
            }
            return mongoInstance!!
        }
        set(db) {
            mongoInstance = db
        }

    fun resetMongo() {
        mongoInstance!!.listCollectionNames().forEach{ collection -> mongo.getCollection(collection).deleteMany(Document()) }
    }
}
