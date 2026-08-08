package com.earthworm.bms.controller;

import com.earthworm.bms.model.CustomerRecord;
import com.earthworm.bms.repository.CustomerRepository;
import com.earthworm.bms.service.reactive.NodeChangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class UserController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private NodeChangeService nodeChangeService;

    @PostMapping("/api/user/update-name")
    public ResponseEntity<?> updateName(@RequestParam String newName, Authentication authentication) {
        String username = authentication.getName();
        Optional<CustomerRecord> userOpt = customerRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            CustomerRecord user = userOpt.get();
            user.setName(newName);
            customerRepository.save(user);

            // Notify the reactive system that this node has changed
            nodeChangeService.notifyChange(user.getId());

            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
