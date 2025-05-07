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
                println("Received message: $data")
                client.sendEvent(
                    "resp-testing-envoy-gateway-socketio",
                    "Server says hi! at ${LocalDateTime.now()} , testing envoy-gateway for Socket.IO"
                )
            })
        server.start()
    }

    override fun destroy() {
        server.stop()
    }
}