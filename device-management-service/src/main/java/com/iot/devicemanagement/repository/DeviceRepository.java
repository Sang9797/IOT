package com.iot.devicemanagement.repository;

import com.iot.common.dto.DeviceDto;
import com.iot.devicemanagement.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, String> {
    
    Optional<Device> findByAddress(String address);
    
    List<Device> findByFactoryId(String factoryId);
    
    List<Device> findByStatus(DeviceDto.DeviceStatus status);
    
    List<Device> findByType(DeviceDto.DeviceType type);
    
    @Query("SELECT d FROM Device d WHERE d.factoryId = :factoryId AND d.status = :status")
    List<Device> findByFactoryIdAndStatus(@Param("factoryId") String factoryId, 
                                         @Param("status") DeviceDto.DeviceStatus status);
    
    @Query("SELECT d FROM Device d WHERE d.lastSeen < :threshold")
    List<Device> findDevicesNotSeenSince(@Param("threshold") java.time.LocalDateTime threshold);
    
    @Query("SELECT COUNT(d) FROM Device d WHERE d.factoryId = :factoryId")
    long countByFactoryId(@Param("factoryId") String factoryId);
    
    @Query("SELECT COUNT(d) FROM Device d WHERE d.status = :status")
    long countByStatus(@Param("status") DeviceDto.DeviceStatus status);
}
