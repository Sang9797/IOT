package com.iot.devicemanagement.repository;

import com.iot.devicemanagement.entity.MqttBroker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MqttBrokerRepository extends JpaRepository<MqttBroker, String> {

    Optional<MqttBroker> findByName(String name);
}
