package idv.maxence2997.socketiomanager

import com.corundumstudio.socketio.SocketIOServer
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
        server.addEventListener(
            "req-testing-envoy-gateway-socketio",
            String::class.java,
        ) { client, data, ackSender ->
            // 1. 拿到原始的 HTTP headers
            val headers = client.handshakeData.httpHeaders

            val userId =
                headers.get("x-user-id")
                    ?: error("x-User-Id header not found")
            val orgId =
                headers.get("x-org-id")
                    ?: error("x-Org-Id header not found")
            val username = headers.get("X-User-Username") ?: ""

            println("Received message: $data from userId=$userId, orgId=$orgId, username=$username")

            // TODO: 轉發到 app service through grpc with static route config.
            client.sendEvent(
                "resp-testing-envoy-gateway-socketio",
                "Hello $username($userId) from org $orgId at ${LocalDateTime.now()} , testing envoy-gateway for Socket.IO",
            )

            // ACK
            ackSender.sendAckData("ACK: $data")
        }
        server.start()
    }

    override fun destroy() {
        server.stop()
    }
}
