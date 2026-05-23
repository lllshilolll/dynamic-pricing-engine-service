package com.example.admin.service;

import com.example.admin.entity.Device;
import com.example.admin.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;

    @Transactional
    public Device create(Device device) {
        return deviceRepository.save(device);
    }

    public List<Device> findAll() {
        return deviceRepository.findAll();
    }

    public Optional<Device> findByExternalId(String externalId) {
        return deviceRepository.findByExternalId(externalId);
    }

    @Transactional
    public Device update(Long id, Device updated) {
        Device device = deviceRepository.findById(id).orElseThrow();
        device.setModel(updated.getModel());
        device.setLocation(updated.getLocation());
        device.setStatus(updated.getStatus());
        return deviceRepository.save(device);
    }
}
