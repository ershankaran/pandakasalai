package com.vanikathunaivan.pandakasalai.Exceptions;

public class ConcurrentUpdateException  extends RuntimeException{
    public ConcurrentUpdateException(String message) {
        super(message);
    }
}
