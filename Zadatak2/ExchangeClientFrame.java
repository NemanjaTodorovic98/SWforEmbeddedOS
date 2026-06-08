import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

public class ExchangeClientFrame extends JFrame
{
    private JTextField usernameField;
    private JTextArea logArea;
    private JTextArea exchangesArea;
    private JButton registerButton;
    private JButton updateButton;
    private JButton findExchangesButton;
    private JCheckBox[] duplicateChecks;
    private JCheckBox[] missingChecks;

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

        updateButton = new JButton("Update");
        updateButton.setEnabled(false);
        top.add(updateButton);

        findExchangesButton = new JButton("Nadji razmene");
        findExchangesButton.setEnabled(false);
        top.add(findExchangesButton);

        add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(1, 3));
        duplicateChecks = new JCheckBox[99];
        missingChecks = new JCheckBox[99];

        JPanel dupGrid = new JPanel(new GridLayout(0, 3));
        JPanel missingGrid = new JPanel(new GridLayout(0, 3));

        int i;
        for (i = 1; i <= 99; i++)
        {
            duplicateChecks[i - 1] = new JCheckBox(String.valueOf(i));
            missingChecks[i - 1] = new JCheckBox(String.valueOf(i));
            dupGrid.add(duplicateChecks[i - 1]);
            missingGrid.add(missingChecks[i - 1]);
        }

        JPanel left = new JPanel(new BorderLayout());
        left.add(new JLabel("Duplikati"), BorderLayout.NORTH);
        left.add(new JScrollPane(dupGrid), BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout());
        right.add(new JLabel("Nedostaju"), BorderLayout.NORTH);
        right.add(new JScrollPane(missingGrid), BorderLayout.CENTER);

        center.add(left);
        center.add(right);

        exchangesArea = new JTextArea();
        exchangesArea.setEditable(false);
        JPanel exchangePanel = new JPanel(new BorderLayout());
        exchangePanel.add(new JLabel("Moguce razmene"), BorderLayout.NORTH);
        exchangePanel.add(new JScrollPane(exchangesArea), BorderLayout.CENTER);
        center.add(exchangePanel);

        add(center, BorderLayout.CENTER);

        logArea = new JTextArea();
        logArea.setEditable(false);
        add(new JScrollPane(logArea), BorderLayout.SOUTH);

        registerButton.addActionListener(e -> register());
        updateButton.addActionListener(e -> updateLists());
    findExchangesButton.addActionListener(e -> loadPossibleExchanges());
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
        updateChecksFromLocalUser();

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

                if (resp.startsWith("REGISTER_OK"))
                {
                    updateButton.setEnabled(true);
                    findExchangesButton.setEnabled(true);
                }
            }
        }
        catch (Exception ex)
        {
            logArea.append("Problem pri povezivanju na server\n");
        }
    }

    private void updateLists()
    {
        if (localUser == null)
        {
            logArea.append("Prvo se registrujte.\n");
            return;
        }

        localUser.getDuplicates().clear();
        localUser.getMissing().clear();

        int i;
        for (i = 1; i <= 99; i++)
        {
            boolean dupSelected = duplicateChecks[i - 1].isSelected();
            boolean missSelected = missingChecks[i - 1].isSelected();

            if (dupSelected && missSelected)
            {
                logArea.append("Slicica " + i + " ne moze biti i duplikat i nedostajuca.\n");
                return;
            }

            if (dupSelected)
            {
                localUser.addDuplicate(i);
            }

            if (missSelected)
            {
                localUser.addMissing(i);
            }
        }

        try
        {
            if (writer == null)
            {
                logArea.append("Niste povezani na server.\n");
                return;
            }

            String msg = "UPDATE|" + localUser.getUsername() + "|" + toCsv(localUser.getDuplicates()) + "|" + toCsv(localUser.getMissing());
            writer.println(msg);
            String resp = reader.readLine();

            if (resp != null)
            {
                logArea.append(resp + "\n");
            }
        }
        catch (Exception ex)
        {
            logArea.append("Problem pri slanju update poruke.\n");
        }
    }

    private void loadPossibleExchanges()
    {
        if (localUser == null)
        {
            logArea.append("Prvo se registrujte.\n");
            return;
        }

        try
        {
            if (writer == null)
            {
                logArea.append("Niste povezani na server.\n");
                return;
            }

            writer.println("GET_POSSIBLE_EXCHANGES");
            String resp = reader.readLine();

            if (resp == null)
            {
                logArea.append("Nema odgovora servera.\n");
                return;
            }

            if (resp.startsWith("POSSIBLE_EXCHANGES|"))
            {
                fillExchangesArea(resp);
            }
            else
            {
                logArea.append(resp + "\n");
            }
        }
        catch (Exception ex)
        {
            logArea.append("Problem pri trazenju razmena.\n");
        }
    }

    private void fillExchangesArea(String response)
    {
        exchangesArea.setText("");

        String payload = response.substring("POSSIBLE_EXCHANGES|".length());

        if (payload.equals("NONE"))
        {
            exchangesArea.setText("Trenutno nema mogucih razmena.");
            return;
        }

        String[] items = payload.split(";");

        int i;
        for (i = 0; i < items.length; i++)
        {
            String[] parts = items[i].split("#");

            if (parts.length == 3)
            {
                exchangesArea.append("Korisnik: " + parts[0] + "\n");
                exchangesArea.append(parts[1].replace("YOU_GIVE=", "Ti dajes: ") + "\n");
                exchangesArea.append(parts[2].replace("YOU_GET=", "Ti dobijas: ") + "\n\n");
            }
        }
    }

    private void updateChecksFromLocalUser()
    {
        int i;
        for (i = 0; i < 99; i++)
        {
            duplicateChecks[i].setSelected(false);
            missingChecks[i].setSelected(false);
        }

        ArrayList<Integer> duplicates = localUser.getDuplicates();
        ArrayList<Integer> missing = localUser.getMissing();

        for (i = 0; i < duplicates.size(); i++)
        {
            int value = duplicates.get(i);
            if (value >= 1 && value <= 99)
            {
                duplicateChecks[value - 1].setSelected(true);
            }
        }

        for (i = 0; i < missing.size(); i++)
        {
            int value = missing.get(i);
            if (value >= 1 && value <= 99)
            {
                missingChecks[value - 1].setSelected(true);
            }
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
