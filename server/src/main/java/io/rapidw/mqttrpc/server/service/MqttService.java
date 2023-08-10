package io.rapidw.mqttrpc.server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.rapidw.mqtt.client.v3_1_1.*;
import io.rapidw.mqtt.client.v3_1_1.handler.*;
import io.rapidw.mqtt.codec.v3_1_1.MqttV311QosLevel;
import io.rapidw.mqtt.codec.v3_1_1.MqttV311TopicAndQosLevel;
import io.rapidw.mqttrpc.mqtt.Heartbeat;
import io.rapidw.mqttrpc.mqtt.InvokeRequest;
import io.rapidw.mqttrpc.mqtt.RegisterRequest;
import io.rapidw.mqttrpc.mqtt.RegisterResponse;
import io.rapidw.mqttrpc.server.config.AppConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MqttService {
    private MqttV311Client client;

    private final AppConfig appConfig;
    private final ObjectMapper objectMapper;
    private final ClientService clientService;
    private MqttConnection mqttConnection;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        log.debug("init");
        client = new MqttV311Client();
        watchHeartbeat();
    }

    public void watchHeartbeat() {
        val connection = client.newConnection(MqttConnectionOption.builder()
                .host(appConfig.getMqtt().getHost())
                .port(appConfig.getMqtt().getPort())
                .cleanSession(true)
                .clientId(appConfig.getMqtt().getClientId())
                .tcpConnectTimeout(1, TimeUnit.SECONDS)
                .mqttConnectTimeout(1, TimeUnit.SECONDS)
                .exceptionHandler((connection1, e) -> log.error("something error", e))
                .build());
        connection.connect(new TcpConnectResultHandler() {
            @Override
            public void onSuccess(MqttConnection connection) {

            }

            @Override
            public void onError(MqttConnection connection, Throwable cause) {

            }

            @Override
            public void onTimeout(MqttConnection connection) {

            }
        }, new MqttConnectResultHandler() {
            @Override
            public void onSuccess(MqttConnection connection) {
                mqttConnection = connection;
                connection.subscribe(List.of(new MqttV311TopicAndQosLevel(appConfig.getMqtt().getRegisterRequestTopic(), MqttV311QosLevel.AT_MOST_ONCE)), new MqttMessageHandler() {
                    @Override
                    public void onMessage(MqttConnection connection, String topic, MqttV311QosLevel qos, boolean retain, boolean dupFlag, Integer packetId, byte[] payload) {
                        try {
                            val request = objectMapper.readValue(payload, RegisterRequest.class);
                            clientService.onRegister(request);
                            connection.publishQos0Message(appConfig.getMqtt().getRegisterResponseTopic(), false,
                                    objectMapper.writeValueAsBytes(new RegisterResponse(request.getClientId(), RegisterResponse.Status.SUCCESS)));
                        } catch (IOException e) {
                            log.error("invalid register req");
                        }
                    }
                }, new MqttSubscribeResultHandler() {
                    @Override
                    public void onSuccess(MqttConnection connection, List<MqttSubscription> subscriptions) {

                    }

                    @Override
                    public void onError(MqttConnection connection, Throwable cause) {
                        log.error("subscribe register request topic error", cause);
                    }
                });
                connection.subscribe(List.of(new MqttV311TopicAndQosLevel(appConfig.getMqtt().getHeartbeatTopic(), MqttV311QosLevel.AT_MOST_ONCE)), new MqttMessageHandler() {
                    @Override
                    public void onMessage(MqttConnection connection, String topic, MqttV311QosLevel qos, boolean retain, boolean dupFlag, Integer packetId, byte[] payload) {
                        try {
                            Heartbeat heartbeat = objectMapper.readValue(payload, Heartbeat.class);
                            clientService.onHeartbeat(heartbeat);
                        } catch (IOException e) {
                            log.error("invalid heartbeat");
                        }
                    }
                }, new MqttSubscribeResultHandler() {
                    @Override
                    public void onSuccess(MqttConnection connection, List<MqttSubscription> subscriptions) {

                    }

                    @Override
                    public void onError(MqttConnection connection, Throwable cause) {
                        log.error("subscribe heartbeat topic error", cause);
                    }
                });
                connection.subscribe(List.of(new MqttV311TopicAndQosLevel(appConfig.getMqtt().getInvokeResponseTopic(), MqttV311QosLevel.AT_MOST_ONCE)), new MqttMessageHandler() {
                    @Override
                    public void onMessage(MqttConnection mqttConnection, String s, MqttV311QosLevel mqttV311QosLevel, boolean b, boolean b1, Integer integer, byte[] bytes) {

                    }
                }, new MqttSubscribeResultHandler() {
                    @Override
                    public void onSuccess(MqttConnection mqttConnection, List<MqttSubscription> list) {

                    }

                    @Override
                    public void onError(MqttConnection mqttConnection, Throwable throwable) {

                    }
                });
            }

            @Override
            public void onError(MqttConnection connection, MqttClientException cause) {

            }

            @Override
            public void onTimeout(MqttConnection connection) {

            }
        });
    }

    public void publishInvoke(InvokeRequest request) {
        try {
            mqttConnection.publishQos0Message(appConfig.getMqtt().getInvokeRequestTopic(), false, objectMapper.writeValueAsBytes(request), new MqttPublishResultHandler() {
                @Override
                public void onSuccess(MqttConnection mqttConnection) {
                }

                @Override
                public void onError(MqttConnection mqttConnection, Throwable throwable) {

                }
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
