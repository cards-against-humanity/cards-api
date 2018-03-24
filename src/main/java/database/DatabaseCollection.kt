package database

import route.card.model.CardCollection
import route.user.model.FriendCollection
import route.user.model.UserCollection

interface DatabaseCollection : UserCollection, FriendCollection, CardCollection