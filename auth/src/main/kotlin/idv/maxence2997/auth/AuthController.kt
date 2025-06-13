package idv.maxence2997.auth

import org.redisson.api.RedissonClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID
import java.util.concurrent.TimeUnit

@RestController
@RequestMapping("/auth")
class AuthController(
    private val redissonClient: RedissonClient,
) {
    companion object {
        const val MAP_KEY = "auth_token_user_map"
    }

    @PostMapping("/login")
    fun login(
        @RequestBody request: LoginRequest,
    ): UUID {
        if (request.username.isEmpty() || request.password.isEmpty()) {
            throw IllegalArgumentException("Username and password must not be empty")
        }

        // 假設這裡的密碼是明文存儲的，實際上應該使用加密存儲
        if (request.username != "admin" || request.password != "password") {
            throw IllegalArgumentException("Invalid username or password")
        }

        // 產生UUID作為 token，存入 UUID -> User Basic Info 在 Redis 中，回傳UUID

        val token = UUID.randomUUID()
        val mapCache = redissonClient.getMapCache<UUID, UserBasicInfo>(MAP_KEY)

        val userBasicInfo =
            UserBasicInfo(
                userId = UUID.fromString("d3b0a9c2-5c1e-4cd1-9114-5a733f18f655"),
                orgId = UUID.fromString("9a7e5e3c-bdb0-4de5-9b56-df81f0194b26"),
                username = request.username,
            )

        mapCache.put(token, userBasicInfo, 60, TimeUnit.MINUTES)

        return token
    }
}

data class LoginRequest(
    val username: String,
    val password: String,
)

data class UserBasicInfo(
    val userId: UUID,
    val orgId: UUID,
    val username: String,
)
