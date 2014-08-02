package de.poweruser.powerserver.main.gui;

import java.awt.EventQueue;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JScrollBar;
import javax.swing.JTextArea;

public class TextAreaOutputStream extends OutputStream {

    // *************************************************************************************************
    // INSTANCE MEMBERS
    // *************************************************************************************************

    private byte[] oneByte; // array for write(int val);
    private Appender appender; // most recent action

    public TextAreaOutputStream(JTextArea txtara) {
        this(txtara, 1000);
    }

    public TextAreaOutputStream(JTextArea txtara, int maxlin) {
        if(maxlin < 1) { throw new IllegalArgumentException("TextAreaOutputStream maximum lines must be positive (value=" + maxlin + ")"); }
        oneByte = new byte[1];
        appender = new Appender(txtara, maxlin);
    }

    public void setVerticalScrollBar(JScrollBar verticalScrollBar) {
        this.appender.setVerticalScrollBar(verticalScrollBar);
    }

    /** Clear the current console text area. */
    public synchronized void clear() {
        if(appender != null) {
            appender.clear();
        }
    }

    @Override
    public synchronized void close() {
        appender = null;
    }

    @Override
    public synchronized void flush() {}

    @Override
    public synchronized void write(int val) {
        oneByte[0] = (byte) val;
        write(oneByte, 0, 1);
    }

    @Override
    public synchronized void write(byte[] ba) {
        write(ba, 0, ba.length);
    }

    @Override
    public synchronized void write(byte[] ba, int str, int len) {
        if(appender != null) {
            appender.append(bytesToString(ba, str, len));
        }
    }

    static private String bytesToString(byte[] ba, int str, int len) {
        try {
            return new String(ba, str, len, "UTF-8");
        } catch(UnsupportedEncodingException thr) {
            return new String(ba, str, len);
        } // all JVMs are required to support UTF-8
    }

    // *************************************************************************************************
    // STATIC MEMBERS
    // *************************************************************************************************

    static class Appender implements Runnable {
        private final JTextArea textArea;
        private JScrollBar vScrollBar;
        private final int maxLines; // maximum lines allowed in text area
        private final LinkedList<Integer> lengths; // length of lines within
                                                   // text area
        private final List<String> values; // values waiting to be appended

        private int curLength; // length of current line
        private boolean clear;
        private boolean queue;

        private static final String EOL1 = "\n";
        private static final String EOL2 = System.getProperty("line.separator", EOL1);

        Appender(JTextArea txtara, int maxlin) {
            textArea = txtara;
            maxLines = maxlin;
            lengths = new LinkedList<Integer>();
            values = new ArrayList<String>();
            vScrollBar = null;

            curLength = 0;
            clear = false;
            queue = true;
        }

        synchronized void append(String val) {
            values.add(val);
            if(queue) {
                queue = false;
                EventQueue.invokeLater(this);
            }
        }

        synchronized void clear() {
            clear = true;
            curLength = 0;
            lengths.clear();
            values.clear();
            if(queue) {
                queue = false;
                EventQueue.invokeLater(this);
            }
        }

        // MUST BE THE ONLY METHOD THAT TOUCHES textArea!
        @Override
        public synchronized void run() {
            if(clear) {
                textArea.setText("");
            }
            for(String val: values) {
                curLength += val.length();
                if(val.endsWith(EOL1) || val.endsWith(EOL2)) {
                    if(lengths.size() >= maxLines) {
                        textArea.replaceRange("", 0, lengths.removeFirst());
                    }
                    lengths.addLast(curLength);
                    curLength = 0;
                }
                boolean scroll = false;
                if(vScrollBar != null) {
                    int currentPosition = vScrollBar.getValue();
                    int maxPosition = vScrollBar.getMaximum() - vScrollBar.getVisibleAmount();
                    scroll = (currentPosition >= maxPosition - 200);
                }
                textArea.append(val);
                if(scroll) {
                    textArea.setCaretPosition(textArea.getDocument().getLength());
                }
            }
            values.clear();
            clear = false;
            queue = true;
        }

        public void setVerticalScrollBar(JScrollBar verticalScrollBar) {
            vScrollBar = verticalScrollBar;
        }
    }
}
