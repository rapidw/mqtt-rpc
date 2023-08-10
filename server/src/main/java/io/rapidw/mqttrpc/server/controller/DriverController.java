package io.rapidw.mqttrpc.server.controller;

import io.rapidw.mqttrpc.server.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/driver")
@RequiredArgsConstructor
public class DriverController {
    private final DriverService driverService;

    @GetMapping
    public String invoke() {
        driverService.invoke();
        return "ok";
    }
}
