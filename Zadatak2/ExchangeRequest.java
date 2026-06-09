import java.util.ArrayList;

public class ExchangeRequest
{
    private String fromUsername;
    private String toUsername;
    private ArrayList<Integer> tickersFromGives;
    private ArrayList<Integer> tickersFromWants;

    public ExchangeRequest(String fromUsername, String toUsername, ArrayList<Integer> tickersFromGives, ArrayList<Integer> tickersFromWants)
    {
        this.fromUsername = fromUsername;
        this.toUsername = toUsername;
        this.tickersFromGives = new ArrayList<Integer>(tickersFromGives);
        this.tickersFromWants = new ArrayList<Integer>(tickersFromWants);
    }

    public String getFromUsername()
    {
        return fromUsername;
    }

    public String getToUsername()
    {
        return toUsername;
    }

    public ArrayList<Integer> getTickersFromGives()
    {
        return tickersFromGives;
    }

    public ArrayList<Integer> getTickersFromWants()
    {
        return tickersFromWants;
    }
}
