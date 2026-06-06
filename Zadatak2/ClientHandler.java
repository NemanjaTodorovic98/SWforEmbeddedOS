import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable
{
    private Socket socket;
    private ServerState serverState;

    public ClientHandler(Socket socket, ServerState serverState)
    {
        this.socket = socket;
        this.serverState = serverState;
    }

    public void run()
    {
        String registeredUsername = null;

        try
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            writer.println("WELCOME");

            String line;
            while ((line = reader.readLine()) != null)
            {
                if (line.trim().length() == 0)
                {
                    writer.println("ERROR|Prazna poruka");
                    continue;
                }

                String[] parts = line.split("\\|", -1);
                String command = parts[0].trim();

                if (command.equalsIgnoreCase("REGISTER"))
                {
                    String result = handleRegister(parts);
                    writer.println(result);

                    if (result.startsWith("REGISTER_OK|"))
                    {
                        registeredUsername = extractUsernameFromOk(result);
                    }
                }
                else if (command.equalsIgnoreCase("UPDATE"))
                {
                    writer.println(handleUpdate(parts, registeredUsername));
                }
                else if (command.equalsIgnoreCase("QUIT"))
                {
                    writer.println("BYE");
                    break;
                }
                else
                {
                    writer.println("ERROR|Nepoznata komanda");
                }
            }
        }
        catch (IOException ex)
        {
        }
        finally
        {
            if (registeredUsername != null)
            {
                serverState.removeUser(registeredUsername);
            }

            try
            {
                socket.close();
            }
            catch (IOException ex)
            {
            }
        }
    }

    private String handleRegister(String[] parts)
    {
        if (parts.length != 4)
        {
            return "ERROR|Format REGISTER|username|dups|missing";
        }

        String username = parts[1].trim();
        ArrayList<Integer> duplicates = parseList(parts[2]);
        ArrayList<Integer> missing = parseList(parts[3]);

        if (duplicates == null || missing == null)
        {
            return "ERROR|Neispravan format liste";
        }

        if (hasOverlap(duplicates, missing))
        {
            return "ERROR|Ista slicica ne moze biti i duplikat i missing";
        }

        boolean registered = serverState.registerUser(username, duplicates, missing);

        if (!registered)
        {
            return "REGISTER_FAIL|Korisnicko ime je zauzeto ili neispravno";
        }

        return "REGISTER_OK|" + username + "|ACTIVE_USERS=" + serverState.getActiveUserCount();
    }

    private String handleUpdate(String[] parts, String registeredUsername)
    {
        if (parts.length != 3)
        {
            return "ERROR|Format UPDATE|dups|missing";
        }

        if (registeredUsername == null)
        {
            return "ERROR|Prvo uradi REGISTER";
        }

        ArrayList<Integer> duplicates = parseList(parts[1]);
        ArrayList<Integer> missing = parseList(parts[2]);

        if (duplicates == null || missing == null)
        {
            return "ERROR|Neispravan format liste";
        }

        if (hasOverlap(duplicates, missing))
        {
            return "ERROR|Ista slicica ne moze biti i duplikat i missing";
        }

        boolean updated = serverState.updateUserLists(registeredUsername, duplicates, missing);

        if (updated)
        {
            return "UPDATE_OK";
        }

        return "UPDATE_FAIL|Korisnik nije registrovan";
    }

    private String extractUsernameFromOk(String registerOkMessage)
    {
        String[] parts = registerOkMessage.split("\\|", -1);

        if (parts.length < 2)
        {
            return null;
        }

        return parts[1];
    }

    private ArrayList<Integer> parseList(String csv)
    {
        ArrayList<Integer> values = new ArrayList<Integer>();

        String trimmed = csv.trim();
        if (trimmed.length() == 0)
        {
            return values;
        }

        String[] tokens = trimmed.split(",");

        int i;
        for (i = 0; i < tokens.length; i++)
        {
            String token = tokens[i].trim();

            if (token.length() == 0)
            {
                return null;
            }

            int number;
            try
            {
                number = Integer.parseInt(token);
            }
            catch (NumberFormatException ex)
            {
                return null;
            }

            if (number < 1 || number > 99)
            {
                return null;
            }

            Integer value = Integer.valueOf(number);
            if (!values.contains(value))
            {
                values.add(value);
            }
        }

        return values;
    }

    private boolean hasOverlap(ArrayList<Integer> first, ArrayList<Integer> second)
    {
        int i;
        for (i = 0; i < first.size(); i++)
        {
            if (second.contains(first.get(i)))
            {
                return true;
            }
        }

        return false;
    }
}
