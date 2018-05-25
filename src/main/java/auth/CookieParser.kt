package auth

interface CookieParser {
    fun isValidAdminToken(cookie: String): Boolean
    fun getUserId(cookie: String): String?
}