public class Pan extends Tool 
{
    public Pan()
    {
        this(100);
    }

    public Pan(int durability)
    {
        super(durability);
    }

    @Override public int useTool()
    {
        if (getDurability() <= 0)
        {
            return 0;
        }
        else
        {
            return randomNumber(0, 60);
        }
    }
}