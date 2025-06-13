package idv.maxence2997.app

import com.corundumstudio.socketio.SocketIOServer
import com.corundumstudio.socketio.listener.DataListener
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Component
import java.time.LocalDateTime

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

                val userId =
                    headers.get("x-user-id")
                        ?: error("X-User-Id header not found")
                val orgId =
                    headers.get("x-org-id")
                        ?: error("X-Org-Id header not found")
                val username = headers.get("X-User-Username") ?: ""

                println("Received message: $data from userId=$userId, orgId=$orgId, username=$username")

                client.sendEvent(
                    "resp-testing-envoy-gateway-socketio",
                    "Hello $username($userId) from org $orgId at ${LocalDateTime.now()} , testing envoy-gateway for Socket.IO",
                )
            },
        )
        server.start()
    }

    override fun destroy() {
        server.stop()
    }
}
