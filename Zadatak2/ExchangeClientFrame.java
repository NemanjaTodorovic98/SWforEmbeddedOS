import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

public class ExchangeClientFrame extends JFrame
{
    private JTextField usernameField;
    private JTextArea dupArea;
    private JTextArea missingArea;
    private JTextArea logArea;
    private JButton registerButton;

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private StickerUser localUser;

    public ExchangeClientFrame()
    {
        setTitle("Menjaza - Klijent");
        setSize(820, 560);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel top = new JPanel();
        top.add(new JLabel("Username:"));

        usernameField = new JTextField(16);
        top.add(usernameField);

        registerButton = new JButton("Register");
        top.add(registerButton);

        add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(1, 2));

        dupArea = new JTextArea();
        dupArea.setEditable(false);
        missingArea = new JTextArea();
        missingArea.setEditable(false);

        JPanel left = new JPanel(new BorderLayout());
        left.add(new JLabel("Duplikati"), BorderLayout.NORTH);
        left.add(new JScrollPane(dupArea), BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout());
        right.add(new JLabel("Nedostaju"), BorderLayout.NORTH);
        right.add(new JScrollPane(missingArea), BorderLayout.CENTER);

        center.add(left);
        center.add(right);

        add(center, BorderLayout.CENTER);

        logArea = new JTextArea();
        logArea.setEditable(false);
        add(new JScrollPane(logArea), BorderLayout.SOUTH);

        registerButton.addActionListener(e -> register());
    }

    private void register()
    {
        String username = usernameField.getText().trim();

        if (username.length() == 0)
        {
            logArea.append("Unesite username.\n");
            return;
        }

        localUser = new StickerUser(username);
        dupArea.setText(toCsv(localUser.getDuplicates()));
        missingArea.setText(toCsv(localUser.getMissing()));

        try
        {
            if (socket == null || socket.isClosed())
            {
                socket = new Socket("localhost", ExchangeServer.PORT);
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream(), true);
                String welcome = reader.readLine();
                if (welcome != null)
                {
                    logArea.append(welcome + "\n");
                }
            }

            String msg = "REGISTER|" + username + "|" + toCsv(localUser.getDuplicates()) + "|" + toCsv(localUser.getMissing());
            writer.println(msg);
            String resp = reader.readLine();
            if (resp != null)
            {
                logArea.append(resp + "\n");
            }
        }
        catch (Exception ex)
        {
            logArea.append("Problem pri povezivanju na server\n");
        }
    }

    private String toCsv(ArrayList<Integer> list)
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
