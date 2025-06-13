package idv.maxence2997.socketiomanager

import com.corundumstudio.socketio.SocketIOServer
import idv.maxence2997.app.grpc.SocketEvent.GrpcHeader
import idv.maxence2997.app.grpc.SocketEvent.HandleEventRequest
import idv.maxence2997.app.grpc.SocketEventServiceGrpc
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Component

@Component
@DependsOn("socketServer") // 確保 socketServer 先建立
class SocketIoServerInitializer(
    private val server: SocketIOServer,
    @GrpcClient("appService")
    private val socketEventStub: SocketEventServiceGrpc.SocketEventServiceBlockingStub
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

            // 2. 呼叫 gRPC 服務
            socketEventStub.handleEvent(
                HandleEventRequest.newBuilder().apply {
                    this.header = GrpcHeader.newBuilder().apply {
                        this.username = username
                        this.userId = userId
                        this.orgId = orgId
                        this.eventName = "req-testing-envoy-gateway-socketio"
                    }.build()
                    this.payload = data
                }.build()
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
