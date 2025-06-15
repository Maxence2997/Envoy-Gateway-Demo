package idv.maxence2997.socketiomanager

import com.corundumstudio.socketio.SocketIOServer
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.MoreExecutors
import idv.maxence2997.app.grpc.SocketEvent.GrpcHeader
import idv.maxence2997.app.grpc.SocketEvent.HandleEventRequest
import idv.maxence2997.app.grpc.SocketEvent.HandleEventResponse
import idv.maxence2997.app.grpc.SocketEventServiceGrpc
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Component

@Component
@DependsOn("socketServer") // 確保 socketServer 先建立
class SocketIoServerInitializer(
    private val server: SocketIOServer,
    private val grpcInstanceResolver: GrpcInstanceResolver,
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
                    // TODO: Recover from error
                    ?: error("x-User-Id header not found")
            val orgId =
                headers.get("x-org-id")
                    // TODO: Recover from error
                    ?: error("x-Org-Id header not found")
            val username = headers.get("X-User-Username") ?: ""

            println("Received message: $data from userId=$userId, orgId=$orgId, username=$username")

            // 2. 呼叫 gRPC 服務
            val request =
                HandleEventRequest
                    .newBuilder()
                    .apply {
                        this.header =
                            GrpcHeader
                                .newBuilder()
                                .apply {
                                    this.username = username
                                    this.userId = userId
                                    this.orgId = orgId
                                    this.eventName = "req-testing-envoy-gateway-socketio"
                                }.build()
                        this.payload = data
                    }.build()

            val socketEventStub =
                grpcInstanceResolver.resolveStub("App-Service") {
                    SocketEventServiceGrpc.newFutureStub(it)
                }

            val future = socketEventStub.handleEvent(request)

            Futures.addCallback(
                future,
                object : FutureCallback<HandleEventResponse> {
                    override fun onSuccess(result: HandleEventResponse) {
                        // 成功邏輯，可以 log 或進一步處理
                        val message = result.toString()
                        println("gRPC Success: $message")
                        client.sendEvent("resp-testing-envoy-gateway-socketio", message)
                    }

                    override fun onFailure(t: Throwable) {
                        // 錯誤處理
                        val errorMessage = "gRPC Error: ${t.message}"
                        println(errorMessage)
                        client.sendEvent("resp-testing-envoy-gateway-socketio", errorMessage)
                    }
                },
                MoreExecutors.directExecutor(),
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
