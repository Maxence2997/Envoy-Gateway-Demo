package idv.maxence2997.app.grpc

import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService
class SocketEventServiceImpl : SocketEventServiceGrpc.SocketEventServiceImplBase() {
    override fun handleEvent(
        request: SocketEvent.HandleEventRequest,
        responseObserver: StreamObserver<SocketEvent.HandleEventResponse>
    ) {
        println("Received event: ${request.header} with data: ${request.payload}")
        // 處理事件邏輯

        responseObserver.onNext(
            SocketEvent.HandleEventResponse.newBuilder()
                .apply {
                    this.success = true
                    this.replyMessage = "Event handled successfully"
                }
                .build()
        )
        responseObserver.onCompleted()
    }
}
