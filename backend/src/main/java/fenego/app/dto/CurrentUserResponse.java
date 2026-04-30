package fenego.app.dto;

import java.util.List;

public class CurrentUserResponse
{
    private String user;
    private String organization;
    private List<String> roles;
    private boolean managerRestricted;

    public CurrentUserResponse()
    {
    }

    public CurrentUserResponse(String user, String organization)
    {
        this.user = user;
        this.organization = organization;
        this.roles = List.of();
        this.managerRestricted = false;
    }

    public CurrentUserResponse(String user, String organization, List<String> roles, boolean managerRestricted)
    {
        this.user = user;
        this.organization = organization;
        this.roles = roles;
        this.managerRestricted = managerRestricted;
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

    public List<String> getRoles()
    {
        return roles;
    }

    public void setRoles(List<String> roles)
    {
        this.roles = roles;
    }

    public boolean isManagerRestricted()
    {
        return managerRestricted;
    }

    public void setManagerRestricted(boolean managerRestricted)
    {
        this.managerRestricted = managerRestricted;
    }
}