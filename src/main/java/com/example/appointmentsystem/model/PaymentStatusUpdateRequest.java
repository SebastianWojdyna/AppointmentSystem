package com.example.appointmentsystem.dto;

public class PaymentStatusUpdateRequest {

    private String orderId;
    private boolean paid;

    // Gettery i settery
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }
}
