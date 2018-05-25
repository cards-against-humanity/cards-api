package auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.SignatureVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.auth0.jwt.interfaces.DecodedJWT

class JWTCookieParser(secret: String) : CookieParser {
    private val verifier = JWT.require(Algorithm.HMAC256(secret)).build()

    override fun isValidAdminToken(cookie: String): Boolean {
        val token: DecodedJWT
        try {
            token = verifier.verify(cookie)
        } catch (e: TokenExpiredException) {
            return false
        } catch (e: SignatureVerificationException) {
            return false
        }

        val roleClaim = token.getClaim("role")

        return if (roleClaim.isNull) {
            false
        } else {
            roleClaim.asString() == "admin"
        }
    }

    override fun getUserId(cookie: String): String? {
        val token: DecodedJWT
        try {
            token = verifier.verify(cookie)
        } catch (e: TokenExpiredException) {
            return null
        } catch (e: SignatureVerificationException) {
            return null
        }

        val userIdClaim = token.getClaim("userId")

        return if (userIdClaim.isNull) {
            null
        } else {
            token.getClaim("userId").asString()
        }
    }
}