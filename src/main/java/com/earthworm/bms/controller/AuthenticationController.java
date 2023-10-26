package com.earthworm.bms.controller;

import com.earthworm.bms.repository.CustomerRepository;
import jdk.jshell.spi.ExecutionControl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

@RestController
@CrossOrigin("*")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private CustomerRepository userRepository;
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
    @GetMapping("/user-details")
    public String getUserDetails(){
        String userName = SecurityContextHolder.getContext().getAuthentication().getName().toString();
        return userRepository.findByUserName(userName).orElseThrow().getName();
    }
}
