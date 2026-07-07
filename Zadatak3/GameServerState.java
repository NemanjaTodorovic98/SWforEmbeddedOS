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
    private final Map<String, String> rematchPendingByUser;
    private final Map<String, Boolean> rematchDecisions;
    private final Map<String, String> rematchOriginalFirst;

    public GameServerState()
    {
        this.onlineUsers = new HashMap<String, GameClientHandler>();
        this.availableUsers = new HashSet<String>();
        this.sessionsByUser = new HashMap<String, GameSession>();
        this.pendingInviteByTarget = new HashMap<String, String>();
        this.rematchPendingByUser = new HashMap<String, String>();
        this.rematchDecisions = new HashMap<String, Boolean>();
        this.rematchOriginalFirst = new HashMap<String, String>();
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

        fromHandler.send("START_GAME|" + responder + "|1");
        responderHandler.send("START_GAME|" + fromUser + "|2");

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

        int playerValue = session.getPlayerOne().equals(username) ? 1 : 2;
        String msg = "MOVE_OK|" + result.getRow() + "|" + result.getColumn() + "|" + playerValue + "|" + (result.getNextPlayer() == null ? "NONE" : result.getNextPlayer());
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

        String p1 = session.getPlayerOne();
        String p2 = session.getPlayerTwo();

        sendGameEnd(session);

        sessionsByUser.remove(p1);
        sessionsByUser.remove(p2);

        rematchPendingByUser.put(p1, p2);
        rematchPendingByUser.put(p2, p1);
        rematchOriginalFirst.put(p1, p1);
        rematchOriginalFirst.put(p2, p1);

        return null;
    }

    public synchronized String rematch(String username, boolean accepted)
    {
        String opponent = rematchPendingByUser.get(username);
        if (opponent == null)
        {
            return "Nema aktivnog rematch zahteva";
        }

        rematchDecisions.put(username, accepted);

        if (!rematchDecisions.containsKey(opponent))
        {
            return null;
        }

        boolean opponentAccepted = rematchDecisions.get(opponent);
        String originalFirst = rematchOriginalFirst.get(username);

        rematchPendingByUser.remove(username);
        rematchPendingByUser.remove(opponent);
        rematchDecisions.remove(username);
        rematchDecisions.remove(opponent);
        rematchOriginalFirst.remove(username);
        rematchOriginalFirst.remove(opponent);

        GameClientHandler myHandler = onlineUsers.get(username);
        GameClientHandler opponentHandler = onlineUsers.get(opponent);

        if (!accepted || !opponentAccepted)
        {
            availableUsers.add(username);
            availableUsers.add(opponent);
            if (myHandler != null) myHandler.send("BACK_TO_LOBBY");
            if (opponentHandler != null) opponentHandler.send("BACK_TO_LOBBY");
            broadcastAvailableUsers();
            return null;
        }

        String newFirst = originalFirst.equals(username) ? opponent : username;
        String newSecond = newFirst.equals(username) ? opponent : username;

        GameSession newSession = new GameSession(newFirst, newSecond);
        sessionsByUser.put(newFirst, newSession);
        sessionsByUser.put(newSecond, newSession);

        GameClientHandler firstHandler = onlineUsers.get(newFirst);
        GameClientHandler secondHandler = onlineUsers.get(newSecond);

        if (firstHandler != null) firstHandler.send("REMATCH_START|" + newSecond + "|1");
        if (secondHandler != null) secondHandler.send("REMATCH_START|" + newFirst + "|2");

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

        String rematchOpponent = rematchPendingByUser.get(username);
        if (rematchOpponent != null)
        {
            rematchPendingByUser.remove(username);
            rematchPendingByUser.remove(rematchOpponent);
            rematchDecisions.remove(username);
            rematchDecisions.remove(rematchOpponent);
            rematchOriginalFirst.remove(username);
            rematchOriginalFirst.remove(rematchOpponent);

            availableUsers.add(rematchOpponent);
            GameClientHandler otherHandler = onlineUsers.get(rematchOpponent);
            if (otherHandler != null) otherHandler.send("BACK_TO_LOBBY");
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
            if (h1 != null) h1.send("GAME_END|DRAW");
            if (h2 != null) h2.send("GAME_END|DRAW");
            return;
        }

        if (h1 != null)
        {
            h1.send("GAME_END|" + winner);
        }

        if (h2 != null)
        {
            h2.send("GAME_END|" + winner);
        }
    }

    private void broadcastAvailableUsers()
    {
        String msg = listFor("");
        for (GameClientHandler handler : onlineUsers.values())
        {
            handler.send(msg);
        }
    }

    private void removeOutgoingInvites(String username)
    {
        ArrayList<String> toRemove = new ArrayList<String>();
        for (Map.Entry<String, String> entry : pendingInviteByTarget.entrySet())
        {
            if (entry.getValue().equals(username))
            {
                toRemove.add(entry.getKey());
            }
        }
        for (String key : toRemove)
        {
            pendingInviteByTarget.remove(key);
        }
    }

    private boolean isRegistered(String username)
    {
        return onlineUsers.containsKey(username);
    }

    private String join(ArrayList<String> list)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++)
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