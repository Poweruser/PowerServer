package de.poweruser.powerserver.logger;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.poweruser.powerserver.main.parser.ParserException;

public class Logger {

    private File logFile;
    private SimpleDateFormat dateFormat;
    private static Logger instance;

    public Logger(File logFile) throws IOException {
        this.instance = this;
        this.logFile = logFile;
        if(!this.logFile.exists()) {
            this.logFile.mkdirs();
            this.logFile.createNewFile();
        }
        if(!this.logFile.canWrite()) { throw new IOException("Unable to write to the log file " + this.logFile.getAbsolutePath()); }
        this.dateFormat = new SimpleDateFormat("[dd.MM.yy-HH:mm:ss]");
    }

    public void log(ParserException exception) {
        StringBuilder sb = new StringBuilder();
        sb.append(this.currentTimeString());
        sb.append(" ");
        sb.append(exception.getErrorMessage());
        sb.append(" For Game \"");
        sb.append(exception.getGame().getGameName());
        sb.append("\" with received data:\n");
        sb.append(exception.getUDPMessage().toString());
        this.writeToFile(sb.toString());
    }

    public static void logStackTraceStatic(String message, Exception e) {
        ByteArrayOutputStream ba = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(ba);
        pw.println(message);
        e.printStackTrace(pw);
        pw.close();
        logStatic(ba.toString());
    }

    public void log(String message) {
        this.writeToFile(message);
    }

    public static void logStatic(String message) {
        instance.writeToFile(message);
    }

    private synchronized void writeToFile(String message) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(this.logFile, true));
            bw.newLine();
            bw.write(this.currentTimeString() + " " + message);
            bw.flush();
        } catch(IOException e) {}
        if(bw != null) {
            try {
                bw.close();
            } catch(IOException e) {}
        }
    }

    private String currentTimeString() {
        return this.dateFormat.format(new Date());
    }
}
