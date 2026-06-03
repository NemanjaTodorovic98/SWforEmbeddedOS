import java.util.ArrayList;

public class PossibleExchange
{
    private String firstUsername;
    private String secondUsername;
    private ArrayList<Integer> firstHasForSecond;
    private ArrayList<Integer> secondHasForFirst;

    public PossibleExchange(String firstUsername, String secondUsername)
    {
        this.firstUsername = firstUsername;
        this.secondUsername = secondUsername;
        this.firstHasForSecond = new ArrayList<Integer>();
        this.secondHasForFirst = new ArrayList<Integer>();
    }

    public String getFirstUsername()
    {
        return firstUsername;
    }


    public String getSecondUsername()
    {
    return secondUsername;
    }

    public ArrayList<Integer> getFirstHasForSecond()
    {
        return firstHasForSecond;
    }

    public ArrayList<Integer> getSecondHasForFirst()
    {
        return secondHasForFirst;
    }

    public boolean hasExchange()
    {
        return firstHasForSecond.size() > 0 && secondHasForFirst.size() > 0;
    }
}
