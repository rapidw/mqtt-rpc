package io.rapidw.mqttrpc.server.model;

import io.rapidw.mqttrpc.driver.spec.Driver;
import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "client_driver")
public class ClientDriver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    private Driver.Type type;

}
