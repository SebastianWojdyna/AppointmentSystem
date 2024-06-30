package com.example.appointmentsystem.service;

import com.example.appointmentsystem.model.AppointmentServiceType;
import com.example.appointmentsystem.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceService {

    private final ServiceRepository serviceRepository;

    @Autowired
    public ServiceService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    public List<AppointmentServiceType> findAll() {
        return serviceRepository.findAll();
    }
}
