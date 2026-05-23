package com.example.admin.controller;

import com.example.admin.entity.Device;
import com.example.admin.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/devices", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Device create(@RequestBody Device device) {
        return deviceService.create(device);
    }

    @GetMapping
    public List<Device> findAll() {
        return deviceService.findAll();
    }

    @GetMapping("/{externalId}")
    public Device findByExternalId(@PathVariable String externalId) {
        return deviceService.findByExternalId(externalId).orElseThrow();
    }

    @PutMapping("/{id}")
    public Device update(@PathVariable Long id, @RequestBody Device device) {
        return deviceService.update(id, device);
    }
}
