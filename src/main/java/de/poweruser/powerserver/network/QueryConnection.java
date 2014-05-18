package de.poweruser.powerserver.network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import de.poweruser.powerserver.games.GameBase;
import de.poweruser.powerserver.games.GeneralDataKeysEnum;
import de.poweruser.powerserver.gamespy.EncType;
import de.poweruser.powerserver.gamespy.GamespyValidation;
import de.poweruser.powerserver.gamespy.encoders.EncoderInterface;
import de.poweruser.powerserver.logger.Logger;
import de.poweruser.powerserver.main.MessageData;
import de.poweruser.powerserver.main.parser.GamespyProtocol1Parser;
import de.poweruser.powerserver.main.parser.ParserException;
import de.poweruser.powerserver.main.parser.dataverification.IntVerify;

public class QueryConnection {

    private enum State {
        NEW,
        CHALLENGE_SENT,
        CHALLENGE_VALID,
        CHALLENGE_INVALID,
        QUERY_RECEIVED,
        QUERY_INVALID,
        LIST_SENT,
        TOOMUCHDATA,
        TIMEOUT,
        DONE;
    }

    private Socket client;
    private State state;
    private long lastStateChange;
    private DataInputStream in;
    private DataOutputStream out;
    private GamespyValidation validation;
    private byte[] receiveBuffer;
    private int receivePos;
    private GameBase requestedGame;
    private EncType encType;

    public QueryConnection(Socket client) throws IOException {
        this.client = client;
        this.changeState(State.NEW);
        this.in = new DataInputStream(new BufferedInputStream(this.client.getInputStream()));
        this.out = new DataOutputStream(new BufferedOutputStream(this.client.getOutputStream()));
        this.receiveBuffer = new byte[512];
        this.receivePos = 0;
        this.requestedGame = null;
        this.encType = null;
    }

    public void close() {
        try {
            this.in.close();
        } catch(IOException e1) {}
        try {
            this.out.close();
        } catch(IOException e1) {}
        try {
            this.client.close();
        } catch(IOException e) {}
        this.changeState(State.DONE);
    }

    public boolean check() {
        if(this.checkLastStateChange(TimeUnit.MINUTES, 1)) {
            this.changeState(State.TIMEOUT);
        }
        switch(this.state) {
            case NEW:
                this.validation = new GamespyValidation();
                String challenge = this.validation.getChallengeString();
                this.sendData("\\basic\\\\secure\\" + challenge);
                this.changeState(State.CHALLENGE_SENT);
                break;
            case CHALLENGE_SENT:
                this.readInput();
                Boolean response = this.checkChallengeResponse();
                if(response != null) {
                    if(response.booleanValue()) {
                        this.changeState(State.CHALLENGE_VALID);
                    } else {
                        this.changeState(State.CHALLENGE_INVALID);
                    }
                }
                break;
            case CHALLENGE_VALID:
                this.readInput();
                Boolean query = this.checkListQuery();
                if(query != null) {
                    if(query.booleanValue()) {
                        this.changeState(State.QUERY_RECEIVED);
                    } else {
                        this.changeState(State.QUERY_INVALID);
                    }
                }
                break;
            case QUERY_RECEIVED:
                this.sendServerList();
            case QUERY_INVALID:
            case CHALLENGE_INVALID:
            case TOOMUCHDATA:
            case TIMEOUT:
                this.close();
                this.changeState(State.DONE);
                break;
            case DONE:
                return true;
            default:
                break;
        }
        return false;
    }

    private void sendServerList() {
        if(this.requestedGame != null && this.encType != null) {
            EncoderInterface encoder = this.encType.getEncoder();
            if(encoder == null) { return; }
            byte[] data = null;
            try {
                data = encoder.encode(this.requestedGame.getActiveServers());
            } catch(IOException e) {
                Logger.logStackTraceStatic("Error while encoding a serverlist with Encoder " + encoder.getClass().getSimpleName() + " for EncType " + this.encType.toString() + ": " + e.toString(), e);
            }
            if(data != null) {
                this.sendData(data);
            }
        }
    }

    private Boolean checkListQuery() {
        Boolean out = null;
        String str = new String(this.receiveBuffer, 0, this.receivePos);
        GamespyProtocol1Parser parser = new GamespyProtocol1Parser();
        MessageData data = null;
        try {
            data = parser.parse(null, str);
        } catch(ParserException e) {
            Logger.logStatic("Error while checking list query:");
            Logger.log(e);
        }
        if(data != null) {
            if(data.containsKey(GeneralDataKeysEnum.GAMENAME) && data.containsKey(GeneralDataKeysEnum.LIST) && data.containsKey(GeneralDataKeysEnum.FINAL)) {
                GameBase game = GameBase.getGameForGameName(data.getData(GeneralDataKeysEnum.GAMENAME));
                if(game != null) {
                    this.requestedGame = game;
                    EncType enctype = this.getEncTypeFromData(data);
                    if(enctype != null) {
                        this.encType = enctype;
                    }
                    out = new Boolean(true);
                } else {
                    out = new Boolean(false);
                }
                this.clearBufferUpToKey(GeneralDataKeysEnum.FINAL);
            }
        }
        return out;
    }

    private void readInput() {
        try {
            int len = this.in.available();
            if(len > 0) {
                int newSize = this.receivePos + len;
                if(newSize > this.receiveBuffer.length) {
                    if(newSize > 1024) {
                        this.changeState(State.TOOMUCHDATA);
                        return;
                    }
                    byte[] newBuffer = new byte[newSize];
                    System.arraycopy(this.receiveBuffer, 0, newBuffer, 0, this.receivePos);
                    this.receiveBuffer = newBuffer;
                }
                this.in.read(this.receiveBuffer, this.receivePos, len);
                this.receivePos += len;
            }
        } catch(IOException e) {
            Logger.logStackTraceStatic("Error while reading input: " + e.toString(), e);
        }
    }

    private Boolean checkChallengeResponse() {
        Boolean out = null;
        String str = new String(this.receiveBuffer, 0, this.receivePos);
        if(str.isEmpty()) { return out; }
        GamespyProtocol1Parser parser = new GamespyProtocol1Parser();
        MessageData data = null;
        try {
            data = parser.parse(null, str);
        } catch(ParserException e) {
            Logger.logStatic("Error while checking challenge response:");
            Logger.log(e);
        }
        if(data != null) {
            if(data.containsKey(GeneralDataKeysEnum.GAMENAME) && data.containsKey(GeneralDataKeysEnum.FINAL) && data.containsKey(GeneralDataKeysEnum.ENCTYPE) && data.containsKey(GeneralDataKeysEnum.VALIDATE)) {
                String gamename = data.getData(GeneralDataKeysEnum.GAMENAME);
                EncType enctype = this.getEncTypeFromData(data);
                String response = data.getData(GeneralDataKeysEnum.VALIDATE);
                GameBase game = GameBase.getGameForGameName(gamename);
                if(game != null && enctype != null) {
                    if(this.validation.verifyChallengeResponse(game, enctype, response)) {
                        this.encType = enctype;
                        out = new Boolean(true);
                    } else {
                        out = new Boolean(false);
                    }
                } else {
                    out = new Boolean(false);
                }
                this.clearBufferUpToKey(GeneralDataKeysEnum.FINAL);
            }
        }
        return out;
    }

    public InetAddress getClientAddress() {
        return this.client.getInetAddress();
    }

    private void sendData(String data) {
        this.sendData(data.getBytes());
    }

    private void sendData(byte[] data) {
        try {
            this.out.write(data);
            this.out.flush();
        } catch(IOException e) {
            Logger.logStackTraceStatic("Error while sending data to " + this.client.getInetAddress().toString(), e);
        }
    }

    private EncType getEncTypeFromData(MessageData data) {
        GeneralDataKeysEnum enc = GeneralDataKeysEnum.ENCTYPE;
        if(data.containsKey(enc)) {
            IntVerify v = (IntVerify) enc.getVerifierCopy();
            if(v.verify(data.getData(enc))) { return EncType.getTypeFromValue(v.getVerifiedValue()); }
        }
        return null;
    }

    private void clearBufferUpToKey(GeneralDataKeysEnum key) {
        String content = new String(this.receiveBuffer, 0, this.receivePos);
        String str = "\\" + key.toString();
        int index = content.indexOf(str);
        int newStart = index + str.length();
        int newLength = this.receivePos - newStart;
        System.arraycopy(this.receiveBuffer, newStart, this.receiveBuffer, 0, newLength);
        this.receivePos = newLength;
    }

    private void changeState(State state) {
        this.lastStateChange = System.currentTimeMillis();
        this.state = state;
    }

    private boolean checkLastStateChange(TimeUnit unit, int value) {
        return (System.currentTimeMillis() - this.lastStateChange) < TimeUnit.MILLISECONDS.convert(value, unit);
    }
}
