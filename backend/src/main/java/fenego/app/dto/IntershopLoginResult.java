package fenego.app.dto;

import java.util.List;

public class IntershopLoginResult
{
    private String user;
    private String organization;
    private String authenticationToken;
    private List<String> roles;

    public IntershopLoginResult()
    {
    }

    public IntershopLoginResult(String user, String organization, String authenticationToken)
    {
        this.user = user;
        this.organization = organization;
        this.authenticationToken = authenticationToken;
        this.roles = List.of();
    }

    public IntershopLoginResult(String user, String organization, String authenticationToken, List<String> roles)
    {
        this.user = user;
        this.organization = organization;
        this.authenticationToken = authenticationToken;
        this.roles = roles;
    }

    public String getUser()
    {
        return user;
    }

    public void setUser(String user)
    {
        this.user = user;
    }

    public String getOrganization()
    {
        return organization;
    }

    public void setOrganization(String organization)
    {
        this.organization = organization;
    }

    public String getAuthenticationToken()
    {
        return authenticationToken;
    }

    public void setAuthenticationToken(String authenticationToken)
    {
        this.authenticationToken = authenticationToken;
    }

    public List<String> getRoles()
    {
        return roles;
    }

    public void setRoles(List<String> roles)
    {
        this.roles = roles;
    }
}