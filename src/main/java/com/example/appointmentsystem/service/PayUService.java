package com.example.appointmentsystem.service;

import com.example.appointmentsystem.config.PayUConfig;
import com.example.appointmentsystem.model.Appointment;
import com.example.appointmentsystem.repository.AppointmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class PayUService {

    private static final Logger logger = LoggerFactory.getLogger(PayUService.class);

    @Autowired
    private PayUConfig payUConfig;

    @Autowired
    private AppointmentRepository appointmentRepository;

    private String getAccessToken() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("grant_type", "client_credentials");
        requestBody.add("client_id", payUConfig.getClientId());
        requestBody.add("client_secret", payUConfig.getClientSecret());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    payUConfig.getApiUrl() + "/pl/standard/user/oauth/authorize",
                    request,
                    Map.class
            );

            logger.info("Response from PayU for access token: " + response);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map responseBody = response.getBody();
                return responseBody != null ? (String) responseBody.get("access_token") : null;
            } else {
                logger.error("Failed to obtain access token: " + response);
                throw new RuntimeException("Failed to obtain access token");
            }
        } catch (HttpClientErrorException e) {
            logger.error("HttpClientErrorException: " + e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            logger.error("Exception: ", e);
            throw new RuntimeException("Failed to obtain access token", e);
        }
    }

    public String initiatePayment(Appointment appointment) {
        String accessToken = getAccessToken();
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        double totalAmount = appointment.getService().getPrice() * 100; // PayU requires amount in cents
        logger.info("Calculated totalAmount: " + totalAmount);

        if (totalAmount <= 0) {
            throw new IllegalArgumentException("Total amount must be greater than zero");
        }

        Map<String, Object> request = new HashMap<>();
        request.put("totalAmount", (int) totalAmount);
        request.put("currencyCode", "PLN");
        request.put("description", "Appointment payment");
        request.put("continueUrl", payUConfig.getContinueUrl());
        request.put("notifyUrl", payUConfig.getNotifyUrl());
        request.put("customerIp", "127.0.0.1");
        request.put("merchantPosId", payUConfig.getClientId());

        logger.info("Request payload: " + request);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    payUConfig.getApiUrl() + "/api/v2_1/orders",
                    entity,
                    Map.class
            );

            logger.info("Response from PayU for payment order: " + response);

            if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.FOUND) {
                Map responseBody = response.getBody();
                logger.info("Payment order created successfully: " + responseBody);
                String redirectUri = responseBody != null ? (String) responseBody.get("redirectUri") : null;
                String orderId = responseBody != null ? (String) responseBody.get("orderId") : null;

                if (orderId == null) {
                    logger.error("Order ID is null in PayU response");
                    throw new RuntimeException("Order ID is null in PayU response");
                }

                // Zaktualizuj orderId w bazie danych
                appointment.setOrderId(orderId);
                appointmentRepository.save(appointment);

                return redirectUri;
            } else {
                logger.error("Failed to create payment order: " + response);
                throw new RuntimeException("Failed to create payment order");
            }
        } catch (HttpClientErrorException e) {
            logger.error("HttpClientErrorException: " + e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            logger.error("Exception: ", e);
            throw new RuntimeException("Failed to create payment", e);
        }
    }

    public String processPayment(String orderId) {
        Optional<Appointment> optionalAppointment = appointmentRepository.findByOrderId(orderId);
        if (optionalAppointment.isPresent()) {
            Appointment appointment = optionalAppointment.get();
            return initiatePayment(appointment);
        } else {
            throw new RuntimeException("Appointment not found for order ID: " + orderId);
        }
    }
}
