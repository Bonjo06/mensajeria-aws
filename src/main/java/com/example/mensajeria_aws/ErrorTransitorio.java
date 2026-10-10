package com.example.mensajeria_aws;

public class ErrorTransitorio extends RuntimeException {
    public ErrorTransitorio(String m) { super(m); }
}