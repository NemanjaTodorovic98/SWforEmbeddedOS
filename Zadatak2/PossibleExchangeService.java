import java.util.ArrayList;

public class PossibleExchangeService
{
    public PossibleExchange calculate(String firstUsername, String secondUsername, ArrayList<Integer> firstDuplicates, ArrayList<Integer> firstMissing, ArrayList<Integer> secondDuplicates, ArrayList<Integer> secondMissing)
    {
        PossibleExchange exchange = new PossibleExchange(firstUsername, secondUsername);

        int i;
        for (i = 0; i < firstDuplicates.size(); i++)
        {
            Integer sticker = firstDuplicates.get(i);

            if (secondMissing.contains(sticker))
            {
                exchange.getFirstHasForSecond().add(sticker);
            }
        }

        for (i = 0; i < secondDuplicates.size(); i++)
        {
            Integer sticker = secondDuplicates.get(i);

            if (firstMissing.contains(sticker))
            {
                exchange.getSecondHasForFirst().add(sticker);
            }
        }

        return exchange;
    }
}
