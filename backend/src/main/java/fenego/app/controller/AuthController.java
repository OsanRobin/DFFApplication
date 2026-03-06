package fenego.app.controller;

import fenego.app.service.AuthService;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController
{
    private final AuthService authService;

    public AuthController(AuthService authService)
    {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login()
    {
        boolean success = authService.login();

        if (success)
        {
            return ResponseEntity.ok("Login successful");
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login failed");
    }
    @GetMapping("/me")
public Map<String, String> me() {
    Map<String, String> user = new HashMap<>();
    user.put("user", "admin");
    user.put("organization", "DailyFreshFood-B1-Site");
    return user;
}
}