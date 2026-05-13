import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class GoldRush
{
    private FortyNiner fortyNiner;
    private File savedGame;
    private int startWeek;

    public GoldRush()
    {
        this.fortyNiner = null;

        this.savedGame = new File("Zadatak1\\savedGame.txt");
        this.startWeek = 1;
    }

    public void survive()
    {
        if (fortyNiner == null)
        {
            fortyNiner = new FortyNiner();
        }

        Scanner sc = new Scanner(System.in);

        int week;
        for (week = startWeek; week <= 20; week++)
        {
            System.out.println("==================");
            System.out.println("Sedmica " + week);

            fortyNiner.itIsSundayAgain(sc);

            System.out.println("Koliko novih Cradle?");

            int cradlesToBuy = readNonNegativeInt(sc);
            fortyNiner.buyCradles(cradlesToBuy);

            fortyNiner.useTools();
            fortyNiner.buyFood();
            fortyNiner.loseEndurance();

            System.out.println("Da li zelis da prekines igru? (da/ne)");
            String stop = sc.nextLine();
            if (stop.equalsIgnoreCase("da"))
            {
                saveGame(week);
                System.out.println("Sacuvan trenutni progres.");
                return;
            }

            System.out.println("Kraj sedmice " + week + ". Novac: $" + fortyNiner.getMoney() + ", endurance: " + fortyNiner.getEndurance() + "%.");
        }

        if (savedGame.exists())
        {
            savedGame.delete();
        }

        System.out.println("==============================");
        System.out.println("Kraj igre, prikupljn novac: $" + fortyNiner.getMoney());
    }

    public void loadGame()
    {
        if (!savedGame.exists())
        {
            return;
        }

        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(savedGame));

            String line = reader.readLine();

            int savedWeek = extractNumber(line);

            line = reader.readLine();
            int endurance = extractNumber(line);

            line = reader.readLine();
            int money = extractNumber(line);

            line = reader.readLine();
            int sluiceDurability = extractNumber(line);

            ArrayList<Tool> loadedTools = new ArrayList<Tool>();
            loadedTools.add(new Pan());
            loadedTools.add(new Sluice(sluiceDurability));

            while ((line = reader.readLine()) != null)
            {
                if (line.trim().length() == 0)
                {
                    continue;
                }

                int cradleDurability = extractNumber(line);
                loadedTools.add(new Cradle(cradleDurability));
            }

            reader.close();

            fortyNiner = new FortyNiner(endurance, money);
            fortyNiner.setTools(loadedTools);
            startWeek = savedWeek + 1;

            if (savedWeek >= 20)
            {
                if (savedGame.exists())
                {
                    savedGame.delete();
                }

                fortyNiner = null;
                startWeek = 1;
                System.out.println("pokrece se nova igra.");
                return;
            }

            System.out.println("Ucitana je prethodno sacuvana igra. Nastavak od sedmice " + startWeek + ".");
        }
        catch (IOException ex)
        {
            System.out.println("Greska pri ucitavanju igre: " + ex.getMessage());
        }
    }

    private void saveGame(int week)
    {
        try
        {
            BufferedWriter writer = new BufferedWriter(new FileWriter(savedGame));

            writer.write("Week no. " + week);
            writer.newLine();
            writer.write("49er endurance: " + fortyNiner.getEndurance() + "%");
            writer.newLine();
            writer.write("49er money: $" + fortyNiner.getMoney());
            writer.newLine();
            writer.write("Sluice durability: " + getSluiceDurability() + "%");
            writer.newLine();

            ArrayList<Tool> tools = fortyNiner.getTools();
            for (int i = 0; i < tools.size(); i++)
            {
                Tool tool = tools.get(i);

                
                if (tool instanceof Cradle)
                {
                    writer.write("Cradle durability: " + tool.getDurability() + "%");
                    writer.newLine();
                }
            }

            writer.close();
        }
        catch (IOException ex)
        {
            System.out.println("Nije moguce sacuvati igru. " + ex.getMessage());
        }
    }

    private int getSluiceDurability()
    {
        ArrayList<Tool> tools = fortyNiner.getTools();
        for (int i = 0; i < tools.size(); i++)
        {
            Tool tool = tools.get(i);

            if (tool instanceof Sluice)
            {
                return tool.getDurability();
            }
        }

        return 0;
    }

    private int extractNumber(String line)
    {
        if (line.indexOf(':') >= 0)
        {
            line = line.substring(line.indexOf(':') + 1);
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < line.length(); i++)
        {
            char c = line.charAt(i);

            if (c >= '0' && c <= '9')
            {
                sb.append(c);
            }
        }


        return Integer.parseInt(sb.toString());
    }

    private int readNonNegativeInt(Scanner sc)
    {
        while (true)
        {
            System.out.println("Unesite broj.");
            if (!sc.hasNextInt())
            {
                sc.nextLine();
                continue;
            }

            int value = sc.nextInt();
            sc.nextLine();

            if (value < 0)
            {
                continue;
            }

            return value;
        }
    }
}