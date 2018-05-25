package auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

class JWTCookieParserTest {

    private val secret = "1234567890"
    private val algorithm = Algorithm.HMAC256(secret)
    private val fakeAlgorithm = Algorithm.HMAC256(secret + "1234")

    private var parser = JWTCookieParser(secret)

    @BeforeEach
    fun reset() {
        parser = JWTCookieParser(secret)
    }

    private fun getUserToken(expiration: Date, userId: String, algorithm: Algorithm): String {
        return JWT.create()
                .withExpiresAt(expiration)
                .withClaim("userId", userId)
                .sign(algorithm)
    }

    private fun getUserToken(expiration: Date, userId: String): String {
        return getUserToken(expiration, userId, algorithm)
    }

    private fun getAdminToken(expiration: Date, algorithm: Algorithm): String {
        return JWT.create()
                .withExpiresAt(expiration)
                .withClaim("role", "admin")
                .sign(algorithm)
    }

    private fun getAdminToken(expiration: Date): String {
        return getAdminToken(expiration, algorithm)
    }

    private fun getGenericToken(expiration: Date, algorithm: Algorithm): String {
        return JWT.create()
                .withExpiresAt(expiration)
                .sign(algorithm)
    }

    private fun getGenericToken(expiration: Date): String {
        return getGenericToken(expiration, algorithm)
    }

    @Test
    fun isValidUserToken() {
        val userId = "user_id"
        val token = getUserToken(Date(System.currentTimeMillis() + 1000), userId)

        assertEquals(userId, parser.getUserId(token))
    }

    @Test
    fun isInvalidUserTokenIfExpired() {
        val userId = "user_id"
        val token = getUserToken(Date(System.currentTimeMillis() - 1000), userId)

        assertEquals(null, parser.getUserId(token))
    }

    @Test
    fun isInvalidUserTokenIfWrongSignature() {
        val userId = "user_id"
        val token = getUserToken(Date(System.currentTimeMillis() + 1000), userId, fakeAlgorithm)

        assertEquals(null, parser.getUserId(token))
    }

    @Test
    fun isInvalidUserTokenIfNoUserIdClaim() {
        val token = getGenericToken(Date(System.currentTimeMillis() + 1000))

        assertEquals(null, parser.getUserId(token))
    }


    @Test
    fun isValidAdminToken() {
        val token = getAdminToken(Date(System.currentTimeMillis() + 1000))

        assertEquals(true, parser.isValidAdminToken(token))
    }

    @Test
    fun isInvalidAdminTokenIfExpired() {
        val token = getAdminToken(Date(System.currentTimeMillis() - 1000))

        assertEquals(false, parser.isValidAdminToken(token))
    }

    @Test
    fun isInvalidAdminTokenIfWrongSignature() {
        val token = getAdminToken(Date(System.currentTimeMillis() + 1000), fakeAlgorithm)

        assertEquals(false, parser.isValidAdminToken(token))
    }

    @Test
    fun isInvalidAdminTokenIfNoRoleClaim() {
        val token = getGenericToken(Date(System.currentTimeMillis() + 1000))

        assertEquals(false, parser.isValidAdminToken(token))
    }

    @Test
    fun isInvalidAdminTokenIfIncorrectRoleClaim() {
        val token = JWT.create()
                .withExpiresAt(Date(System.currentTimeMillis() + 1000))
                .withClaim("role", "fake_role")
                .sign(algorithm)

        assertEquals(false, parser.isValidAdminToken(token))
    }
}