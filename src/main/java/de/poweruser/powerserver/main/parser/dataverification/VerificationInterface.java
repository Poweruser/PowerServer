package de.poweruser.powerserver.main.parser.dataverification;

public interface VerificationInterface {
    public boolean verify(String data);

    public VerificationInterface createCopy();
}
