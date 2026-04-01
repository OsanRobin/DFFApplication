package fenego.app.dto;

public class AttributeOptionDTO
{
    private String name;
    private String displayName;

    public AttributeOptionDTO() {}

    public AttributeOptionDTO(String name, String displayName)
    {
        this.name = name;
        this.displayName = displayName;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }
}