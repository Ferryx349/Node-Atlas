package com.project1.shriganeshaynamah.Controllers;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.project1.shriganeshaynamah.Dao.userDao;
import com.project1.shriganeshaynamah.helper.Msg;
import com.project1.shriganeshaynamah.service.FileStorageService;
import com.project1.shriganeshaynamah.user.User;

import jakarta.servlet.http.HttpSession;

@Controller
public class ProfileController {

    @Autowired
    private userDao userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private FileStorageService fileStorageService;

    @ModelAttribute
    public void addLoggedInUser(Model model, Principal principal) {
        if (principal != null) {
            model.addAttribute("user", userRepository.findByName(principal.getName()));
        }
    }

    @GetMapping("/user/profile")
    public String profilePage(Model model, HttpSession session) {
        if (session.getAttribute("message") != null) {
            model.addAttribute("message", session.getAttribute("message"));
            session.removeAttribute("message");
        }
        model.addAttribute("pageTitle", "My Profile");
        model.addAttribute("activePage", "profile");
        return "user/profile";
    }

    @PostMapping("/user/profile/update")
    public String updateProfile(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam(required = false) String about,
            Principal principal,
            HttpSession session) {

        User user = userRepository.findByName(principal.getName());

        if (userRepository.existsByEmail(email) && !email.equals(user.getEmail())) {
            session.setAttribute("message", new Msg("Email is already in use.", "alert-danger"));
            return "redirect:/user/profile";
        }

        if (userRepository.existsByName(name) && !name.equals(user.getName())) {
            session.setAttribute("message", new Msg("Username is already taken.", "alert-danger"));
            return "redirect:/user/profile";
        }

        user.setName(name);
        user.setEmail(email);
        user.setAbout(about);
        userRepository.save(user);

        session.setAttribute("message", new Msg("Profile updated successfully.", "alert-success"));
        return "redirect:/user/profile";
    }

    @PostMapping("/user/profile/photo")
    public String uploadProfilePhoto(
            @RequestParam("photo") MultipartFile photo,
            @RequestParam(value = "removePhoto", defaultValue = "false") boolean removePhoto,
            Principal principal,
            HttpSession session) {

        User user = userRepository.findByName(principal.getName());

        try {
            if (removePhoto) {
                fileStorageService.deleteIfExists(user.getImageurl());
                user.setImageurl(null);
            } else {
                fileStorageService.deleteIfExists(user.getImageurl());
                user.setImageurl(fileStorageService.storeUserImage(photo, user.getId()));
            }
            userRepository.save(user);
            session.setAttribute("message", new Msg("Profile photo updated.", "alert-success"));
        } catch (IllegalArgumentException ex) {
            session.setAttribute("message", new Msg(ex.getMessage(), "alert-danger"));
        }

        return "redirect:/user/profile";
    }

    @PostMapping("/user/profile/password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Principal principal,
            HttpSession session) {

        User user = userRepository.findByName(principal.getName());

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            session.setAttribute("message", new Msg("Current password is incorrect.", "alert-danger"));
            return "redirect:/user/profile";
        }

        if (newPassword.length() < 6) {
            session.setAttribute("message", new Msg("New password must be at least 6 characters.", "alert-danger"));
            return "redirect:/user/profile";
        }

        if (!newPassword.equals(confirmPassword)) {
            session.setAttribute("message", new Msg("New passwords do not match.", "alert-danger"));
            return "redirect:/user/profile";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        session.setAttribute("message", new Msg("Password changed successfully.", "alert-success"));
        return "redirect:/user/profile";
    }
}
