package com.Group5.ISMS.controller;

import com.Group5.ISMS.dto.ApiResponse;
import com.Group5.ISMS.dto.LoginRequest;
import com.Group5.ISMS.dto.RegisterRequest;
import com.Group5.ISMS.dto.UserDto;
import com.Group5.ISMS.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;
    public static final String SESSION_USER_KEY = "LOGGED_IN_USER";

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping({"/", "/index", "/dashboard"})
    public String showDashboard(HttpSession session, Model model) {
        UserDto user = (UserDto) session.getAttribute(SESSION_USER_KEY);
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "index";
    }

    @GetMapping("/h2-console")
    public String redirectH2Console() {
        return "redirect:/h2-console/";
    }

    @GetMapping("/login")
    public String showLoginForm(HttpSession session, Model model) {
        if (session.getAttribute(SESSION_USER_KEY) != null) {
            return "redirect:/dashboard";
        }
        if (!model.containsAttribute("loginRequest")) {
            model.addAttribute("loginRequest", new LoginRequest());
        }
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@ModelAttribute("loginRequest") LoginRequest request,
                               HttpSession session,
                               Model model) {
        try {
            UserDto user = userService.login(request);
            session.setAttribute(SESSION_USER_KEY, user);
            return "redirect:/dashboard";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "login";
        } catch (Exception e) {
            model.addAttribute("error", "Login error: " + e.getMessage());
            return "login";
        }
    }

    @GetMapping("/register")
    public String showRegisterForm(HttpSession session, Model model) {
        if (session.getAttribute(SESSION_USER_KEY) != null) {
            return "redirect:/dashboard";
        }
        if (!model.containsAttribute("registerRequest")) {
            model.addAttribute("registerRequest", new RegisterRequest());
        }
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(@ModelAttribute("registerRequest") RegisterRequest request,
                                  @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {
        if (request.getPassword() != null && confirmPassword != null && !request.getPassword().equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match!");
            return "register";
        }

        try {
            userService.register(request);
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please sign in.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            model.addAttribute("error", "Username or email is already in use. Please use a different one.");
            return "register";
        } catch (Exception e) {
            model.addAttribute("error", "Registration error: " + e.getMessage());
            return "register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        if (session != null) {
            session.invalidate();
        }
        redirectAttributes.addFlashAttribute("success", "You have been logged out successfully.");
        return "redirect:/login";
    }

    @GetMapping("/products")
    public String showProducts(HttpSession session, Model model) {
        UserDto user = (UserDto) session.getAttribute(SESSION_USER_KEY);
        if (user == null) return "redirect:/login";
        model.addAttribute("user", user);
        return "products";
    }

    @GetMapping("/suppliers")
    public String showSuppliers(HttpSession session, Model model) {
        UserDto user = (UserDto) session.getAttribute(SESSION_USER_KEY);
        if (user == null) return "redirect:/login";
        model.addAttribute("user", user);
        return "suppliers";
    }

    @GetMapping("/stock")
    public String showStock(HttpSession session, Model model) {
        UserDto user = (UserDto) session.getAttribute(SESSION_USER_KEY);
        if (user == null) return "redirect:/login";
        model.addAttribute("user", user);
        return "stock";
    }

    @GetMapping("/users")
    public String showUsers(HttpSession session, Model model) {
        UserDto user = (UserDto) session.getAttribute(SESSION_USER_KEY);
        if (user == null) return "redirect:/login";
        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            return "redirect:/dashboard";
        }
        model.addAttribute("user", user);
        return "users";
    }

    @ResponseBody
    @PostMapping("/api/auth/login")
    public ResponseEntity<ApiResponse<UserDto>> apiLogin(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {
            UserDto user = userService.login(request);
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute(SESSION_USER_KEY, user);
            return ResponseEntity.ok(ApiResponse.ok("Login successful", user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(e.getMessage()));
        }
    }

    @ResponseBody
    @PostMapping("/api/auth/register")
    public ResponseEntity<ApiResponse<UserDto>> apiRegister(@RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        try {
            UserDto createdUser = userService.register(request);
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute(SESSION_USER_KEY, createdUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Registration successful", createdUser));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("Username or email is already in use. Please use a different one."));
        }
    }

    @ResponseBody
    @PostMapping("/api/auth/logout")
    public ResponseEntity<ApiResponse<Void>> apiLogout(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok(ApiResponse.ok("Logged out"));
    }

    @ResponseBody
    @GetMapping("/api/auth/me")
    public ResponseEntity<ApiResponse<UserDto>> apiMe(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            UserDto user = (UserDto) session.getAttribute(SESSION_USER_KEY);
            if (user != null) {
                return ResponseEntity.ok(ApiResponse.ok("Authenticated", user));
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Not authenticated"));
    }
}
