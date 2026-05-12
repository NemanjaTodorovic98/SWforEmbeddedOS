import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;

public class FortyNiner
{
    private int endurance;
    private int money;
    private ArrayList<Tool> tools;
    private Random rnd;

    public FortyNiner()
    {
        this(100, 100);
    }

    public FortyNiner(int endurance, int money)
    {
        this.endurance = endurance;
        this.money = money;
        this.rnd = new Random();

        this.tools = new ArrayList<Tool>();

        tools.add(new Pan());
        tools.add(new Sluice());
        tools.add(new Cradle());
    }

    public int getEndurance()
    {
        return endurance;
    }

    public void setEndurance(int endurance)
    {
        this.endurance = endurance;
    }

    public int getMoney()
    {
        return money;
    }

    public void setMoney(int money)
    {
        this.money = money;
    }

    public ArrayList<Tool> getTools()
    {
        return tools;
    }

    public void setTools(ArrayList<Tool> tools)
    {
        this.tools = tools;
    }

    public void useTools()
    {
        if (endurance <= 0)
        {
            System.out.println("Endurance je 0%. Ove sedmice nema zarade od alata.");
            return;
        }

        int totalIncome = 0;

        Iterator<Tool> it = tools.iterator();
        while (it.hasNext())
        {
            Tool tool = it.next();
            int income = tool.useTool();
            totalIncome = totalIncome + income;

            System.out.println("Alat " + tool.getClass().getSimpleName() + " je doneo $" + income + ".");

            if (tool instanceof Cradle && tool.getDurability() <= 0)
            {
                it.remove();
                System.out.println("Jedna Cradle se pokvarila i uklonjena je iz kolekcije alata.");
            }
        }

        money = money + totalIncome;
        System.out.println("Ukupna zarada ove sedmice je $" + totalIncome + ". Trenutno stanje novca: $" + money + ".");
    }

    public void buyFood()
    {
        int foodCost = randomNumber(30, 50);
        money = money - foodCost;
        System.out.println("Kupljena je hrana za $" + foodCost + ". Preostali novac: $" + money + ".");
    }

    public void loseEndurance()
    {
        int loss = randomNumber(10, 25);
        endurance = endurance - loss;
        if (endurance < 0)
        {
            endurance = 0;
        }
        System.out.println("Izgubljeno je " + loss + "% endurance. Trenutna endurance vrednost: " + endurance + "%.");
    }

    public void itIsSundayAgain(Scanner sc)
    {
        boolean validChoice = false;

        while (!validChoice)
        {
            System.out.println("Izaberi opciju:");
            System.out.println("1 - Ne radi nista");
            System.out.println("2 - Popravi Sluice ($100)");
            System.out.println("3 - Idi u saloon");

            if (!sc.hasNextInt())
            {
                System.out.println("Neispravan unos. Unesi broj 1, 2 ili 3.");
                sc.nextLine();
                continue;
            }

            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice)
            {
                case 1:
                {
                    System.out.println("Nisi radio nista ove nedelje.");
                    validChoice = true;
                    break;
                }
                case 2:
                {
                    fixSluice();
                    validChoice = true;
                    break;
                }
                case 3:
                {
                    goToSaloon();
                    validChoice = true;
                    break;
                }
                default:
                {
                    System.out.println("Neispravan izbor. Unesi 1, 2 ili 3.");
                    break;
                }
            }
        }
    }

    public void buyCradles(int count)
    {
        int canAfford = money / 30;

        if (canAfford == 0)
        {
            System.out.println("Nema dovoljno novca ni za jednu Cradle.");
            return;
        }

        int toBuy = count;
        if (count > canAfford)
        {
            System.out.println("Nije moguce kupiti " + count);
            toBuy = canAfford;
        }

        int i;
        for (i = 0; i < toBuy; i++)
        {
            tools.add(new Cradle());
            money = money - 30;
        }

        System.out.println("Kupljeno je " + toBuy + " Cradle za $" + (toBuy * 30) + ". Trenutno stanje novca: $" + money);
    }

    private void goToSaloon()
    {
        int price = randomNumber(50, 200);

        int gain = randomNumber(5, 50);

        money = money - price;
        endurance = endurance + gain;

        if (endurance > 100)
        {
            endurance = 100;
        }

        System.out.println("Saloon je kostao $" + price + ". Endurance se povecaoza " + gain + "%.");
        System.out.println("Trenutni novac: $" + money + ", trenutna endurance: " + endurance + "%.");
    }

    private void fixSluice()
    {
        if (money < 100)
        {
            System.out.println("Nedovoljo novca za popravku.");
            return;
        }

        Tool tool;
        int i;
        for (i = 0; i < tools.size(); i++)
        {
            tool = tools.get(i);
            if (tool instanceof Sluice)
            {
                ((Sluice) tool).repair();
                money = money - 100;
                System.out.println("Sluice je popravljen na 100%. Preostali novac: $" + money + ".");
                return;
            }
        }

        System.out.println("nije pronadjen medju alatima.");
    }

    private int randomNumber(int min, int max)
    {
        return min + rnd.nextInt(max - min + 1);
    }
}