import java.util.ArrayList;
import java.util.HashMap;

public class ServerState
{
    private HashMap<String, StickerUser> activeUsers;

    public ServerState()
    {
        this.activeUsers = new HashMap<String, StickerUser>();
    }

    public synchronized boolean registerUser(String username, ArrayList<Integer> duplicates, ArrayList<Integer> missing)
    {
        if (username == null)
        {
            return false;
        }

        String trimmed = username.trim();

        if (trimmed.length() == 0)
        {
            return false;
        }

        if (activeUsers.containsKey(trimmed))
        {
            return false;
        }

        StickerUser user = new StickerUser(trimmed, duplicates, missing);
        activeUsers.put(trimmed, user);
        return true;
    }

    public synchronized boolean updateUserLists(String username, ArrayList<Integer> duplicates, ArrayList<Integer> missing)
    {
        StickerUser user = activeUsers.get(username);

        if (user == null)
        {
            return false;
        }

        user.getDuplicates().clear();
        user.getMissing().clear();

        int i;
        Integer value;

        
        for (i = 0; i < duplicates.size(); i++)
        {
            value = duplicates.get(i);
            user.addDuplicate(value.intValue());
        }

        for (i = 0; i < missing.size(); i++)
        {
            value = missing.get(i);
            user.addMissing(value.intValue());
        }

        return true;
    }

    public synchronized void removeUser(String username)
    {
        if (username == null)
        {
            return;
        }

        activeUsers.remove(username);
    }

    public synchronized int getActiveUserCount()
    {
        return activeUsers.size();
    }

    public synchronized StickerUser getUser(String username)
    {
        return activeUsers.get(username);
    }

    public synchronized ArrayList<StickerUser> getOtherUsersSnapshot(String username)
    {
        ArrayList<StickerUser> users = new ArrayList<StickerUser>();

        for (StickerUser user : activeUsers.values())
        {
            if (username != null && username.equals(user.getUsername()))
            {
                continue;
            }

            users.add(new StickerUser(user.getUsername(), user.getDuplicates(), user.getMissing()));
        }

        return users;
    }
}
