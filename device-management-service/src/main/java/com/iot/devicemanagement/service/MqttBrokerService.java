package com.iot.devicemanagement.service;

import com.iot.common.config.KafkaTopics;
import com.iot.common.dto.DeviceDto;
import com.iot.common.dto.MqttBrokerDto;
import com.iot.devicemanagement.entity.MqttBroker;
import com.iot.devicemanagement.exception.ConflictException;
import com.iot.devicemanagement.exception.NotFoundException;
import com.iot.devicemanagement.exception.ValidationException;
import com.iot.devicemanagement.repository.DeviceRepository;
import com.iot.devicemanagement.repository.MqttBrokerRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MqttBrokerService {

    private final MqttBrokerRepository mqttBrokerRepository;
    private final DeviceRepository deviceRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public MqttBrokerService(
            MqttBrokerRepository mqttBrokerRepository,
            DeviceRepository deviceRepository,
            KafkaTemplate<String, Object> kafkaTemplate
    ) {
        this.mqttBrokerRepository = mqttBrokerRepository;
        this.deviceRepository = deviceRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public MqttBrokerDto createBroker(MqttBrokerDto dto) {
        validateUniqueName(dto.getName(), null);
        MqttBroker broker = new MqttBroker();
        applyUpdates(broker, dto);
        MqttBroker saved = mqttBrokerRepository.save(broker);
        kafkaTemplate.send(KafkaTopics.MQTT_BROKER_UPDATES, saved.getId(), saved.toDto());
        return saved.toDto();
    }

    public List<MqttBrokerDto> getAllBrokers() {
        return mqttBrokerRepository.findAll().stream().map(MqttBroker::toDto).toList();
    }

    public MqttBrokerDto getBrokerById(String id) {
        return getBrokerEntity(id).toDto();
    }

    public MqttBrokerDto updateBroker(String id, MqttBrokerDto dto) {
        MqttBroker broker = getBrokerEntity(id);
        validateUniqueName(dto.getName(), id);
        applyUpdates(broker, dto);
        MqttBroker saved = mqttBrokerRepository.save(broker);
        kafkaTemplate.send(KafkaTopics.MQTT_BROKER_UPDATES, saved.getId(), saved.toDto());
        return saved.toDto();
    }

    public void deleteBroker(String id) {
        MqttBroker broker = getBrokerEntity(id);
        if (deviceRepository.countByMqttBroker_Id(id) > 0) {
            throw new ConflictException("Broker " + id + " cannot be deleted while devices are assigned");
        }
        mqttBrokerRepository.delete(broker);
        kafkaTemplate.send(KafkaTopics.MQTT_BROKER_UPDATES, id, new BrokerDeletionEvent(id));
    }

    public List<DeviceDto> getDevicesForBroker(String brokerId) {
        getBrokerEntity(brokerId);
        return deviceRepository.findByMqttBroker_Id(brokerId).stream().map(device -> device.toDto()).toList();
    }

    public MqttBroker getRequiredEnabledBroker(String brokerId) {
        if (brokerId == null || brokerId.isBlank()) {
            throw new ValidationException("mqttBrokerId is required");
        }
        MqttBroker broker = getBrokerEntity(brokerId);
        if (!Boolean.TRUE.equals(broker.getEnabled())) {
            throw new ValidationException("Broker " + brokerId + " is disabled");
        }
        return broker;
    }

    private MqttBroker getBrokerEntity(String id) {
        return mqttBrokerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Broker not found with id: " + id));
    }

    private void validateUniqueName(String name, String currentId) {
        mqttBrokerRepository.findByName(name)
                .filter(existing -> !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new ValidationException("Broker name already exists: " + name);
                });
    }

    private void applyUpdates(MqttBroker broker, MqttBrokerDto dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new ValidationException("Broker name is required");
        }
        if (dto.getHost() == null || dto.getHost().isBlank()) {
            throw new ValidationException("Broker host is required");
        }
        if (dto.getProtocol() == null || dto.getProtocol().isBlank()) {
            throw new ValidationException("Broker protocol is required");
        }
        if (dto.getPort() == null || dto.getPort() < 1 || dto.getPort() > 65535) {
            throw new ValidationException("Broker port must be between 1 and 65535");
        }
        broker.setName(dto.getName());
        broker.setHost(dto.getHost());
        broker.setPort(dto.getPort());
        broker.setProtocol(dto.getProtocol());
        broker.setUsername(dto.getUsername());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            broker.setPassword(dto.getPassword());
        }
        broker.setEnabled(dto.getEnabled() == null ? Boolean.TRUE : dto.getEnabled());
        broker.setDescription(dto.getDescription());
        broker.setTopicPrefix(dto.getTopicPrefix());
    }

    public static class BrokerDeletionEvent {
        private String brokerId;

        public BrokerDeletionEvent() {
        }

        public BrokerDeletionEvent(String brokerId) {
            this.brokerId = brokerId;
        }

        public String getBrokerId() {
            return brokerId;
        }

        public void setBrokerId(String brokerId) {
            this.brokerId = brokerId;
        }
    }
}
