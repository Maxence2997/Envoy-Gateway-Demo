package idv.maxence2997.socketiomanager

import com.github.benmanes.caffeine.cache.Caffeine
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import org.springframework.cloud.client.ServiceInstance
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class GrpcInstanceResolver(
    // DiscoveryClient 會每次都去詢問 服務註冊中心，會有性能問題，改用 LoadBalancerClient，預設用 Round Robin policy
//    private val discoveryClient: DiscoveryClient,
    private val loadBalancerClient: LoadBalancerClient,
) {
    private val grpcChannelCache =
        Caffeine
            .newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(100)
            .build<String, ManagedChannel> { serviceName ->
                createChannel(serviceName)
            }

    fun <T> resolveStub(
        serviceName: String,
        factory: (ManagedChannel) -> T,
    ): T {
        val channel = resolveChannel(serviceName)
        return factory(channel)
    }

    private fun resolveChannel(serviceName: String): ManagedChannel = grpcChannelCache.get(serviceName)

    private fun getInstance(serviceName: String): ServiceInstance? = loadBalancerClient.choose(serviceName)

    private fun createChannel(serviceName: String): ManagedChannel {
        val instance =
            getInstance(serviceName) ?: throw IllegalStateException("No instance found for service: $serviceName")

        println("instances: $instance")
        val host = instance.host
        val port =
            instance.metadata["gRPC_port"]?.toInt()
                ?: throw IllegalStateException("gRPC_port metadata not found for service: $serviceName")

        val channel =
            ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext() // 生產環境建議加 TLS
                .build()
        println("Resolved gRPC channel for service $serviceName at $host:$port")
        return channel
    }
}
