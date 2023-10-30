package com.earthworm.bms.service;

import java.io.IOException;
import java.time.Instant;
import java.util.HashSet;
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
        return userRecord;
    }

    public LoginResponseDTO loginUser(String username, String password, HttpServletResponse response) throws IOException {
        System.out.println("trying to authenticate username " + username + " password " + password);
        try{
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            String token = tokenService.generateJwt(auth,60000);
            String refreshToken = tokenService.generateJwt(auth,300000);
            Cookie cookie = new Cookie("refreshToken", refreshToken);
            cookie.setPath("/login");
            cookie.setMaxAge(Instant.now().plusMillis(300000).getNano());
            cookie.setHttpOnly(true);
            response.addCookie(cookie);
            return new LoginResponseDTO(userRepository.findByUsername(username).get(), token);

        } catch(AuthenticationException e){
            //response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,e.getMessage());
            return new LoginResponseDTO(null, "");
        }
    }

}
