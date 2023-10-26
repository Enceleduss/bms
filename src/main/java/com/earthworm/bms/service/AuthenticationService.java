package com.earthworm.bms.service;

import java.util.HashSet;
import java.util.Set;

import com.earthworm.bms.model.datapojos.RegistrationDetailsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.earthworm.bms.model.CustomerRecord;
import com.earthworm.bms.model.datapojos.LoginResponseDTO;
import com.earthworm.bms.model.Role;
import com.earthworm.bms.repository.RoleRepository;
import com.earthworm.bms.repository.CustomerRepository;

@Service
@Transactional
public class AuthenticationService {

    @Autowired
    private CustomerRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    public CustomerRecord registerUser(RegistrationDetailsDTO rec){

        String encodedPassword = passwordEncoder.encode(rec.getPassword());
        Role userRole = roleRepository.findByAuthority("USER").get();

        Set<Role> authorities = new HashSet<>();

        authorities.add(userRole);

        return userRepository.save(new CustomerRecord(rec.getName(), rec.getEmail(), rec.getUserName(), encodedPassword, rec.getAddress(), rec.getPan(), rec.getUid(), authorities));
    }

    public LoginResponseDTO loginUser(String username, String password){
        System.out.println("trying to authenticate username " + username + " password " + password);
        try{
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            String token = tokenService.generateJwt(auth);

            return new LoginResponseDTO(userRepository.findByUserName(username).get(), token);

        } catch(AuthenticationException e){
            return new LoginResponseDTO(null, "");
        }
    }

}
