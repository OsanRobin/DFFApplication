package fenego.app.controller;

import fenego.app.dto.CurrentUserResponse;
import fenego.app.dto.IntershopLoginResult;
import fenego.app.dto.LoginRequest;
import fenego.app.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

            List<String> roles = result.getRoles() == null ? List.of() : result.getRoles();
            boolean managerRestricted = roles.stream().anyMatch(role -> "manager".equalsIgnoreCase(role));

            session.setAttribute("authenticated", true);
            session.setAttribute("user", result.getUser());
            session.setAttribute("organization", result.getOrganization());
            session.setAttribute("intershopToken", result.getAuthenticationToken());
            session.setAttribute("roles", roles);
            session.setAttribute("managerRestricted", managerRestricted);

            return ResponseEntity.ok(new CurrentUserResponse(
                    result.getUser(),
                    result.getOrganization(),
                    roles,
                    managerRestricted
            ));
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

        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) session.getAttribute("roles");

        boolean managerRestricted = Boolean.TRUE.equals(session.getAttribute("managerRestricted"));

        return ResponseEntity.ok(new CurrentUserResponse(
                user,
                organization,
                roles == null ? List.of() : roles,
                managerRestricted
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session)
    {
        session.invalidate();
        return ResponseEntity.noContent().build();
    }
}