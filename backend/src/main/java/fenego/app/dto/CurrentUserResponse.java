package fenego.app.dto;

public class CurrentUserResponse
{
    private String user;
    private String organization;

    public CurrentUserResponse()
    {
    }

    public CurrentUserResponse(String user, String organization)
    {
        this.user = user;
        this.organization = organization;
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
}