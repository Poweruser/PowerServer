package de.poweruser.powerserver.main.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.io.PrintStream;
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

    public MainWindow() {
        this.server = null;
        this.alreadyShuttingDown = false;
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
        this.add(textField, BorderLayout.SOUTH);
        this.setMinimumSize(new Dimension(500, 400));
        this.setResizable(true);
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
}
