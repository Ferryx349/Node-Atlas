package com.project1.shriganeshaynamah.Controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.project1.shriganeshaynamah.Dao.userDao;
import com.project1.shriganeshaynamah.helper.Msg;
import com.project1.shriganeshaynamah.user.User;

@Controller
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private userDao userRepository;

    @GetMapping("/signup")
    public String signupPage(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping("/regis")
    public String registerUser(
            @ModelAttribute User user,
            @RequestParam(value = "agreement", defaultValue = "false") boolean agreement,
            Model model) {

        if (!agreement) {
            model.addAttribute("user", user);
            model.addAttribute("message", new Msg("Please accept the terms and conditions.", "alert-danger"));
            return "signup";
        }

        if (userRepository.existsByName(user.getName())) {
            model.addAttribute("user", user);
            model.addAttribute("message", new Msg("Username already taken. Please choose another.", "alert-danger"));
            return "signup";
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            model.addAttribute("user", user);
            model.addAttribute("message", new Msg("Email already registered.", "alert-danger"));
            return "signup";
        }

        try {
            user.setRole("USER");
            user.setEnable("true");
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            User savedUser = userRepository.save(user);
            log.info("New user registered: {}", savedUser.getName());
            model.addAttribute("user", new User());
            model.addAttribute("message", new Msg("Successfully registered! You can now sign in.", "alert-success"));
            return "signup";
        } catch (Exception e) {
            log.error("Registration failed", e);
            model.addAttribute("user", user);
            model.addAttribute("message", new Msg("Something went wrong. Please try again.", "alert-danger"));
            return "signup";
        }
    }
}
