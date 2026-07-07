import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class GameClientHandler implements Runnable
{
    private final Socket socket;
    private final GameServerState serverState;

    private BufferedReader reader;
    private PrintWriter writer;
    private String username;

    public GameClientHandler(Socket socket, GameServerState serverState)
    {
        this.socket = socket;
        this.serverState = serverState;
    }

    @Override
    public void run()
    {
        try
        {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            send("WELCOME");

            boolean running = true;
            while (running)
            {
                String line = reader.readLine();
                if (line == null)
                {
                    break;
                }

                running = handleCommand(line);
            }
        }
        catch (IOException ex)
        {
        }
        finally
        {
            serverState.disconnect(username);
            closeQuietly();
        }
    }

    public synchronized void send(String message)
    {
        if (writer != null)
        {
            writer.println(message);
        }
    }

    private boolean handleCommand(String line)
    {
        if (line.trim().length() == 0)
        {
            send("ERROR|Prazna poruka");
            return true;
        }

        String[] parts = line.split("\\|", -1);
        String command = parts[0].trim();

        if (GameProtocol.REGISTER.equalsIgnoreCase(command))
        {
            return handleRegister(parts);
        }

        if (GameProtocol.LIST.equalsIgnoreCase(command))
        {
            if (!requireRegister())
            {
                return true;
            }

            send(serverState.listFor(username));
            return true;
        }

        if (GameProtocol.INVITE.equalsIgnoreCase(command))
        {
            if (!requireRegister())
            {
                return true;
            }

            if (parts.length != 2)
            {
                send("ERROR|Format INVITE|toUser");
                return true;
            }

            String error = serverState.invite(username, parts[1].trim());
            if (error != null)
            {
                send("ERROR|" + error);
            }
            else
            {
                send("INVITE_RESULT|SENT|" + parts[1].trim());
            }
            return true;
        }

        if (GameProtocol.INVITE_RESPONSE.equalsIgnoreCase(command))
        {
            if (!requireRegister())
            {
                return true;
            }

            if (parts.length != 3)
            {
                send("ERROR|Format INVITE_RESPONSE|fromUser|ACCEPT/REJECT");
                return true;
            }

            boolean accepted;
            if ("ACCEPT".equalsIgnoreCase(parts[2].trim()))
            {
                accepted = true;
            }
            else if ("REJECT".equalsIgnoreCase(parts[2].trim()))
            {
                accepted = false;
            }
            else
            {
                send("ERROR|Odluka mora biti ACCEPT ili REJECT");
                return true;
            }

            String error = serverState.inviteResponse(username, parts[1].trim(), accepted);
            if (error != null)
            {
                send("ERROR|" + error);
            }
            return true;
        }

        if (GameProtocol.MOVE.equalsIgnoreCase(command))
        {
            if (!requireRegister())
            {
                return true;
            }

            if (parts.length != 2)
            {
                send("ERROR|Format MOVE|column");
                return true;
            }

            int column;
            try
            {
                column = Integer.parseInt(parts[1].trim());
            }
            catch (NumberFormatException ex)
            {
                send("MOVE_INVALID|Neispravna kolona");
                return true;
            }

            String error = serverState.move(username, column);
            if (error != null)
            {
                send("MOVE_INVALID|" + error);
            }
            return true;
        }

        if ("REMATCH".equalsIgnoreCase(command))
        {
            if (!requireRegister())
            {
                return true;
            }

            if (parts.length != 2)
            {
                send("ERROR|Format REMATCH|YES/NO");
                return true;
            }

            boolean accepted = "YES".equalsIgnoreCase(parts[1].trim());
            String error = serverState.rematch(username, accepted);
            if (error != null)
            {
                send("ERROR|" + error);
            }
            return true;
        }

        if (GameProtocol.QUIT.equalsIgnoreCase(command))
        {
            send("BYE");
            return false;
        }

        send("ERROR|Nepoznata komanda");
        return true;
    }

    private boolean handleRegister(String[] parts)
    {
        if (parts.length != 2)
        {
            send("ERROR|Format REGISTER|username");
            return true;
        }

        if (username != null)
        {
            send("ERROR|Vec ste registrovani");
            return true;
        }

        String requested = parts[1].trim();
        String error = serverState.register(requested, this);

        if (error != null)
        {
            send("REGISTER_FAIL|" + error);
            return true;
        }

        username = requested;
        send("REGISTER_OK");
        send(serverState.listFor(username));
        return true;
    }

    private boolean requireRegister()
    {
        if (username == null)
        {
            send("ERROR|Prvo uradite REGISTER");
            return false;
        }

        return true;
    }

    private void closeQuietly()
    {
        try
        {
            socket.close();
        }
        catch (IOException ex)
        {
        }
    }
}
