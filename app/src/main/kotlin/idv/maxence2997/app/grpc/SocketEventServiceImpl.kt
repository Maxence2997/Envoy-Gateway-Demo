package idv.maxence2997.app.grpc

import idv.maxence2997.app.proto.SocketEventServiceGrpc
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService
class SocketEventServiceImpl : SocketEventServiceGrpc.SocketEventServiceImplBase()
