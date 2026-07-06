import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class GameServer
{
    public static final int PORT = 6060;

    public static void main(String[] args)
    {
        GameServerState state = new GameServerState();

        try
        {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Game server started on port " + PORT);

            while (true)
            {
                Socket socket = serverSocket.accept();
                GameClientHandler handler = new GameClientHandler(socket, state);
                Thread thread = new Thread(handler);
                thread.start();
            }
        }
        catch (IOException ex)
        {
            System.out.println("Server error: " + ex.getMessage());
        }
    }
}
