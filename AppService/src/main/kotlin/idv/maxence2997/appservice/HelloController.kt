package idv.maxence2997.appservice

import jakarta.servlet.http.HttpServletRequest
import java.time.LocalDateTime
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HelloController {

    @GetMapping("/hello")
    fun hello(request: HttpServletRequest): String {
        val token = request.getHeader("X-Auth-Token") ?: error("X-Auth-Token header not found")
        val userId = request.getHeader("X-User-Id") ?: error("X-User-Id header not found")
        val orgId = request.getHeader("X-Org-Id") ?: error("X-Org-Id header not found")
        val username = request.getHeader("X-User-Username") ?: ""

        return "Hello $username($userId) from org $orgId, token=$token at ${LocalDateTime.now()}, testing envoy-gateway for REST API"
    }
}