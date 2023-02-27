package ${modulePackageName}.config;

import static ${modulePackageName}.ModuleOption.*;
import ${modulePackageName}.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.beans.factory.annotation.*;
import javax.annotation.Resource;

@Slf4j
@Configuration(PLUGIN_PREFIX + "ModuleWebSocketConfigurer")
@ConditionalOnProperty(prefix = PLUGIN_PREFIX, name = "ModuleWebSocketConfigurer", matchIfMissing = true)
@EnableWebSocketMessageBroker
public class ModuleWebSocketConfigurer
        implements WebSocketMessageBrokerConfigurer {

    @Autowired
    TaskScheduler messageBrokerTaskScheduler;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        //endPoint 注册协议节点,并映射指定的URl点对点
        //允许使用socketJs方式访问，访问点为webSocketServer，允许跨域
        registry.addEndpoint(WS_PATH)
                .setAllowedOriginPatterns("*")
                .withSockJS(); //支持socketJs
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        registry.setPreservePublishOrder(true);

        registry.enableSimpleBroker(BASE_PATH + "queue/", BASE_PATH + "topic/")
                .setHeartbeatValue(new long[]{10000, 20000})
                .setTaskScheduler(this.messageBrokerTaskScheduler);

        //send命令时需要带上/app前缀
        // 全局使用的消息前缀（客户端订阅路径上会体现出来）
        registry.setApplicationDestinationPrefixes(BASE_PATH);

    }
}
