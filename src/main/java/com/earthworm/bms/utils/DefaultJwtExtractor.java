package com.earthworm.bms.utils;

import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPublicKey;

@Component
public class DefaultJwtExtractor {
    private final BearerTokenResolver resolver = new DefaultBearerTokenResolver();;

    public BearerTokenResolver getResolver() {
        return this.resolver;// = new DefaultBearerTokenResolver();
    }
}
