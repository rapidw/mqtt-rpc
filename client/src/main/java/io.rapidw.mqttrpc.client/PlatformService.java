package io.rapidw.mqttrpc.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlatformService {

    private volatile boolean connected;

    public void onRegisterSuccess() {
        this.connected = true;
    }


}
