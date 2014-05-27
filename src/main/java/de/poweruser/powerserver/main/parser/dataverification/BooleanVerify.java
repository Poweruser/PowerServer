package de.poweruser.powerserver.main.parser.dataverification;

public class BooleanVerify implements VerificationInterface {

    @Override
    public boolean verify(String data) {
        String trimmed = data.trim().toLowerCase();
        return (trimmed.equalsIgnoreCase("true") || trimmed.equalsIgnoreCase("1") || trimmed.equalsIgnoreCase("yes"));
    }

    @Override
    public BooleanVerify createCopy() {
        return new BooleanVerify();
    }
}
