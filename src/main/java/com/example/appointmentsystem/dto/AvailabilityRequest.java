package com.example.appointmentsystem.dto;

import java.time.LocalDateTime;
import java.util.List;

public class AvailabilityRequest {
    private Long serviceId;
    private List<LocalDateTime> availableTimes;
    private double price;

    // Gettery i settery
    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }

    public List<LocalDateTime> getAvailableTimes() { return availableTimes; }
    public void setAvailableTimes(List<LocalDateTime> availableTimes) { this.availableTimes = availableTimes; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}
