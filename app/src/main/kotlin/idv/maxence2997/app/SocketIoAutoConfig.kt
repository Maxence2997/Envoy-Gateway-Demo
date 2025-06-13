package idv.maxence2997.app

import com.corundumstudio.socketio.Configuration
import com.corundumstudio.socketio.SocketIOServer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration as SpringConfiguration

@SpringConfiguration
class SocketIoAutoConfig {
    @Bean
    fun socketConfig(
        @Value("\${socket.host}") host: String,
        @Value("\${socket.port}") port: Int,
    ): Configuration =
        Configuration().apply {
            this.hostname = host
            this.port = port
        }

    @Bean
    fun socketServer(cfg: Configuration): SocketIOServer = SocketIOServer(cfg)
}
