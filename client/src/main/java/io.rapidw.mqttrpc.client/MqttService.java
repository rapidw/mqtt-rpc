package io.rapidw.mqttrpc.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.rapidw.mqtt.client.v3_1_1.*;
import io.rapidw.mqtt.client.v3_1_1.handler.*;
import io.rapidw.mqtt.codec.v3_1_1.MqttV311QosLevel;
import io.rapidw.mqtt.codec.v3_1_1.MqttV311TopicAndQosLevel;
import io.rapidw.mqttrpc.mqtt.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class MqttService {
    private MqttV311Client client;
    private MqttConnection connection;
    private byte[] heartbeatByteArray;
    private final HashedWheelTimer timer = new HashedWheelTimer(1, TimeUnit.SECONDS);
    private Timeout registerTimeout;

    private final AppConfig appConfig;
    private final ObjectMapper objectMapper;
    private final PlatformService platformService;
    private final DriverService driverService;

    @EventListener(ApplicationReadyEvent.class)
    public void register() {
        log.debug("application ready");
        if (client == null) {
            client = new MqttV311Client();
        }
        if (connection == null) {
            connection = client.newConnection(MqttConnectionOption.builder()
                    .host(appConfig.getMqtt().getHost())
                    .port(appConfig.getMqtt().getPort())
                    .cleanSession(true)
                    .clientId(appConfig.getMqtt().getClientId())
                    .tcpConnectTimeout(1, TimeUnit.SECONDS)
                    .mqttConnectTimeout(1, TimeUnit.SECONDS)
                    .exceptionHandler((connection1, e) -> log.error("something error", e))
                    .build()
            );
            connection.connect(new TcpConnectResultHandler() {
                @Override
                public void onSuccess(MqttConnection connection) {

                }

                @Override
                public void onError(MqttConnection connection, Throwable cause) {
                    log.error("tcp connect error");
                    System.exit(-1);
                }

                @Override
                public void onTimeout(MqttConnection connection) {
                    log.error("tcp connect timeout");
                    System.exit(-1);
                }
            }, new MqttConnectResultHandler() {
                @Override
                public void onSuccess(MqttConnection connection) {
                    connection.subscribe(List.of(new MqttV311TopicAndQosLevel(appConfig.getMqtt().getRegisterResponseTopic(), MqttV311QosLevel.AT_MOST_ONCE)), new MqttMessageHandler() {
                        @Override
                        public void onMessage(MqttConnection connection, String topic, MqttV311QosLevel qos, boolean retain, boolean dupFlag, Integer packetId, byte[] payload) {
                            try {
                                val response = objectMapper.readValue(payload, RegisterResponse.class);
                                if (response.getStatus() == RegisterResponse.Status.SUCCESS) {
                                    log.info("register success");
                                    registerTimeout.cancel();
                                    platformService.onRegisterSuccess();
                                    heartbeatByteArray = objectMapper.writeValueAsBytes(new Heartbeat().setClientId(appConfig.getId()));
                                }

                                connection.subscribe(List.of(new MqttV311TopicAndQosLevel(appConfig.getMqtt().getInvokeRequestTopic(), MqttV311QosLevel.AT_MOST_ONCE)), new MqttMessageHandler() {
                                    @Override
                                    public void onMessage(MqttConnection mqttConnection, String s, MqttV311QosLevel mqttV311QosLevel, boolean b, boolean b1, Integer integer, byte[] bytes) {
                                        try {
                                            log.debug("new invoke request");
                                            val request = objectMapper.readValue(bytes, InvokeRequest.class);
                                            val result = driverService.invoke(request.getType(), request.getMethod(), request.getParams());
                                            val response = new InvokeResponse();
                                            response.setInvokeId(request.getInvokeId());
                                            response.setClientId(response.getClientId());
                                            response.setResult(result);
                                            connection.publishQos0Message(appConfig.getMqtt().getInvokeResponseTopic(), false, objectMapper.writeValueAsBytes(response), new MqttPublishResultHandler() {
                                                @Override
                                                public void onSuccess(MqttConnection mqttConnection) {

                                                }

                                                @Override
                                                public void onError(MqttConnection mqttConnection, Throwable throwable) {

                                                }
                                            });
                                        } catch (IOException e) {
                                            log.error("invoke request error", e);
                                        }
                                    }
                                }, new MqttSubscribeResultHandler() {
                                    @Override
                                    public void onSuccess(MqttConnection mqttConnection, List<MqttSubscription> list) {
                                        log.debug("subscribe invoke topic success");
                                    }

                                    @Override
                                    public void onError(MqttConnection mqttConnection, Throwable throwable) {
                                        log.error("subscribe invoke topic error");
                                        System.exit(-1);
                                    }
                                });
                            } catch (IOException e) {
                                log.error("register error", e);
                            }
                        }
                    }, new MqttSubscribeResultHandler() {
                        @Override
                        public void onSuccess(MqttConnection connection, List<MqttSubscription> subscriptions) {

                        }

                        @Override
                        public void onError(MqttConnection connection, Throwable cause) {
                            log.error("subscribe register response error");
                            System.exit(-1);
                        }
                    });
                    RegisterRequest request = new RegisterRequest();
                    request.setClientId(appConfig.getId());
                    request.setTypes(driverService.getAllDriverType());
                    try {
                        connection.publishQos0Message(appConfig.getMqtt().getRegisterRequestTopic(), false, objectMapper.writeValueAsBytes(request),
                                new MqttPublishResultHandler() {
                                    @Override
                                    public void onSuccess(MqttConnection mqttConnection) {
                                        registerTimeout = timer.newTimeout(timeout -> {
                                            log.error("register response timeout");
                                            System.exit(-1);
                                        }, 3, TimeUnit.SECONDS);
                                    }

                                    @Override
                                    public void onError(MqttConnection mqttConnection, Throwable throwable) {

                                    }
                                });
                    } catch (IOException e) {
                        log.error("register request error", e);
                    }
                }

                @Override
                public void onError(MqttConnection connection, MqttClientException cause) {
                    log.error("publish register request error");
                    System.exit(-1);
                }

                @Override
                public void onTimeout(MqttConnection connection) {
                    log.error("publish register request timeout");
                    System.exit(-1);
                }
            });
        }
    }

    @Scheduled(cron = "0/10 * * * * ? ")
    public void heartbeat() {
        if (connection != null) {
            connection.publishQos0Message(appConfig.getMqtt().getHeartbeatTopic(), false, this.heartbeatByteArray);
        } else {
            log.error("no connection");
            System.exit(-1);
        }
    }
}
