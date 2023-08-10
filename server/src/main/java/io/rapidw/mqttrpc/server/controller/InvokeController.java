package io.rapidw.mqttrpc.server.controller;

import io.rapidw.mqttrpc.dto.http.HttpInvokeRequest;
import io.rapidw.mqttrpc.server.service.DriverService;
import io.rapidw.vo.response.BaseResponse;
import io.rapidw.vo.response.DataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/invoke")
@RequiredArgsConstructor
public class InvokeController {
    private final DriverService driverService;

    @GetMapping
    public DataResponse<Object> invoke(@RequestBody HttpInvokeRequest request) {
        return DataResponse.of(driverService.invoke(request));
    }
}
