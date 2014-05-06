package de.poweruser.powerserver.games;

import de.poweruser.powerserver.main.parser.dataverification.IPAddressVerify;
import de.poweruser.powerserver.main.parser.dataverification.IntVerify;
import de.poweruser.powerserver.main.parser.dataverification.QueryIdFormatVerify;
import de.poweruser.powerserver.main.parser.dataverification.StringLengthVerify;
import de.poweruser.powerserver.main.parser.dataverification.StringVerify;
import de.poweruser.powerserver.main.parser.dataverification.VerificationInterface;

public enum GeneralDataKeysEnum implements DataKeysInterface {
    ENCTYPE("enctype", new IntVerify(0, 3)),
    FINAL("final", new StringVerify(new String[] {}, false)),
    GAMENAME("gamename"),
    HEARTBEAT("heartbeat", new IntVerify(1024, 65535)),
    HEARTBEATBROADCAST("heartbeatbroadcast", new IntVerify(1024, 65535)),
    HOST("host", new IPAddressVerify()),
    LIST("list", new StringVerify(new String[] { "cmp" }, false)),
    QUERYID("queryid", new QueryIdFormatVerify()),
    STATECHANGED("statechanged", new IntVerify(0, 1)),
    VALIDATE("validate", new StringLengthVerify(8, 8));

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

    public Object getVerifierCopy() {
        return this.verifier.createCopy();
    }
}
