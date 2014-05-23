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

    public enum State {
        NEW,
        CHALLENGE_SENT,
        CHALLENGE_VALID,
        CHALLENGE_INVALID,
        QUERY_RECEIVED,
        QUERY_INVALID,
        LIST_SENT,
        SENDING_FAILED,
        TOOMUCHDATA,
        TIMEOUT,
        SUCCESSFUL,
        DONE;
    }

    private Socket client;
    private State state;
    private State failedState;
    private String failMessage;
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
        this.failedState = null;
        this.failMessage = null;
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
    }

    public void forceClose() {
        this.close();
        this.changeState(State.DONE);
    }

    public boolean check() {
        if(!this.checkLastStateChange(TimeUnit.SECONDS, 10)) {
            if(this.state.equals(State.LIST_SENT)) {
                this.changeState(State.SUCCESSFUL);
            } else {
                this.changeStateFail(State.TIMEOUT, "The connection timed out. Last state: " + this.state.toString());
            }
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
                Result response = this.checkChallengeResponse();
                if(response != null) {
                    if(response.getResult()) {
                        this.changeState(State.CHALLENGE_VALID);
                    } else {
                        this.changeStateFail(State.CHALLENGE_INVALID, response.getMessage());
                    }
                }
                break;
            case CHALLENGE_VALID:
                this.readInput();
                Result query = this.checkListQuery();
                if(query != null) {
                    if(query.getResult()) {
                        this.changeState(State.QUERY_RECEIVED);
                    } else {
                        this.changeStateFail(State.QUERY_INVALID, query.getMessage());
                    }
                }
                break;
            case QUERY_RECEIVED:
                Result sent = this.sendServerList();
                if(sent.getResult()) {
                    this.changeState(State.LIST_SENT);
                } else {
                    this.changeStateFail(State.SENDING_FAILED, sent.getMessage());
                }
                break;
            case LIST_SENT:
                break;
            case QUERY_INVALID:
            case CHALLENGE_INVALID:
            case TOOMUCHDATA:
            case TIMEOUT:
            case SUCCESSFUL:
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

    private Result sendServerList() {
        Result out = new Result(true);
        if(this.requestedGame != null && this.encType != null) {
            EncoderInterface encoder = this.encType.getEncoder();
            if(encoder != null) {
                byte[] data = null;
                try {
                    data = encoder.encode(this.requestedGame.getActiveServers());
                } catch(IOException e) {
                    Logger.logStackTraceStatic("Error while encoding a serverlist with Encoder " + encoder.getClass().getSimpleName() + " for EncType " + this.encType.toString() + ": " + e.toString(), e);
                }
                if(data != null) {
                    int count = 0;
                    for(int i = 4; i < 8; i++) {
                        count <<= 8;
                        count |= data[i];
                    }
                    String logMessage = "QUERY Successful from " + this.client.getInetAddress().toString() + " : Sent " + data.length + " Bytes (" + (count / 6) + " IPV4 Servers with " + count + " Bytes)";
                    Logger.logStatic(logMessage);

                    this.sendData(data);
                } else {
                    out = new Result(false, "Encoding of the server list failed");
                }
            } else {
                out = new Result(false, "No encoder available yet for EncType: " + this.encType.toString());
            }
        } else if(this.requestedGame == null) {
            out = new Result(false, "The query did not mention a game");
        } else if(this.encType == null) {
            out = new Result(false, "The query did not mention an EncType");
        }
        return out;
    }

    private Result checkListQuery() {
        Result out = null;
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
                String gameString = data.getData(GeneralDataKeysEnum.GAMENAME);
                GameBase game = GameBase.getGameForGameName(gameString);
                if(game != null) {
                    this.requestedGame = game;
                    EncType enctype = this.getEncTypeFromData(data);
                    if(enctype != null) {
                        this.encType = enctype;
                    }
                    out = new Result(true);
                } else {
                    out = new Result(false, "Could not find a matching game for \"" + gameString + "\" in the list query: " + str);
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
                        this.failedState = this.state;
                        this.failMessage = "The client has sent too much data";
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

    private Result checkChallengeResponse() {
        Result out = null;
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
                String gameString = data.getData(GeneralDataKeysEnum.GAMENAME);
                EncType enctype = this.getEncTypeFromData(data);
                String response = data.getData(GeneralDataKeysEnum.VALIDATE);
                GameBase game = GameBase.getGameForGameName(gameString);
                if(game != null && enctype != null) {
                    if(this.validation.verifyChallengeResponse(game, enctype, response)) {
                        this.encType = enctype;
                        out = new Result(true);
                    } else {
                        out = new Result(false, "Validation failed. EncType: " + enctype.toString() + " Secure: " + this.validation.getChallengeString() + " Received: " + response + " Should have been: " + this.validation.getValidationString());
                    }
                } else if(game == null) {
                    out = new Result(false, "Could not find a matching game for \"" + gameString + "\" in the challenge response: " + str);
                } else if(enctype == null) {
                    out = new Result(false, "Could not recognise the EncType \"" + data.getData(GeneralDataKeysEnum.ENCTYPE) + "\" in the challenge response: " + str);
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
        String str = "\\" + key.getKeyString();
        int index = content.toLowerCase().indexOf(str);
        if(index >= 0) {
            int newStart = index + str.length();
            int newLength = this.receivePos - newStart;
            System.arraycopy(this.receiveBuffer, newStart, this.receiveBuffer, 0, newLength);
            this.receivePos = newLength;
        }
    }

    private void changeState(State state) {
        this.lastStateChange = System.currentTimeMillis();
        this.state = state;
    }

    private void changeStateFail(State state, String message) {
        this.changeState(state);
        this.failedState = state;
        this.failMessage = message;
    }

    private boolean checkLastStateChange(TimeUnit unit, int value) {
        return (System.currentTimeMillis() - this.lastStateChange) < TimeUnit.MILLISECONDS.convert(value, unit);
    }

    public State getFailedState() {
        return this.failedState;
    }

    public String getFailMessage() {
        return this.failMessage;
    }

    private class Result {
        private String message;
        private boolean result;

        public Result(boolean result) {
            this.result = result;
        }

        public Result(boolean result, String message) {
            this(result);
            this.message = message;
        }

        public boolean getResult() {
            return this.result;
        }

        public String getMessage() {
            return this.message;
        }
    }
}
