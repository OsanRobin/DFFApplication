package fenego.app.dto;

import java.util.ArrayList;
import java.util.List;

public class CustomerUserDetailResponse
{
    private CustomerUserDTO user;
    private List<CustomerUserAttributeDTO> attributes = new ArrayList<>();

    public CustomerUserDTO getUser()
    {
        return user;
    }

    public void setUser(CustomerUserDTO user)
    {
        this.user = user;
    }

    public List<CustomerUserAttributeDTO> getAttributes()
    {
        return attributes;
    }

    public void setAttributes(List<CustomerUserAttributeDTO> attributes)
    {
        this.attributes = attributes;
    }
}