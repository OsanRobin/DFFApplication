package fenego.app.dto;

import java.util.ArrayList;
import java.util.List;

public class CustomerUserDTO
{
    private String name;
    private String login;
    private String firstName;
    private String lastName;
    private boolean active;
    private String businessPartnerNo;
    private List<String> roleIds = new ArrayList<>();
    private List<String> roleNames = new ArrayList<>();
    private String budgetPeriod;
    private int pendingOneTimeRequisitionsCount;
    private int pendingRecurringRequisitionsCount;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getLogin()
    {
        return login;
    }

    public void setLogin(String login)
    {
        this.login = login;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    public boolean isActive()
    {
        return active;
    }

    public void setActive(boolean active)
    {
        this.active = active;
    }

    public String getBusinessPartnerNo()
    {
        return businessPartnerNo;
    }

    public void setBusinessPartnerNo(String businessPartnerNo)
    {
        this.businessPartnerNo = businessPartnerNo;
    }

    public List<String> getRoleIds()
    {
        return roleIds;
    }

    public void setRoleIds(List<String> roleIds)
    {
        this.roleIds = roleIds;
    }

    public List<String> getRoleNames()
    {
        return roleNames;
    }

    public void setRoleNames(List<String> roleNames)
    {
        this.roleNames = roleNames;
    }

    public String getBudgetPeriod()
    {
        return budgetPeriod;
    }

    public void setBudgetPeriod(String budgetPeriod)
    {
        this.budgetPeriod = budgetPeriod;
    }

    public int getPendingOneTimeRequisitionsCount()
    {
        return pendingOneTimeRequisitionsCount;
    }

    public void setPendingOneTimeRequisitionsCount(int pendingOneTimeRequisitionsCount)
    {
        this.pendingOneTimeRequisitionsCount = pendingOneTimeRequisitionsCount;
    }

    public int getPendingRecurringRequisitionsCount()
    {
        return pendingRecurringRequisitionsCount;
    }

    public void setPendingRecurringRequisitionsCount(int pendingRecurringRequisitionsCount)
    {
        this.pendingRecurringRequisitionsCount = pendingRecurringRequisitionsCount;
    }
}