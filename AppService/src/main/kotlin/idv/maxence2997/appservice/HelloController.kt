package idv.maxence2997.appservice

import jakarta.servlet.http.HttpServletRequest
import java.time.LocalDateTime
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HelloController {

    @GetMapping("/hello")
    fun hello(request: HttpServletRequest): String {
        val token = request.getHeader("x-auth-token") ?: error("x-auth-token header not found")
        val userId = request.getHeader("x-user-id") ?: error("x-user-id header not found")
        val orgId = request.getHeader("x-org-id") ?: error("x-org-id header not found")
        val username = request.getHeader("x-user-username") ?: ""

        return "Hello $username($userId) from org $orgId, token=$token at ${LocalDateTime.now()}, testing envoy-gateway for REST API"
    }
}