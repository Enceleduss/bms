package com.earthworm.bms.controller;

import com.earthworm.bms.repository.CustomerRepository;
import com.earthworm.bms.service.JsScopeService;
import com.earthworm.bms.service.UserService;
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

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String showRootPage() {
        return "redirect:/login";
    }

    @GetMapping("/index")
    public String showHomePage() {
        return "index";
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // No longer putting isAdmin here, it will be fetched by React via useGraalVMHook
        // jsScopeService.put("isAdmin", authentication.getAuthorities().stream()
        //         .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));

        model.addAttribute("username", username);
        model.addAttribute("serverTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        model.addAttribute("message", "Welcome to the Banking Management System!");
        model.addAttribute("isAdmin", authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));

        return "landing";
    }
}
