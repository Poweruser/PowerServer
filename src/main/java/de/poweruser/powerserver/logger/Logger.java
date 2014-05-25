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
    public static boolean guiInUse;
    private LogLevel logLevel;

    public Logger(File logFile) throws IOException {
        guiInUse = false;
        instance = this;
        this.logLevel = LogLevel.NORMAL;
        this.logFile = logFile;
        if(!this.logFile.exists()) {
            this.logFile.createNewFile();
        }
        if(!this.logFile.canWrite()) { throw new IOException("Unable to write to the log file " + this.logFile.getAbsolutePath()); }
        this.dateFormat = new SimpleDateFormat("[dd.MM.yy-HH:mm:ss]");
    }

    public static void log(LogLevel level, ParserException exception) {
        StringBuilder sb = new StringBuilder();
        sb.append(instance.currentTimeString());
        sb.append(" ");
        sb.append(exception.getErrorMessage());
        sb.append(" For Game \"");
        sb.append(exception.getGameName());
        sb.append("\" with received data:\n");
        sb.append(exception.getData());
        logStatic(level, sb.toString());
    }

    public static void logStackTraceStatic(LogLevel level, String message, Exception e) {
        ByteArrayOutputStream ba = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(ba);
        pw.println(message);
        e.printStackTrace(pw);
        pw.close();
        logStatic(level, ba.toString());
    }

    public void log(LogLevel level, String message) {
        if(level.doesPass(this.getLogLevel())) {
            this.writeToFile(message);
        }
    }

    public static void logStatic(LogLevel level, String message) {
        if(instance != null && level.doesPass(instance.getLogLevel())) {
            instance.writeToFile(message);
        }
    }

    private synchronized void writeToFile(String message) {
        String output = this.currentTimeString() + " " + message;
        if(guiInUse) {
            System.out.println(output);
        }
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(this.logFile, true));
            bw.newLine();
            bw.write(output);
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

    public void setLogLevel(LogLevel level) {
        this.logLevel = level;
    }

    public static void setLogLevel(int value) {
        if(instance != null) {
            LogLevel l = LogLevel.valueToLevel(value);
            if(l != null) {
                instance.setLogLevel(l);
            }
        }
    }

    public LogLevel getLogLevel() {
        return this.logLevel;
    }
}
