package com.earthworm.bms.service;

import java.time.Instant;
import java.util.stream.Collectors;

import com.earthworm.bms.model.RefreshToken;
import com.earthworm.bms.repository.CustomerRepository;
import com.earthworm.bms.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private JwtDecoder jwtDecoder;
    @Autowired
    private CustomerRepository userRepository;
    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    public String generateJwt(Authentication auth, long expiration){

        Instant now = Instant.now();
        String scope = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));
        Instant expiryTime = now.plusMillis(expiration);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(expiryTime)
                .subject(auth.getName())
                .claim("roles", scope)
                .build();
         String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
         RefreshToken tokenObj = new RefreshToken(auth.getName(),token,expiryTime);
         refreshTokenRepository.save(tokenObj);
         return token;
}
}

