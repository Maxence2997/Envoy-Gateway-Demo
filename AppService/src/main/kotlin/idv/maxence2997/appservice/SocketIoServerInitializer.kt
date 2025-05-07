package idv.maxence2997.appservice

import com.corundumstudio.socketio.SocketIOServer
import com.corundumstudio.socketio.listener.DataListener
import java.time.LocalDateTime
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Component


@Component
@DependsOn("socketServer") // 確保 socketServer 先建立
class SocketIoServerInitializer(
    private val server: SocketIOServer,
) : InitializingBean,
    DisposableBean {
    override fun afterPropertiesSet() {
        server.addEventListener<String>(
            "req-testing-envoy-gateway-socketio",
            String::class.java,
            DataListener { client, data, ackSender ->
                // 1. 拿到原始的 HTTP headers
                val headers = client.handshakeData.httpHeaders

                val token = headers.get("X-Auth-Token")
                    ?: error("X-Auth-Token header not found")
                val userId = headers.get("X-User-Id")
                    ?: error("X-User-Id header not found")
                val orgId = headers.get("X-Org-Id")
                    ?: error("X-Org-Id header not found")
                val username = headers.get("X-User-Username") ?: ""

                println("Received message: $data from userId=$userId, token=$token")

                client.sendEvent(
                    "resp-testing-envoy-gateway-socketio",
                    "Hello $username($userId) from org $orgId, token=$token at ${LocalDateTime.now()} , testing envoy-gateway for Socket.IO"
                )
            })
        server.start()
    }

    override fun destroy() {
        server.stop()
    }
}