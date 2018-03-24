package database.memorymodel

import org.springframework.stereotype.Service
import route.card.model.CardCollection
import route.user.model.FriendCollection
import route.user.model.UserCollection

@Service
class MemoryCollection(
        private val memoryUserCollection: MemoryUserCollection = MemoryUserCollection(),
        private val memoryFriendCollection: MemoryFriendCollection = MemoryFriendCollection(memoryUserCollection),
        private val memoryCardCollection: MemoryCardCollection = MemoryCardCollection(memoryUserCollection)
) : UserCollection by memoryUserCollection, FriendCollection by memoryFriendCollection, CardCollection by memoryCardCollection