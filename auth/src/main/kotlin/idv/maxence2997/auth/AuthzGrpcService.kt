package idv.maxence2997.auth

import com.google.rpc.Code
import com.google.rpc.Status
import io.envoyproxy.envoy.config.core.v3.HeaderValue
import io.envoyproxy.envoy.config.core.v3.HeaderValueOption
import io.envoyproxy.envoy.service.auth.v3.AuthorizationGrpc
import io.envoyproxy.envoy.service.auth.v3.CheckRequest
import io.envoyproxy.envoy.service.auth.v3.CheckResponse
import io.envoyproxy.envoy.service.auth.v3.DeniedHttpResponse
import io.envoyproxy.envoy.service.auth.v3.OkHttpResponse
import io.envoyproxy.envoy.type.v3.HttpStatus
import io.envoyproxy.envoy.type.v3.StatusCode
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService
import org.redisson.api.RedissonClient
import java.util.UUID

@GrpcService
class AuthzGrpcService(
    private val redissonClient: RedissonClient,
) : AuthorizationGrpc.AuthorizationImplBase() {
    companion object {
        const val MAP_KEY = "auth_token_user_map"
    }

    override fun check(
        request: CheckRequest,
        responseObserver: StreamObserver<CheckResponse>,
    ) {
        val headers = request.attributes.request.http.headersMap
        val token =
            headers["x-auth-token"].takeUnless { it.isNullOrBlank() }?.let { UUID.fromString(it) } ?: run {
                responseObserver.onNext(
                    CheckResponse.newBuilder()
                        .setStatus(Status.newBuilder().setCode(Code.UNAUTHENTICATED_VALUE))
                        .setDeniedResponse(
                            DeniedHttpResponse.newBuilder()
                                .setStatus(HttpStatus.newBuilder().setCode(StatusCode.Unauthorized))
                                .setBody("Missing token"),
                        ).build(),
                )
                responseObserver.onCompleted()
                return
            }

        val mapCache = redissonClient.getMapCache<UUID, UserBasicInfo>(MAP_KEY)

        val userBasicInfo =
            mapCache.getOrElse(token) {
                responseObserver.onNext(
                    CheckResponse.newBuilder()
                        .setStatus(Status.newBuilder().setCode(Code.PERMISSION_DENIED_VALUE))
                        .setDeniedResponse(
                            DeniedHttpResponse.newBuilder()
                                .setStatus(HttpStatus.newBuilder().setCode(StatusCode.Forbidden))
                                .setBody("Invalid token"),
                        ).build(),
                )
                responseObserver.onCompleted()
                return
            }

        // 回傳 OK 並注入 header
        val okHttp =
            OkHttpResponse.newBuilder()
                .addHeaders(
                    HeaderValueOption.newBuilder()
                        .setHeader(
                            HeaderValue.newBuilder()
                                .setKey("x-user-id")
                                .setValue(userBasicInfo.userId.toString()),
                        ),
                )
                .addHeaders(
                    HeaderValueOption.newBuilder()
                        .setHeader(
                            HeaderValue.newBuilder()
                                .setKey("x-org-id")
                                .setValue(userBasicInfo.orgId.toString()),
                        ),
                )
                .addHeaders(
                    HeaderValueOption.newBuilder()
                        .setHeader(
                            HeaderValue.newBuilder()
                                .setKey("x-user-username")
                                .setValue(userBasicInfo.username),
                        ),
                )
                .addHeadersToRemove("x-auth-token") // 移除原本的 token header
                .build()

        responseObserver.complete {
            CheckResponse.newBuilder()
                .setStatus(
                    Status.newBuilder()
                        .setCode(Code.OK_VALUE) // 0 = OK
                        .build(),
                )
                .setOkResponse(okHttp)
                .build()
        }
    }
}

inline fun <T> StreamObserver<T>.complete(block: () -> T) {
    this.onNext(block.invoke())
    this.onCompleted()
}
