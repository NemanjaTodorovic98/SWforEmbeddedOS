import java.util.ArrayList;

public class PossibleExchangeTest
{
    public static void main(String[] args)
    {
        ArrayList<Integer> user1Duplicates = new ArrayList<Integer>();
        user1Duplicates.add(Integer.valueOf(3));
        user1Duplicates.add(Integer.valueOf(17));
        user1Duplicates.add(Integer.valueOf(45));
        user1Duplicates.add(Integer.valueOf(70));

        ArrayList<Integer> user1Missing = new ArrayList<Integer>();
        user1Missing.add(Integer.valueOf(9));
        user1Missing.add(Integer.valueOf(36));
        user1Missing.add(Integer.valueOf(56));
        user1Missing.add(Integer.valueOf(91));

        ArrayList<Integer> user2Duplicates = new ArrayList<Integer>();
        
            user2Duplicates.add(Integer.valueOf(36));
        user2Duplicates.add(Integer.valueOf(56));
        user2Duplicates.add(Integer.valueOf(91));
        user2Duplicates.add(Integer.valueOf(99));

        ArrayList<Integer> user2Missing = new ArrayList<Integer>();
        user2Missing.add(Integer.valueOf(3));
        user2Missing.add(Integer.valueOf(17));
        user2Missing.add(Integer.valueOf(45));
        user2Missing.add(Integer.valueOf(60));

        PossibleExchangeService service = new PossibleExchangeService();
        PossibleExchange exchange = service.calculate("user1", "user2", user1Duplicates, user1Missing, user2Duplicates, user2Missing);


        System.out.println("User1 ima za drugog: " + exchange.getFirstHasForSecond());
        System.out.println("User2 ima za prvog: " + exchange.getSecondHasForFirst());
        System.out.println("postoji razmena: " + exchange.hasExchange());
    }
}
