package io.rapidw.mqttrpc.server.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Entity
@Table(name = "client")
public class Client {
    @Id
    private String id;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "client")
    private List<ClientDriver> services;

    private Instant lastHeartbeatTime;

    private Boolean online;
}
