public class Sluice extends Tool {
    public Sluice()
    {
        this(100);
    }

    public Sluice(int durability)
    {
        super(durability);
    }

    @Override public int useTool()
    {
        if (getDurability() <= 0)
        {
            return 0;
        }

        int durabilityAfterUse = getDurability() - randomNumber(20, 50);

        if (durabilityAfterUse < 0)
        {
            durabilityAfterUse = 0;
        }
        setDurability(durabilityAfterUse);
        
        return randomNumber(0, 500);
    }

    public void repair()
    {
        setDurability(100);
    }
}