package de.poweruser.powerserver.main.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import de.poweruser.powerserver.logger.Logger;
import de.poweruser.powerserver.main.PowerServer;

public class MainWindow extends JFrame implements Observer {

    private static final long serialVersionUID = 2846198182943968671L;

    private PowerServer server;
    private boolean alreadyShuttingDown;
    private ArrayList<String> commandHistory;
    private CommandHistoryPos selectedCommandIndex;
    private final int maxHistorySize;

    public MainWindow() {
        this.server = null;
        this.alreadyShuttingDown = false;
        this.commandHistory = new ArrayList<String>();
        this.selectedCommandIndex = CommandHistoryPos.POSITION;
        this.maxHistorySize = 20;

        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if(!MainWindow.this.alreadyShuttingDown) {
                    if(JOptionPane.showConfirmDialog(MainWindow.this, "Do you really want to stop the master server?", "Really Closing?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                        if(MainWindow.this.server != null) {
                            MainWindow.this.server.shutdown();
                        }
                        MainWindow.this.alreadyShuttingDown = true;
                        MainWindow.this.setDefaultCloseOperation(EXIT_ON_CLOSE);
                        MainWindow.this.dispatchEvent(new WindowEvent(MainWindow.this, WindowEvent.WINDOW_CLOSING));
                    }
                }
            }
        });
        this.setTitle("PowerServer");
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.setLayout(new BorderLayout());
        JTextArea textArea = new JTextArea();
        JScrollPane pane = new JScrollPane(textArea);
        pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        textArea.setEditable(false);
        this.add(pane, BorderLayout.CENTER);
        JTextField textField = new JTextField();
        textField.addKeyListener(new KeyListener() {

            @Override
            public void keyReleased(KeyEvent event) {}

            @Override
            public void keyPressed(KeyEvent event) {
                if(event.getKeyCode() == KeyEvent.VK_ENTER) {
                    JTextField source = (JTextField) event.getSource();
                    String line = source.getText();
                    if(!line.trim().isEmpty()) {
                        source.setText("");
                        MainWindow.this.addCommandToHistory(line);
                        MainWindow.this.server.issueCommand(line);
                    }
                } else if(event.getKeyCode() == KeyEvent.VK_UP) {
                    String previous = MainWindow.this.selectPreviousCommand();
                    if(previous != null) {
                        JTextField source = (JTextField) event.getSource();
                        source.setText(previous);
                    }
                } else if(event.getKeyCode() == KeyEvent.VK_DOWN) {
                    String next = MainWindow.this.selectNextCommand();
                    if(next != null) {
                        JTextField source = (JTextField) event.getSource();
                        source.setText(next);
                    }
                } else if(event.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    JTextField source = (JTextField) event.getSource();
                    source.setText("");
                }
            }

            @Override
            public void keyTyped(KeyEvent e) {}
        });
        this.add(textField, BorderLayout.SOUTH);
        this.setMinimumSize(new Dimension(600, 400));
        this.setResizable(true);
        this.setLocationRelativeTo(null);
        pack();
        TextAreaOutputStream taos = new TextAreaOutputStream(textArea);
        PrintStream ps = new PrintStream(taos);
        System.setErr(ps);
        System.setOut(ps);
        Logger.guiInUse = true;
    }

    @Override
    public void update(Observable arg0, Object arg1) {
        // TODO Auto-generated method stub

    }

    public void setModel(PowerServer server) {
        this.server = server;
        this.server.addObserver(this);
    }

    private void addCommandToHistory(String line) {
        if(!this.commandHistory.contains(line)) {
            this.commandHistory.add(line);
        }
        while(this.commandHistory.size() > this.maxHistorySize) {
            this.commandHistory.remove(0);
        }
        this.selectedCommandIndex.setValue(CommandHistoryPos.END);
    }

    private String selectPreviousCommand() {
        if(this.selectedCommandIndex.equals(CommandHistoryPos.START)) { return null; }
        if(this.selectedCommandIndex.equals(CommandHistoryPos.END) && this.commandHistory.size() > 0) {
            this.selectedCommandIndex.setValue(this.commandHistory.size() - 1);
        } else if(0 < this.selectedCommandIndex.getValue() && this.selectedCommandIndex.getValue() <= this.commandHistory.size()) {
            this.selectedCommandIndex.setValue(this.selectedCommandIndex.getValue() - 1);
        } else {
            this.selectedCommandIndex.setValue(CommandHistoryPos.START);
            return null;
        }
        return this.commandHistory.get(this.selectedCommandIndex.getValue());
    }

    private String selectNextCommand() {
        if(this.selectedCommandIndex.equals(CommandHistoryPos.END)) { return null; }
        if(this.selectedCommandIndex.equals(CommandHistoryPos.START) && this.commandHistory.size() > 0) {
            this.selectedCommandIndex.setValue(0);
        } else if(-1 <= this.selectedCommandIndex.getValue() && this.selectedCommandIndex.getValue() < this.commandHistory.size() - 1) {
            this.selectedCommandIndex.setValue(this.selectedCommandIndex.getValue() + 1);
        } else {
            this.selectedCommandIndex.setValue(CommandHistoryPos.END);
            return null;
        }
        return this.commandHistory.get(this.selectedCommandIndex.getValue());
    }

    private enum CommandHistoryPos {
        START(Integer.MIN_VALUE),
        POSITION(Integer.MAX_VALUE),
        END(Integer.MAX_VALUE);

        private int value;

        private CommandHistoryPos(int value) {
            this.value = value;
        }

        public boolean equals(CommandHistoryPos chp) {
            return this.value == chp.value;
        }

        public void setValue(CommandHistoryPos chp) {
            this.setValue(chp.value);
        }

        public void setValue(int value) {
            if(this == POSITION) {
                this.value = value;
            }
        }

        public int getValue() {
            return this.value;
        }
    }
}
