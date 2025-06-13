package idv.maxence2997.app

import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
class HelloController {
    @GetMapping("/hello")
    fun hello(request: HttpServletRequest): String {
        val userId = request.getHeader("x-user-id") ?: error("x-user-id header not found")
        val orgId = request.getHeader("x-org-id") ?: error("x-org-id header not found")
        val username = request.getHeader("x-user-username") ?: ""

        return "Hello $username($userId) from org $orgId at ${LocalDateTime.now()}, testing envoy-gateway for REST API"
    }
}
