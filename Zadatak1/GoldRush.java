import java.util.Scanner;

public class GoldRush
{
    private FortyNiner fortyNiner;

    public GoldRush()
    {
        this.fortyNiner = null;
    }

    public void survive()
    {
        if (fortyNiner == null)
        {
            fortyNiner = new FortyNiner();
        }

        Scanner sc = new Scanner(System.in);

        int week;
        for (week = 1; week <= 20; week++)
        {
            System.out.println("==================");
            System.out.println("Sedmica " + week);

            fortyNiner.useTools();
            fortyNiner.buyFood();
            fortyNiner.loseEndurance();

            System.out.println("Da li zelis da prekines igru? (da/ne)");
            String stop = sc.nextLine();
            if (stop.equalsIgnoreCase("da"))
            {
                System.out.println("Igra je prekinuta.");
                break;
            }

            if (week < 20)
            {
                fortyNiner.itIsSundayAgain(sc);

                System.out.println("Koliko Cradle zelis da kupis ove nedelje?");
                int cradlesToBuy = sc.nextInt();
                sc.nextLine();
                fortyNiner.buyCradles(cradlesToBuy);
            }

            System.out.println("Kraj sedmice " + week + ". Novac: $" + fortyNiner.getMoney() + ", endurance: " + fortyNiner.getEndurance() + "%.");
        }

        System.out.println("==============================");
        System.out.println("Kraj igre, novac: $" + fortyNiner.getMoney());
    }
}