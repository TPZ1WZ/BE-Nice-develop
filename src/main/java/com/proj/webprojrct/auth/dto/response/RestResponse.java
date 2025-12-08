package com.proj.webprojrct.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class RestResponse<T> {

    private int statusCode;
    private String error;
    private Object message;
    private T data;
}
