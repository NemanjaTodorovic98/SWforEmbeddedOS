import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GameServerState
{
    private final Map<String, GameClientHandler> onlineUsers;
    private final Set<String> availableUsers;
    private final Map<String, GameSession> sessionsByUser;
    private final Map<String, String> pendingInviteByTarget;

    public GameServerState()
    {
        this.onlineUsers = new HashMap<String, GameClientHandler>();
        this.availableUsers = new HashSet<String>();
        this.sessionsByUser = new HashMap<String, GameSession>();
        this.pendingInviteByTarget = new HashMap<String, String>();
    }

    public synchronized String register(String username, GameClientHandler handler)
    {
        if (username == null || username.trim().length() == 0)
        {
            return "Neispravno korisnicko ime";
        }

        if (onlineUsers.containsKey(username))
        {
            return "Korisnicko ime je zauzeto";
        }

        onlineUsers.put(username, handler);
        availableUsers.add(username);
        broadcastAvailableUsers();
        return null;
    }

    public synchronized String listFor(String username)
    {
        ArrayList<String> users = new ArrayList<String>();

        for (String user : availableUsers)
        {
            if (!user.equals(username))
            {
                users.add(user);
            }
        }

        Collections.sort(users);
        if (users.size() == 0)
        {
            return "USERS|";
        }

        return "USERS|" + join(users);
    }

    public synchronized String invite(String fromUser, String toUser)
    {
        if (!isRegistered(fromUser))
        {
            return "Niste registrovani";
        }

        if (toUser == null || toUser.trim().length() == 0 || fromUser.equals(toUser))
        {
            return "Neispravan protivnik";
        }

        if (!onlineUsers.containsKey(toUser))
        {
            return "Korisnik nije online";
        }

        if (!availableUsers.contains(fromUser) || !availableUsers.contains(toUser))
        {
            return "Igrac nije dostupan";
        }

        if (pendingInviteByTarget.containsKey(toUser))
        {
            return "Korisnik vec ima poziv na cekanju";
        }

        pendingInviteByTarget.put(toUser, fromUser);
        GameClientHandler target = onlineUsers.get(toUser);
        if (target != null)
        {
            target.send("INVITE_INCOMING|" + fromUser);
        }

        return null;
    }

    public synchronized String inviteResponse(String responder, String fromUser, boolean accepted)
    {
        String pendingFrom = pendingInviteByTarget.get(responder);

        if (pendingFrom == null || !pendingFrom.equals(fromUser))
        {
            return "Poziv nije pronadjen";
        }

        pendingInviteByTarget.remove(responder);

        GameClientHandler fromHandler = onlineUsers.get(fromUser);
        GameClientHandler responderHandler = onlineUsers.get(responder);

        if (fromHandler == null || responderHandler == null)
        {
            return "Jedan od igraca nije online";
        }

        if (!accepted)
        {
            fromHandler.send("INVITE_RESULT|REJECTED|" + responder);
            responderHandler.send("INVITE_RESULT|REJECTED|" + fromUser);
            return null;
        }

        if (!availableUsers.contains(fromUser) || !availableUsers.contains(responder))
        {
            return "Igrac nije dostupan";
        }

        GameSession session = new GameSession(fromUser, responder);
        sessionsByUser.put(fromUser, session);
        sessionsByUser.put(responder, session);

        availableUsers.remove(fromUser);
        availableUsers.remove(responder);

        fromHandler.send("INVITE_RESULT|ACCEPTED|" + responder);
        responderHandler.send("INVITE_RESULT|ACCEPTED|" + fromUser);

        fromHandler.send("START_GAME|" + responder + "|YOUR_TURN");
        responderHandler.send("START_GAME|" + fromUser + "|WAIT");

        broadcastAvailableUsers();
        return null;
    }

    public synchronized String move(String username, int column)
    {
        GameSession session = sessionsByUser.get(username);
        if (session == null)
        {
            return "Niste u aktivnoj igri";
        }

        GameSession.MoveResult result = session.playMove(username, column);
        if (!result.isValid())
        {
            return result.getReason();
        }

        String other = session.getOpponent(username);
        GameClientHandler first = onlineUsers.get(username);
        GameClientHandler second = onlineUsers.get(other);

        String msg = "MOVE_OK|" + result.getColumn() + "|" + result.getRow() + "|next=" + (result.getNextPlayer() == null ? "NONE" : result.getNextPlayer());
        if (first != null)
        {
            first.send(msg);
        }
        if (second != null)
        {
            second.send(msg);
        }

        if (!result.isGameOver())
        {
            if (first != null)
            {
                first.send(username.equals(result.getNextPlayer()) ? "YOUR_TURN" : "WAIT_TURN");
            }
            if (second != null)
            {
                second.send(other.equals(result.getNextPlayer()) ? "YOUR_TURN" : "WAIT_TURN");
            }
            return null;
        }

        sendGameEnd(session);

        sessionsByUser.remove(session.getPlayerOne());
        sessionsByUser.remove(session.getPlayerTwo());
        availableUsers.add(session.getPlayerOne());
        availableUsers.add(session.getPlayerTwo());
        broadcastAvailableUsers();

        return null;
    }

    public synchronized void disconnect(String username)
    {
        if (username == null)
        {
            return;
        }

        onlineUsers.remove(username);
        availableUsers.remove(username);
        pendingInviteByTarget.remove(username);
        removeOutgoingInvites(username);

        GameSession session = sessionsByUser.remove(username);
        if (session != null)
        {
            String other = session.getOpponent(username);
            sessionsByUser.remove(other);

            GameClientHandler otherHandler = onlineUsers.get(other);
            if (otherHandler != null)
            {
                availableUsers.add(other);
                otherHandler.send("BACK_TO_LOBBY");
            }
        }

        broadcastAvailableUsers();
    }

    private void sendGameEnd(GameSession session)
    {
        String p1 = session.getPlayerOne();
        String p2 = session.getPlayerTwo();

        GameClientHandler h1 = onlineUsers.get(p1);
        GameClientHandler h2 = onlineUsers.get(p2);

        String winner = session.getWinner();

        if (winner == null)
        {
            if (h1 != null)
            {
                h1.send("GAME_END|DRAW");
            }
            if (h2 != null)
            {
                h2.send("GAME_END|DRAW");
            }
            return;
        }

        if (h1 != null)
        {
            h1.send(winner.equals(p1) ? "GAME_END|WIN" : "GAME_END|LOSE");
        }

        if (h2 != null)
        {
            h2.send(winner.equals(p2) ? "GAME_END|WIN" : "GAME_END|LOSE");
        }
    }

    private boolean isRegistered(String username)
    {
        return username != null && onlineUsers.containsKey(username);
    }

    private void removeOutgoingInvites(String fromUser)
    {
        ArrayList<String> toRemove = new ArrayList<String>();

        for (Map.Entry<String, String> entry : pendingInviteByTarget.entrySet())
        {
            if (fromUser.equals(entry.getValue()))
            {
                toRemove.add(entry.getKey());
            }
        }

        int i;
        for (i = 0; i < toRemove.size(); i++)
        {
            pendingInviteByTarget.remove(toRemove.get(i));
        }
    }

    private void broadcastAvailableUsers()
    {
        for (String user : availableUsers)
        {
            GameClientHandler handler = onlineUsers.get(user);
            if (handler != null)
            {
                handler.send(listFor(user));
            }
        }
    }

    private String join(ArrayList<String> list)
    {
        StringBuilder sb = new StringBuilder();

        int i;
        for (i = 0; i < list.size(); i++)
        {
            sb.append(list.get(i));

            if (i < list.size() - 1)
            {
                sb.append(",");
            }
        }

        return sb.toString();
    }
}
