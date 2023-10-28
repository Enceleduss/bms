package com.earthworm.bms.controller;

import com.earthworm.bms.repository.CustomerRepository;
import com.earthworm.bms.service.TokenService;
import com.earthworm.bms.utils.DefaultJwtExtractor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jdk.jshell.spi.ExecutionControl;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.earthworm.bms.model.CustomerRecord;
import com.earthworm.bms.model.datapojos.LoginResponseDTO;
import com.earthworm.bms.model.datapojos.LoginDTO;
import com.earthworm.bms.model.datapojos.RegistrationDetailsDTO;
import com.earthworm.bms.service.AuthenticationService;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@CrossOrigin("*")
public class AuthenticationController {

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
    public CustomerRecord registerUser(@RequestBody RegistrationDetailsDTO body){
        System.out.println("body " + body.toString());
        if(!userRepository.existsByUserName(body.getUserName()))
            return authenticationService.registerUser(body);
        else
            return null;
    }

    @PostMapping("/login")
    public LoginResponseDTO loginUser(@RequestBody LoginDTO body){
        return authenticationService.loginUser(body.getUsername(), body.getPassword());
    }
    @GetMapping("/refresh")
    public void refreshUser(HttpServletRequest request, HttpServletResponse response){
        System.out.println("request "+request.getHeader("Authorization"));
        try {
            String token = bearerTokenResolver.getResolver().resolve(request);
            BearerTokenAuthenticationToken authenticationRequest = new BearerTokenAuthenticationToken(token);
            authenticationRequest.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            Authentication authenticationResult = jwtAuthResolver.resolve(request).authenticate(authenticationRequest);
            String username = ((Jwt) authenticationResult.getPrincipal()).getSubject();
            CustomerRecord user = customerRepository.findByUserName(username).orElseThrow(() ->
                            new UsernameNotFoundException("User not found with username or email: "+ username));
            if(user != null)
            {

                String jwt_token = tokenService.generateJwt(authenticationResult,10000);
                response.setHeader(AUTHORIZATION, jwt_token);
            }
        }
        catch(Exception e){
            e.printStackTrace();;
        }
    }
    @GetMapping("/user-details")
    public String getUserDetails(){
        String userName = SecurityContextHolder.getContext().getAuthentication().getName().toString();
        return userRepository.findByUserName(userName).orElseThrow().getName();
    }
}
