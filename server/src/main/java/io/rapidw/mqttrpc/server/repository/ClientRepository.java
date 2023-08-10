package io.rapidw.mqttrpc.server.repository;

import io.rapidw.mqttrpc.server.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client, String> {

    @Modifying(clearAutomatically = true)
    @Query("update Client c set c.online = false where c.lastHeartbeatTime < current_timestamp - 10000")
    void updateStatus();
}
