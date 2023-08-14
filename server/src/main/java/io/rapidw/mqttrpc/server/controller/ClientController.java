package io.rapidw.mqttrpc.server.controller;

import io.rapidw.mqttrpc.server.model.Client;
import io.rapidw.mqttrpc.server.service.ClientService;
import io.rapidw.vo.response.BaseResponse;
import io.rapidw.vo.response.DataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/client")
@RequiredArgsConstructor
public class ClientController {
    private final ClientService clientService;

    @GetMapping
    public DataResponse<Collection<Client>> query() {
        return DataResponse.of(clientService.query());
    }

    @PostMapping("/update")
    public BaseResponse update(UpdateRequest request) {
        clientService.update(request);
        return BaseResponse.SUCCESS;
    }
}
