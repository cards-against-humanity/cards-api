package database.memorymodel

import route.user.model.UserCollection
import route.user.model.UserModel

class MemoryUserCollection : UserCollection {
    private val users: MutableMap<String, MemoryUserModel> = HashMap()

    override fun getUser(id: String): UserModel {
        val user = users[id]
        if (user != null) {
            return user
        } else {
            throw Exception("User does not exist with id: $id")
        }
    }

    override fun getUsers(ids: List<String>): List<UserModel> {
        return ids.map { id -> users[id]!! }
    }

    override fun getUser(oAuthId: String, oAuthProvider: String): UserModel {
        val possibleUsers = users.filterValues { user -> user.oAuthId == oAuthId && user.oAuthProvider == oAuthProvider }
        if (possibleUsers.isEmpty()) {
            throw Exception("User does not exist with oAuthId of $oAuthId and oAuthProvider of $oAuthProvider")
        }
        return possibleUsers.toList()[0].second
    }

    override fun createUser(name: String, oAuthId: String, oAuthProvider: String): UserModel {
        if (users.filterValues { user -> user.oAuthId == oAuthId && user.oAuthProvider == oAuthProvider }.isNotEmpty()) {
            throw Exception("User already exists with that oAuth ID and provider")
        }
        val model = MemoryUserModel(users.size.toString(), name, oAuthId, oAuthProvider)
        users[model.id] = model
        return model
    }

    private class MemoryUserModel(override val id: String, override var name: String, val oAuthId: String, val oAuthProvider: String) : UserModel {
        override fun setName(name: String): UserModel {
            this.name = name
            return this
        }
    }
}