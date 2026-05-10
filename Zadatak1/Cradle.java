public class Cradle extends Tool
{
    public Cradle()
    {
        this(100);
    }

    public Cradle(int durability)
    {
        super(durability);
    }

    @Override public int useTool()
    {
        if (getDurability() <= 0)
        {
            return 0;
        }

        if (randomNumber(1, 100) <= 20)
        {
            setDurability(0);
        }
        
        return randomNumber(0, 30);
    }
}