package com.earthworm.bms.controller;

import com.earthworm.bms.model.datapojos.CustomerDTO;
import com.earthworm.bms.repository.CustomerRepository;
import com.earthworm.bms.service.JsScopeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class ThymeleafAppController {

    @Autowired
    private JsScopeService jsScopeService;

    @Autowired
    private CustomerRepository customerRepository;

    @GetMapping("/")
    public String showRootPage() {
        return "redirect:/login";
    }

    @GetMapping("/index")
    public String showHomePage() {
        return "index"; // Refers to src/main/resources/templates/home.html
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    @GetMapping("/landing")
    public String showLandingPage(Model model) {
        // 1. Get the currently logged-in user's details
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // 2. Populate the JavaScript Scope with user data
        customerRepository.findByUsername(username).ifPresent(user -> {
            // Create a DTO to decouple the view from the JPA entity proxy
            CustomerDTO userDto = new CustomerDTO(
                user.getName(),
                user.getEmail(),
                user.getUsername(),
                user.getAcctype(),
                user.getBalance()
            );
            
            // Put the clean DTO into the scope
            jsScopeService.put("user", userDto);

            jsScopeService.put("isAdmin", authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        });

        // 3. Add data to the Model to be used in the view
        model.addAttribute("username", username);
        
        // Adding more dynamic data
        model.addAttribute("serverTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        model.addAttribute("message", "Welcome to the Banking Management System!");
        model.addAttribute("isAdmin", authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));

        // 4. Return the name of the Thymeleaf template (landing.html)
        return "landing";
    }
}
