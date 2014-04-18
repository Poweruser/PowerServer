package de.poweruser.powerserver.main.parser.dataverification;

public class BooleanVerify implements VerificationInterface {

    @Override
    public boolean verify(String data) {
        switch(data.trim().toLowerCase()) {
            case "true":
            case "1":
            case "yes":
                return true;
            default:
        }
        return false;
    }

}
