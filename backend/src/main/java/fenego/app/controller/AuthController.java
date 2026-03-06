package fenego.app.controller;

import fenego.app.dto.CurrentUserResponse;
import fenego.app.dto.IntershopLoginResult;
import fenego.app.dto.LoginRequest;
import fenego.app.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class AuthController
{
    private final AuthService authService;

    public AuthController(AuthService authService)
    {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpSession session)
    {
        try
        {
            IntershopLoginResult result = authService.login(request);

            session.setAttribute("authenticated", true);
            session.setAttribute("user", result.getUser());
            session.setAttribute("organization", result.getOrganization());
            session.setAttribute("intershopToken", result.getAuthenticationToken());

            return ResponseEntity.ok(new CurrentUserResponse(result.getUser(), result.getOrganization()));
        }
        catch (RuntimeException ex)
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session)
    {
        Boolean authenticated = (Boolean) session.getAttribute("authenticated");

        if (authenticated == null || !authenticated)
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }

        String user = (String) session.getAttribute("user");
        String organization = (String) session.getAttribute("organization");

        return ResponseEntity.ok(new CurrentUserResponse(user, organization));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session)
    {
        session.invalidate();
        return ResponseEntity.noContent().build();
    }
}