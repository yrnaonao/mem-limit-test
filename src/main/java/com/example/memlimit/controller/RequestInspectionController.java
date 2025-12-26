package com.example.memlimit.controller;

import com.example.memlimit.model.HeadersResponse;
import com.example.memlimit.model.IpResponse;
import com.example.memlimit.model.RequestInfo;
import com.example.memlimit.model.UserAgentResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class RequestInspectionController {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/ip")
    public IpResponse getIp(HttpServletRequest request) {
        return IpResponse.builder()
                .origin(getClientIp(request))
                .build();
    }

    @GetMapping("/headers")
    public HeadersResponse getHeaders(HttpServletRequest request) {
        return HeadersResponse.builder()
                .headers(extractHeaders(request))
                .build();
    }

    @GetMapping("/user-agent")
    public UserAgentResponse getUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return UserAgentResponse.builder()
                .userAgent(userAgent != null ? userAgent : "")
                .build();
    }

    @GetMapping("/get")
    public RequestInfo handleGet(HttpServletRequest request) throws IOException {
        return buildRequestInfo(request, "GET");
    }

    @PostMapping("/post")
    public RequestInfo handlePost(HttpServletRequest request) throws IOException {
        return buildRequestInfo(request, "POST");
    }

    @PutMapping("/put")
    public RequestInfo handlePut(HttpServletRequest request) throws IOException {
        return buildRequestInfo(request, "PUT");
    }

    @PatchMapping("/patch")
    public RequestInfo handlePatch(HttpServletRequest request) throws IOException {
        return buildRequestInfo(request, "PATCH");
    }

    @DeleteMapping("/delete")
    public RequestInfo handleDelete(HttpServletRequest request) throws IOException {
        return buildRequestInfo(request, "DELETE");
    }

    @RequestMapping(value = "/anything", method = {RequestMethod.GET, RequestMethod.POST, 
            RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH, RequestMethod.OPTIONS,
            RequestMethod.HEAD, RequestMethod.TRACE})
    public RequestInfo handleAnything(HttpServletRequest request) throws IOException {
        return buildRequestInfo(request, request.getMethod());
    }

    @RequestMapping(value = "/anything/**", method = {RequestMethod.GET, RequestMethod.POST, 
            RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH, RequestMethod.OPTIONS,
            RequestMethod.HEAD, RequestMethod.TRACE})
    public RequestInfo handleAnythingWithPath(HttpServletRequest request) throws IOException {
        return buildRequestInfo(request, request.getMethod());
    }

    private RequestInfo buildRequestInfo(HttpServletRequest request, String method) throws IOException {
        String contentType = request.getContentType();
        Map<String, String> queryParams = new HashMap<>();
        Map<String, String> formParams = new HashMap<>();
        
        // Separate query parameters from form parameters
        String queryString = request.getQueryString();
        if (queryString != null && !queryString.isEmpty()) {
            queryParams = parseFormData(queryString);
        }
        
        RequestInfo.RequestInfoBuilder builder = RequestInfo.builder()
                .method(method)
                .origin(getClientIp(request))
                .url(getFullUrl(request))
                .headers(extractHeaders(request))
                .args(queryParams)
                .files(new HashMap<>())
                .form(new HashMap<>());

        // Extract body data
        if (contentType != null && !method.equals("GET")) {
            if (contentType.contains("application/x-www-form-urlencoded")) {
                // For form data, get it from request parameters (body is already parsed)
                Map<String, String[]> parameterMap = request.getParameterMap();
                for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                    // Skip parameters that are in the query string
                    if (!queryParams.containsKey(entry.getKey()) && entry.getValue().length > 0) {
                        formParams.put(entry.getKey(), entry.getValue()[0]);
                    }
                }
                builder.form(formParams);
                // Reconstruct the form data as body
                StringBuilder bodyBuilder = new StringBuilder();
                for (Map.Entry<String, String> entry : formParams.entrySet()) {
                    if (bodyBuilder.length() > 0) bodyBuilder.append("&");
                    bodyBuilder.append(entry.getKey()).append("=").append(entry.getValue());
                }
                builder.data(bodyBuilder.toString());
            } else {
                String body = readRequestBody(request);
                
                if (contentType.contains("application/json")) {
                    try {
                        Object jsonData = objectMapper.readValue(body, Object.class);
                        builder.json(jsonData);
                        builder.data(body);
                    } catch (Exception e) {
                        builder.data(body);
                    }
                } else if (contentType.contains("multipart/form-data")) {
                    // For multipart, we'll just store the raw body
                    builder.data(body);
                } else {
                    builder.data(body);
                }
            }
        } else {
            builder.data("");
        }

        return builder.build();
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // If multiple IPs in X-Forwarded-For, take the first one
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        return headers;
    }

    private Map<String, String> extractQueryParameters(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> parameterMap = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            if (entry.getValue().length > 0) {
                params.put(entry.getKey(), entry.getValue()[0]);
            }
        }
        return params;
    }

    private String getFullUrl(HttpServletRequest request) {
        StringBuffer url = request.getRequestURL();
        String queryString = request.getQueryString();
        if (queryString != null) {
            url.append('?').append(queryString);
        }
        return url.toString();
    }

    private String readRequestBody(HttpServletRequest request) throws IOException {
        try (BufferedReader reader = request.getReader()) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (Exception e) {
            return "";
        }
    }

    private Map<String, String> parseFormData(String body) {
        Map<String, String> formData = new HashMap<>();
        if (body == null || body.isEmpty()) {
            return formData;
        }
        
        String[] pairs = body.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                try {
                    String key = java.net.URLDecoder.decode(keyValue[0], "UTF-8");
                    String value = java.net.URLDecoder.decode(keyValue[1], "UTF-8");
                    formData.put(key, value);
                } catch (Exception e) {
                    // If decoding fails, use raw values
                    formData.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return formData;
    }
}
