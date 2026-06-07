import java.io.*;
import java.net.*;

public class ClientHandlerTest
{
    public static void main(String[] args) throws Exception
    {
        ServerState serverState = new ServerState();
        ServerSocket serverSocket = new ServerSocket(0);
        int port = serverSocket.getLocalPort();

        Thread serverThread = new Thread(new Runnable()
        {
            public void run()
            {
                try
                {
                    Socket accepted = serverSocket.accept();
                    ClientHandler handler = new ClientHandler(accepted, serverState);
                    handler.run();
                    serverSocket.close();
                }
                catch (Exception ex)
                {
                }
            }
        });

        serverThread.start();

        Socket client = new Socket("localhost", port);
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        PrintWriter out = new PrintWriter(client.getOutputStream(), true);

        String welcome = in.readLine();

        out.println("REGISTER|user1|3,10|20,30");
        String registerResp = in.readLine();

        out.println("UPDATE|7,8|60");
        String updateResp = in.readLine();

        out.println("QUIT");
        String quitResp = in.readLine();

        client.close();
        serverThread.join();

        System.out.println("welcome: " + welcome);
        System.out.println("register: " + registerResp);
        System.out.println("update: " + updateResp);
        System.out.println("quit: " + quitResp);
        System.out.println("active users after quit: " + serverState.getActiveUserCount());
    }
}
