package com.iot.devicemanagement.controller;

import com.iot.common.dto.DeviceDto;
import com.iot.common.dto.MqttBrokerDto;
import com.iot.devicemanagement.service.MqttBrokerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/mqtt-brokers")
@CrossOrigin(origins = "*")
public class MqttBrokerController {

    private final MqttBrokerService mqttBrokerService;

    public MqttBrokerController(MqttBrokerService mqttBrokerService) {
        this.mqttBrokerService = mqttBrokerService;
    }

    @PostMapping
    public ResponseEntity<MqttBrokerDto> createBroker(@Valid @RequestBody MqttBrokerDto mqttBrokerDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mqttBrokerService.createBroker(mqttBrokerDto));
    }

    @GetMapping
    public ResponseEntity<List<MqttBrokerDto>> getBrokers() {
        return ResponseEntity.ok(mqttBrokerService.getAllBrokers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MqttBrokerDto> getBroker(@PathVariable String id) {
        return ResponseEntity.ok(mqttBrokerService.getBrokerById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MqttBrokerDto> updateBroker(@PathVariable String id, @Valid @RequestBody MqttBrokerDto mqttBrokerDto) {
        return ResponseEntity.ok(mqttBrokerService.updateBroker(id, mqttBrokerDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBroker(@PathVariable String id) {
        mqttBrokerService.deleteBroker(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/devices")
    public ResponseEntity<List<DeviceDto>> getBrokerDevices(@PathVariable String id) {
        return ResponseEntity.ok(mqttBrokerService.getDevicesForBroker(id));
    }
}
