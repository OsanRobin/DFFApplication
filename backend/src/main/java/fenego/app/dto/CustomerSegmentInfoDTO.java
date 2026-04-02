package fenego.app.dto;

public class CustomerSegmentInfoDTO
{
    private int total;
    private int limit;
    private int offset;
    private boolean hasMoreElements;

    public CustomerSegmentInfoDTO() {}

    public int getTotal()
    {
        return total;
    }

    public void setTotal(int total)
    {
        this.total = total;
    }

    public int getLimit()
    {
        return limit;
    }

    public void setLimit(int limit)
    {
        this.limit = limit;
    }

    public int getOffset()
    {
        return offset;
    }

    public void setOffset(int offset)
    {
        this.offset = offset;
    }

    public boolean isHasMoreElements()
    {
        return hasMoreElements;
    }

    public void setHasMoreElements(boolean hasMoreElements)
    {
        this.hasMoreElements = hasMoreElements;
    }
}