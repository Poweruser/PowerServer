package de.poweruser.powerserver.main.parser.dataverification;

public class DummyVerify implements VerificationInterface {

    @Override
    public boolean verify(String data) {
        return true;
    }

    @Override
    public VerificationInterface createCopy() {
        return new DummyVerify();
    }

}
