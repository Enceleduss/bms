package com.earthworm.bms.controller;

import com.earthworm.bms.model.CustomerRecord;
import com.earthworm.bms.model.datapojos.DepositDTO;
import com.earthworm.bms.model.datapojos.LoginDTO;
import com.earthworm.bms.model.datapojos.LoginResponseDTO;
import com.earthworm.bms.model.datapojos.RegistrationDetailsDTO;
import com.earthworm.bms.repository.CustomerRepository;
import com.earthworm.bms.service.AuthenticationService;
import com.earthworm.bms.service.TokenService;
import com.earthworm.bms.utils.DefaultJwtExtractor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.Arrays;

@RestController
public class CustmerController {

    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private CustomerRepository userRepository;
    @Autowired
    private DefaultJwtExtractor bearerTokenResolver;
    @Autowired
    AuthenticationManagerResolver jwtAuthResolver;
    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    TokenService tokenService;
    @PostMapping("/register")
    public ResponseEntity<CustomerRecord> registerUser(@RequestBody RegistrationDetailsDTO body){
        System.out.println("body " + body.toString());
        if(!userRepository.existsByUsername(body.getUsername()))
            authenticationService.registerUser(body);
        else
            return ResponseEntity.badRequest().build();
        return ResponseEntity.created(null).build();
    }

    @GetMapping("/user-details")
    public CustomerRecord getUserDetails(){
        String userName = SecurityContextHolder.getContext().getAuthentication().getName().toString();
        return userRepository.findByUsername(userName).orElseThrow(() ->
                new UsernameNotFoundException("No user session is active"));
    }
    @PutMapping("/make-deposit")
    public CustomerRecord updateUserDetails(@RequestBody DepositDTO body){
        String userName = SecurityContextHolder.getContext().getAuthentication().getName().toString();
        CustomerRecord user = userRepository.findByUsername(userName).orElseThrow(() ->
                new UsernameNotFoundException("No user session is active"));
        user.depositBalance(body.getDeposit());
        user.setAcctype(body.getAcctype());
        userRepository.save(user);
        return user;
    }
}
