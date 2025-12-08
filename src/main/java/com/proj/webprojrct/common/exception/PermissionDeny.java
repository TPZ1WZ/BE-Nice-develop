package com.proj.webprojrct.common.exception;

public class PermissionDeny extends RuntimeException {

    public PermissionDeny(String message) {
        super(message);
    }
}
