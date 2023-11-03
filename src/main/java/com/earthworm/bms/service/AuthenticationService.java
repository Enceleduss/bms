package com.earthworm.bms.service;

import java.io.IOException;
import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.earthworm.bms.model.datapojos.RegistrationDetailsDTO;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

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
import org.springframework.web.bind.annotation.CrossOrigin;

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

        CustomerRecord userRecord = userRepository.save(new CustomerRecord(rec.getName(), rec.getEmail(), rec.getUsername(), encodedPassword, rec.getAddress(), rec.getPan(), rec.getUid(), authorities));
        userRecord.setAcctype(rec.getAcctype());
        userRecord.setBranchname(rec.getBranchname());
        userRecord.setDob(rec.getDob());
        userRecord.setCountry(rec.getCountry());
        userRecord.setDocnum(rec.getDocnum());
        userRecord.setIdentificationtype(rec.getIdentificationtype());
        userRecord.setPhone(rec.getPhone());
        userRecord.setState(rec.getState());
        userRecord.setInitialdeposit(rec.getInitialdeposit());
        userRecord.depositBalance(rec.getInitialdeposit());
        return userRecord;
    }

    public LoginResponseDTO loginUser(String username, String password, HttpServletResponse response) throws UserPrincipalNotFoundException {
        System.out.println("trying to authenticate username " + username + " password " + password);

        Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );


            String token = tokenService.generateJwt(auth,10000);
            String refreshToken = tokenService.generateJwt(auth,300000);
            Cookie cookie = new Cookie("refreshToken", refreshToken);
            cookie.setMaxAge(60000);
            cookie.setHttpOnly(true);
            response.addCookie(cookie);
            //response.setHeader("Access-Control-Allow-Origin", "*");
            //response.setHeader("Access-Control-Allow-Credentials", "true");
            //response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
            //response.setHeader("Access-Control-Max-Age", "3600");
            //response.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With, remember-me");
            return new LoginResponseDTO(userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User id or password is invalid")), token);


    }

}
