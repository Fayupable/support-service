package com.fayupable.test.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.ws.rs.core.HttpHeaders;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignClientConfiguration implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        String authorizationHeader = RequestContextHolder.getRequestAttributes() != null ?
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest()
                        .getHeader(HttpHeaders.AUTHORIZATION) : null;
        if (authorizationHeader != null) {
            requestTemplate.header(HttpHeaders.AUTHORIZATION, authorizationHeader);
        }
    }
}
