import java.util.Random;

public abstract class Tool 
{
    private int durability;
    private Random rnd;

    public Tool(int durability)
    {
        this.durability = durability;
        this.rnd = new Random();
    }

    public int getDurability()
    {
        return durability;
    }

    protected void setDurability(int durability)
    {
        this.durability = durability;
    }

    protected int randomNumber(int min, int max)
    {
        return min + rnd.nextInt(max - min + 1);
    }

    public abstract int useTool();
}