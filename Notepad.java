import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;

public class Notepad extends JFrame implements ActionListener, WindowListener {

    JTabbedPane tabbedPane = new JTabbedPane();
    File[] files = new File[10]; // Limit for simplicity; can be dynamic

    public Notepad() {
        Font fnt = new Font("Arial", Font.PLAIN, 15);
        Container con = getContentPane();

        JMenuBar jmb = new JMenuBar();
        JMenu jmfile = new JMenu("File");
        JMenu jmedit = new JMenu("Edit");
        JMenu jmhelp = new JMenu("Help");

        con.setLayout(new BorderLayout());
        con.add(tabbedPane, BorderLayout.CENTER);

        createMenuItem(jmfile, "New Tab");
        createMenuItem(jmfile, "Open");
        createMenuItem(jmfile, "Save");
        createMenuItem(jmfile, "Save As");
        jmfile.addSeparator();
        createMenuItem(jmfile, "Close Tab");
        createMenuItem(jmfile, "Exit");

        createMenuItem(jmedit, "Cut");
        createMenuItem(jmedit, "Copy");
        createMenuItem(jmedit, "Paste");

        createMenuItem(jmhelp, "About Notepad");

        jmb.add(jmfile);
        jmb.add(jmedit);
        jmb.add(jmhelp);

        setJMenuBar(jmb);

        addWindowListener(this);
        setSize(800, 600);
        setTitle("Notepad");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Add the "+" button tab
        addPlusTab();

        // Create the first tab on startup
        createNewTab();

        // Add mouse listener to allow tab renaming on double-click
        tabbedPane.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.getClickCount() == 2) {  // Check for double-click
                    int index = tabbedPane.indexAtLocation(e.getX(), e.getY());
                    if (index != -1 && index < tabbedPane.getTabCount() - 1) {  // Ensure it's not the "+" tab
                        renameTab(index);
                    }
                }
            }
        });

        setVisible(true);
    }

    public void createMenuItem(JMenu jm, String txt) {
        JMenuItem jmi = new JMenuItem(txt);
        jmi.addActionListener(this);
        jm.add(jmi);
    }

    public void addPlusTab() {
        // Create a dummy panel for the "+" tab
        JButton plusButton = new JButton("+");
        plusButton.setFont(new Font("Arial", Font.BOLD, 20));
        plusButton.setFocusable(false);

        // Create a tab for the "+" button
        tabbedPane.addTab("", null); // Add an empty tab
        tabbedPane.setTabComponentAt(tabbedPane.getTabCount() - 1, plusButton);

        // Add an ActionListener to handle the button click
        plusButton.addActionListener(e -> createNewTab());
    }

    public void createNewTab() {
        JTextArea textArea = new JTextArea();
        textArea.setFont(new Font("Arial", Font.PLAIN, 15));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        // Add shortcut Ctrl + S for save
        InputMap inputMap = textArea.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actionMap = textArea.getActionMap();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), "save");
        actionMap.put("save", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveCurrentTab(new JFileChooser(), false);
            }
        });

        // Shortcut Ctrl + O for open
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK), "open");
        actionMap.put("open", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openFile();  // Call the openFile method
            }
        });

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        String title = "Untitled   ";
        int index = tabbedPane.getTabCount() - 1;
        tabbedPane.insertTab(title, null, scrollPane, null, index);
        files[index] = null; // No file initially

        // Set header with close button
        setTabHeader(index);

        // Set focus to the newly created tab
        tabbedPane.setSelectedIndex(index);
    }

    public void setTabHeader(int index) {
        JPanel tabPanel = new JPanel(new BorderLayout());
        JLabel tabTitle = new JLabel(tabbedPane.getTitleAt(index));
        JButton closeButton = new JButton("X");
        closeButton.setFocusable(false);
        closeButton.setMargin(new Insets(0, 5, 0, 5));
        closeButton.setFont(new Font("Arial", Font.BOLD, 12));

        // Close tab action
        closeButton.addActionListener(e -> {
            int selectedIndex = tabbedPane.indexOfTabComponent(tabPanel);
            if (selectedIndex != -1 && selectedIndex < tabbedPane.getTabCount() - 1) {
                tabbedPane.removeTabAt(selectedIndex);
            }
        });

        tabPanel.add(tabTitle, BorderLayout.WEST);
        tabPanel.add(closeButton, BorderLayout.EAST);
        tabPanel.setOpaque(false);

        tabbedPane.setTabComponentAt(index, tabPanel);
    }


    public JTextArea getCurrentTextArea() {
        JScrollPane scrollPane = (JScrollPane) tabbedPane.getSelectedComponent();
        return (JTextArea) scrollPane.getViewport().getView();
    }

    public void actionPerformed(ActionEvent e) {
        JFileChooser jfc = new JFileChooser();

        switch (e.getActionCommand()) {
            case "New Tab":
                createNewTab();
                break;

            case "Open":
                openFile();  // Refactored to a separate method
                break;

            case "Save":
                saveCurrentTab(jfc, false);
                break;

            case "Save As":
                saveCurrentTab(jfc, true);
                break;

            case "Close Tab":
                int selectedIndex = tabbedPane.getSelectedIndex();
                if (selectedIndex != -1 && selectedIndex < tabbedPane.getTabCount() - 1) {
                    tabbedPane.removeTabAt(selectedIndex);
                }
                break;

            case "Exit":
                Exiting();
                break;

            case "Cut":
                getCurrentTextArea().cut();
                break;

            case "Copy":
                getCurrentTextArea().copy();
                break;

            case "Paste":
                getCurrentTextArea().paste();
                break;

            case "About Notepad":
                JOptionPane.showMessageDialog(this, "Multi-Tab Notepad Application", "About Notepad",
                        JOptionPane.INFORMATION_MESSAGE);
                break;
        }
    }


    // Refactored openFile method
    private void openFile() {
        JFileChooser jfc = new JFileChooser();
        if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File file = jfc.getSelectedFile();
                JTextArea textArea = new JTextArea();
                textArea.setFont(new Font("Arial", Font.PLAIN, 15));
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);

                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                textArea.setText("");
                while ((line = reader.readLine()) != null) {
                    textArea.append(line + "\n");
                }
                reader.close();

                int index = tabbedPane.getTabCount() - 1;
                tabbedPane.insertTab(file.getName() + "   ", null, scrollPane, null, index);
                files[index] = file;
                setTabHeader(index);

                tabbedPane.setSelectedIndex(index);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error opening file: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveCurrentTab(JFileChooser jfc, boolean saveAs) {
        int selectedIndex = tabbedPane.getSelectedIndex();
        if (selectedIndex == -1 || selectedIndex == tabbedPane.getTabCount() - 1) {
            return; // No tab selected or "+" tab
        }

        File currentFile = files[selectedIndex];

        if (saveAs || currentFile == null) {
            if (jfc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                currentFile = jfc.getSelectedFile();
                files[selectedIndex] = currentFile;
                tabbedPane.setTitleAt(selectedIndex, currentFile.getName());
            } else {
                return;
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile))) {
            writer.write(getCurrentTextArea().getText());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Renaming a tab on double-click
    private void renameTab(int index) {
        // Prompt user for a new name
        String newName = JOptionPane.showInputDialog(this, "Enter new tab name:", tabbedPane.getTitleAt(index));
        if (newName != null && !newName.trim().isEmpty()) {
            // Update the tab's title in the custom header
            Component tabComponent = tabbedPane.getTabComponentAt(index);
            if (tabComponent instanceof JPanel) {
                JPanel tabPanel = (JPanel) tabComponent;
                for (Component comp : tabPanel.getComponents()) {
                    if (comp instanceof JLabel) {
                        ((JLabel) comp).setText(newName + "   "); // Update the JLabel text
                        break;
                    }
                }
            }
            tabbedPane.setTitleAt(index, newName); // Update internal title for the tab
        }
    }


    public void windowDeactivated(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowClosed(WindowEvent e) {}

    public void windowClosing(WindowEvent e) {
        Exiting();
    }

    public void windowOpened(WindowEvent e) {}

    public void Exiting() {
        System.exit(0);
    }

    public static void main(String[] args) {
        new Notepad();
    }
}
