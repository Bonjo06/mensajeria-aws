package com.example.mensajeria_aws;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mensajeria")
public record MensajeriaProperties(String colaOrdenes, String dlqOrdenes, String topicoNotificaciones) 
{}