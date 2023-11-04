package com.earthworm.bms.controller;

import com.earthworm.bms.repository.CustomerRepository;
import com.earthworm.bms.service.RefreshTokenService;
import com.earthworm.bms.service.TokenService;
import com.earthworm.bms.utils.DefaultJwtExtractor;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jdk.jshell.spi.ExecutionControl;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.bind.annotation.*;
import com.earthworm.bms.exceptions.RefreshTokenException;//CustomerRecord;
import com.earthworm.bms.model.CustomerRecord;
import com.earthworm.bms.model.datapojos.LoginResponseDTO;
import com.earthworm.bms.model.datapojos.LoginDTO;
import com.earthworm.bms.model.datapojos.RegistrationDetailsDTO;
import com.earthworm.bms.service.AuthenticationService;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.Arrays;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
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
    @Autowired
    RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> loginUser(@RequestBody LoginDTO body, HttpServletRequest request, HttpServletResponse response) throws UserPrincipalNotFoundException {
        //throw new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found with name ");
        try{
            return new ResponseEntity<>(authenticationService.loginUser(body.getUsername(), body.getPassword(),response), HttpStatus.OK);
        }
     catch(AuthenticationException e){
         throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Authentication error "+e.getMessage(),e);
     }
        catch(Exception e)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Authentication error "+e.getMessage(),e);
        }
        //response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      //  return new LoginResponseDTO(null, "");
    //}
      //  catch(RuntimeException e)
    //{

    //}
    }
    @GetMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refreshUser(HttpServletRequest request, HttpServletResponse response){
        System.out.println("entered  refreshUser controller "+request.getCookies().length);
        String receivedToken = Arrays.stream(request.getCookies())
                .filter(cookie->cookie.getName().equals("refreshToken"))
                .map(cookie -> cookie.getValue())
                .findAny().orElseThrow();
        System.out.println("request "+receivedToken);
        try {
            String token = receivedToken;
            refreshTokenService.findByToken(token).orElseThrow(() -> new RefreshTokenException(token,
                    "Refresh token is not in database!"));
            BearerTokenAuthenticationToken authenticationRequest = new BearerTokenAuthenticationToken(token);
            authenticationRequest.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            Authentication authenticationResult = jwtAuthResolver.resolve(request).authenticate(authenticationRequest);
            String username = ((Jwt) authenticationResult.getPrincipal()).getSubject();
            CustomerRecord user = customerRepository.findByUsername(username).orElseThrow(() ->
                            new UsernameNotFoundException("User not found with username or email: "+ username));
            String jwt_token = "";
            if(user != null)
            {

                jwt_token = tokenService.generateJwt(authenticationResult,10000);
            }
            return new ResponseEntity<>(new LoginResponseDTO(user,jwt_token), HttpStatus.OK);
        }
        catch(Exception e){
            //e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Session expired, "+e.getMessage(),e);
        }
    }

    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser(@RequestBody String body, HttpServletResponse response) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName().toString();
        if (userName != "") {
            refreshTokenService.deleteByUserName(userName);
        }

        Cookie cookie = new Cookie("refreshToken", "");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);

        return ResponseEntity.ok()
                .body("You've been signed out!");
    }

}
