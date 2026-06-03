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
        this.duplicates = duplicates;
        this.missing = missing;
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
        Integer value = Integer.valueOf(stickerNumber);

        if (!duplicates.contains(value) && !missing.contains(value))
        {
            duplicates.add(value);
        }
    }

    public void addMissing(int stickerNumber)
    {
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

        generateRnadomList(duplicates, duplicateCount);
        generateRnadomList(missing, missingCount);
    }

    private void generateRnadomList(ArrayList<Integer> list, int count)
    {
        while (list.size() < count)
        {
            Integer stickerNumber = Integer.valueOf(random.nextInt(99) + 1);

            if (duplicates.contains(stickerNumber) || missing.contains(stickerNumber) || targetList.contains(stickerNumber))
            {
                continue;
            }

            targetList.add(stickerNumber);
        }
    }

    private int randomStickerCount()
    {
        return random.nextInt(20) + 1;
    }
}