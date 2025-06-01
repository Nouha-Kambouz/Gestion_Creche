package com.example.gestion_creche.controller;
import com.example.gestion_creche.model.User;
import com.example.gestion_creche.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterForm() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String email, 
                             @RequestParam String password,
                             RedirectAttributes redirectAttributes) {
        try {
            User user = new User();
            user.setEmail(email);
            user.setPassword(password);
            userService.registerUser(user, "ROLE_PARENT");
            return "redirect:/login?registered=true";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Registration failed. Email might already be in use.");
            return "redirect:/register";
        }
    }
}