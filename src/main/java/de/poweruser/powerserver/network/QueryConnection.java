package de.poweruser.powerserver.network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import de.poweruser.powerserver.games.GameBase;
import de.poweruser.powerserver.games.GeneralDataKeysEnum;
import de.poweruser.powerserver.gamespy.EncType;
import de.poweruser.powerserver.gamespy.GamespyValidation;
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
        LIST_SENT,
        DONE;
    }

    private Socket client;
    private State state;
    private DataInputStream in;
    private DataOutputStream out;
    private GamespyValidation validation;
    private byte[] receiveBuffer;
    private int receivePos;

    public QueryConnection(Socket client) throws IOException {
        this.client = client;
        this.state = State.NEW;
        this.in = new DataInputStream(new BufferedInputStream(this.client.getInputStream()));
        this.out = new DataOutputStream(new BufferedOutputStream(this.client.getOutputStream()));
        this.receiveBuffer = new byte[512];
        this.receivePos = 0;
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
        this.state = State.DONE;
    }

    public boolean check() {
        switch(this.state) {
            case NEW:
                this.validation = new GamespyValidation();
                String challenge = this.validation.getChallengeString();
                this.sendData("\\basic\\\\secure\\" + challenge);
                this.state = State.CHALLENGE_SENT;
                break;
            case CHALLENGE_SENT:
                this.readInput();
                Boolean response = this.checkChallengeResponse();
                if(response != null) {
                    if(response.booleanValue()) {
                        this.state = State.CHALLENGE_VALID;
                    } else {
                        this.state = State.CHALLENGE_INVALID;
                    }
                }
                break;
            case CHALLENGE_VALID:
                this.readInput();
                Boolean query = this.checkListQuery();
                if(query != null && query.booleanValue()) {
                    this.state = State.QUERY_RECEIVED;
                }
                break;
            case QUERY_RECEIVED:
                this.sendServerList();
                break;
            case CHALLENGE_INVALID:
                this.close();
                this.state = State.DONE;
                break;
            case DONE:
                return true;
            default:
                break;
        }
        return false;
    }

    private void sendServerList() {
        // TODO Auto-generated method stub

    }

    private Boolean checkListQuery() {
        // TODO Auto-generated method stub
        return null;
    }

    private void readInput() {
        try {
            int len = this.in.available();
            if(len > 0) {
                if(this.receivePos + len > this.receiveBuffer.length) {
                    byte[] newBuffer = new byte[this.receivePos + len];
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
        GamespyProtocol1Parser parser = new GamespyProtocol1Parser();
        MessageData data = null;
        try {
            data = parser.parse(null, str);
        } catch(ParserException e) {
            Logger.logStackTraceStatic("Error while checking challenge response:" + e.toString(), e);
        }
        if(data != null) {
            if(data.containsKey(GeneralDataKeysEnum.GAMENAME) && data.containsKey(GeneralDataKeysEnum.FINAL) && data.containsKey(GeneralDataKeysEnum.ENCTYPE) && data.containsKey(GeneralDataKeysEnum.VALIDATE)) {
                String gamename = data.getData(GeneralDataKeysEnum.GAMENAME);
                GeneralDataKeysEnum enc = GeneralDataKeysEnum.ENCTYPE;
                IntVerify v = (IntVerify) enc.getVerifierCopy();
                v.verify(data.getData(enc));
                EncType enctype = EncType.getTypeFromValue(v.getVerifiedValue());
                String response = data.getData(GeneralDataKeysEnum.VALIDATE);
                GameBase game = GameBase.getGameForGameName(gamename);
                if(game != null && enctype != null) {
                    if(this.validation.verifyChallengeResponse(game, enctype, response)) {
                        this.state = State.CHALLENGE_VALID;
                    } else {
                        this.state = State.CHALLENGE_INVALID;
                    }
                } else {
                    Logger.logStatic("Error while checking a challengeResponse:");
                    if(game == null) {
                        Logger.logStatic("No game found for gamename: " + gamename);
                    }
                    if(enctype == null) {
                        Logger.logStatic("No EncType found for id: " + v.getVerifiedValue());
                    }
                    this.state = State.CHALLENGE_INVALID;
                }
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
}
