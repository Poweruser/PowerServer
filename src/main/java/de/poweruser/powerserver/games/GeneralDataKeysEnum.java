package de.poweruser.powerserver.games;

import de.poweruser.powerserver.main.parser.dataverification.IPAddressVerify;
import de.poweruser.powerserver.main.parser.dataverification.IntVerify;
import de.poweruser.powerserver.main.parser.dataverification.QueryIdFormatVerify;
import de.poweruser.powerserver.main.parser.dataverification.StringVerify;
import de.poweruser.powerserver.main.parser.dataverification.VerificationInterface;

public enum GeneralDataKeysEnum implements DataKeysInterface {
    FINAL("final", new StringVerify(new String[] {}, false)),
    GAMENAME("gamename"),
    HEARTBEAT("heartbeat", new IntVerify(1024, 65535)),
    HEARTBEATBROADCAST("heartbeatbroadcast", new IntVerify(1024, 65535)),
    HOST("host", new IPAddressVerify()),
    QUERYID("queryid", new QueryIdFormatVerify()),
    STATECHANGED("statechanged", new IntVerify(0, 1));

    private String key;
    private VerificationInterface verifier;

    private GeneralDataKeysEnum(String key) {
        this.key = key;
        this.verifier = null;
    }

    private GeneralDataKeysEnum(String key, VerificationInterface verifier) {
        this.key = key;
        this.verifier = verifier;
    }

    @Override
    public String getKeyString() {
        return this.key;
    }

    @Override
    public boolean verifyData(String data) {
        if(this.verifier == null || this.verifier.verify(data)) {
            return true;
        } else {
            return false;
        }
    }
}
