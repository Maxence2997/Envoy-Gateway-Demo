package idv.maxence2997.authservice

import org.redisson.api.RedissonClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HelloController(
    private val redissonClient: RedissonClient
) {

    @GetMapping("/hello")
    fun hello(): String {
        // 使用 Redisson 操作 Redis 中的鍵值
        val bucket = redissonClient.getBucket<String>("greeting")
        bucket.setIfAbsent("Hello from Redis!")  // 只有鍵不存在時才設定
        return bucket.get() ?: "No greeting found"
    }
}