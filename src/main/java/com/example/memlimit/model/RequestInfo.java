package com.example.memlimit.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestInfo {
    private Map<String, String> args;
    private Object data;
    private Map<String, String> files;
    private Map<String, String> form;
    private Map<String, String> headers;
    private Object json;
    private String method;
    private String origin;
    private String url;
}
