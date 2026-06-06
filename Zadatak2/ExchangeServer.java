import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ExchangeServer
{
    public static final int PORT = 5050;

    public static void main(String[] args)
    {
        ServerState serverState = new ServerState();

        try
        {
            ServerSocket serverSocket = new ServerSocket(PORT);

            while (true)
            {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket, serverState);
                Thread thread = new Thread(handler);
                thread.start();
            }
        }
        catch (IOException ex)
        {
            System.out.println("error: " + ex.getMessage());
        }
    }
}
