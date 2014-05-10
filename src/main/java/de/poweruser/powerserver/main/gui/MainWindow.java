package de.poweruser.powerserver.main.gui;

import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import de.poweruser.powerserver.main.PowerServer;

public class MainWindow extends JFrame implements Observer {

    private static final long serialVersionUID = 2846198182943968671L;

    private PowerServer server;

    public MainWindow(PowerServer server) {
        this.server = server;
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if(JOptionPane.showConfirmDialog(MainWindow.this, "Do you really want to stop the master server?", "Really Closing?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                    MainWindow.this.server.shutdown();
                }
            }
        });
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
    }

    @Override
    public void update(Observable arg0, Object arg1) {
        // TODO Auto-generated method stub

    }
}
