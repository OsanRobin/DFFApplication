package fenego.app.jpa;

public class CustomerSegmentAssignment
{
    private String customerNo;
    private String segmentId;

    public String getCustomerNo()
    {
        return customerNo;
    }

    public void setCustomerNo(String customerNo)
    {
        this.customerNo = customerNo;
    }

    public String getSegmentId()
    {
        return segmentId;
    }

    public void setSegmentId(String segmentId)
    {
        this.segmentId = segmentId;
    }
}