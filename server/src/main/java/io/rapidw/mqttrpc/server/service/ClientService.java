package io.rapidw.mqttrpc.server.service;

import io.rapidw.mqttrpc.dto.Heartbeat;
import io.rapidw.mqttrpc.dto.RegisterRequest;
import io.rapidw.mqttrpc.server.model.Client;
import io.rapidw.mqttrpc.server.model.ClientDriver;
import io.rapidw.mqttrpc.server.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientService {

    private final ClientRepository clientRepository;

    @Transactional
    public void onRegister(RegisterRequest request) {
        log.debug("client register");

        clientRepository.findById(request.getClientId())
                .ifPresentOrElse(
                        client -> {
                            client.setLastHeartbeatTime(Instant.now());
                            client.setId(request.getClientId());
                            client.setOnline(true);
                            clientRepository.save(client);
                        }, () -> {
                            val client = new Client();
                            client.setId(request.getClientId());
                            client.setOnline(true);
                            client.setLastHeartbeatTime(Instant.now());
                            client.setServices(request.getTypes().stream().map(v ->
                                    new ClientDriver().setClient(client).setType(v)
                            ).toList());
                            clientRepository.save(client);
                        }
                );
    }

    @Transactional
    public void onHeartbeat(Heartbeat heartbeat) {
        log.debug("on heartbeat");

        clientRepository.findById(heartbeat.getClientId())
                .ifPresentOrElse(
                        client -> {
                            client.setLastHeartbeatTime(Instant.now());
                            clientRepository.save(client);
                        }, () -> log.info("heartbeat from unregister client")
                );
    }

    public Collection<Client> query() {
        return clientRepository.findAll();
    }

    // 每10秒检查一次心跳
    @Scheduled(cron = "0/10 * * * * ? ")
    @Transactional
    public void cron() {
        log.debug("cron");
        clientRepository.updateStatus();
    }
}
