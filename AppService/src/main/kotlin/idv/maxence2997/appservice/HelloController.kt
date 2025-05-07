package idv.maxence2997.appservice

import java.time.LocalDateTime
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HelloController{

    @GetMapping("/hello")
    fun hello(): String {

        return "reply Server says hi! at ${LocalDateTime.now()}, testing envoy-gateway for REST API"
    }
}