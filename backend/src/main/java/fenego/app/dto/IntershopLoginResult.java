package fenego.app.dto;

public class IntershopLoginResult
{
    private String user;
    private String organization;
    private String authenticationToken;

    public IntershopLoginResult()
    {
    }

    public IntershopLoginResult(String user, String organization, String authenticationToken)
    {
        this.user = user;
        this.organization = organization;
        this.authenticationToken = authenticationToken;
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
}