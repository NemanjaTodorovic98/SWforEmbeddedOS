import java.util.ArrayList;
import java.util.Random;

public class StickerUser
{
    private String username;
    private ArrayList<Integer> duplicates;
    private ArrayList<Integer> missing;
    private Random random;

    public StickerUser(String username)
    {
        this.username = username;
        this.duplicates = new ArrayList<Integer>();
        this.missing = new ArrayList<Integer>();
        this.random = new Random();

        generateInitialData();
    }

    public StickerUser(String username, ArrayList<Integer> duplicates, ArrayList<Integer> missing)
    {
        this.username = username;
        this.duplicates = new ArrayList<Integer>();
        this.missing = new ArrayList<Integer>();
        this.random = new Random();

        copyValidValues(duplicates, this.duplicates);
        copyValidValues(missing, this.missing);

        removeOverlaps();
    }

    public String getUsername()
    {
        return username;
    }

    public ArrayList<Integer> getDuplicates()
    {
        return duplicates;
    }

    public ArrayList<Integer> getMissing()
    {
        return missing;
    }

    public void addDuplicate(int stickerNumber)
    {
        if (stickerNumber < 1 || stickerNumber > 99)
        {
            return;
        }

        Integer value = Integer.valueOf(stickerNumber);

        if (!duplicates.contains(value) && !missing.contains(value))
        {
            duplicates.add(value);
        }
    }

    public void addMissing(int stickerNumber)
    {
        if (stickerNumber < 1 || stickerNumber > 99)
        {
            return;
        }

        Integer value = Integer.valueOf(stickerNumber);

        if (!missing.contains(value) && !duplicates.contains(value))
        {
            missing.add(value);
        }
    }

    public void removeDuplicate(int stickerNumber)
    {
        duplicates.remove(Integer.valueOf(stickerNumber));
    }

    public void removeMissing(int stickerNumber)
    {
        missing.remove(Integer.valueOf(stickerNumber));
    }

    private void generateInitialData()
    {
        int duplicateCount = randomStickerCount();
        int missingCount = randomStickerCount();

        generateRandomList(duplicates, duplicateCount);
        generateRandomList(missing, missingCount);
    }

    private void generateRandomList(ArrayList<Integer> list, int count)
    {
        while (list.size() < count)
        {
            Integer stickerNumber = Integer.valueOf(random.nextInt(99) + 1);

            if (duplicates.contains(stickerNumber) || missing.contains(stickerNumber) || list.contains(stickerNumber))
            {
                continue;
            }

            list.add(stickerNumber);
        }
    }

    private void copyValidValues(ArrayList<Integer> source, ArrayList<Integer> target)
    {
        if (source == null)
        {
            return;
        }

        int i;
        for (i = 0; i < source.size(); i++)
        {
            Integer value = source.get(i);

            if (value == null)
            {
                continue;
            }

            if (value.intValue() < 1 || value.intValue() > 99)
            {
                continue;
            }

            if (!target.contains(value))
            {
                target.add(value);
            }
        }
    }

    private void removeOverlaps()
    {
        int i;
        for (i = missing.size() - 1; i >= 0; i--)
        {
            Integer value = missing.get(i);

            if (duplicates.contains(value))
            {
                missing.remove(i);
            }
        }
    }

    private int randomStickerCount()
    {
        return random.nextInt(20) + 1;
    }
}