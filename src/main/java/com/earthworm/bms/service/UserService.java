package com.earthworm.bms.service;

import com.earthworm.bms.model.CustomerRecord;
import com.earthworm.bms.model.datapojos.CustomerDTO;
import com.earthworm.bms.repository.CustomerRepository;
import com.earthworm.bms.repository.GraphRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private GraphRepository graphRepository;

    /**
     * Retrieves the CustomerDTO for the currently authenticated user.
     * This method is designed to be called from the GraalVM JavaScript context.
     */
    public CustomerDTO getCurrentUserDTO() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Optional<CustomerRecord> userOpt = graphRepository.findById(52);

        if (userOpt.isPresent()) {

            CustomerRecord user = userOpt.get();
            System.out.println("user found with id "+user.getId());
            return new CustomerDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getUsername(),
                user.getAcctype(),
                user.getBalance()
            );
        }
        // Return a default/empty DTO or throw an exception if user not found
        // For now, returning null might cause JS errors, so a default DTO is safer.
        return new CustomerDTO(0L, "Guest", "guest@example.com", "guest", "N/A", 0.0);
    }
}
