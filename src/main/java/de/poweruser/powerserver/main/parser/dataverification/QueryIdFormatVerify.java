package de.poweruser.powerserver.main.parser.dataverification;

public class QueryIdFormatVerify implements VerificationInterface {

    @Override
    public boolean verify(String data) {
        String[] split = data.split("\\.");
        if(split.length == 2) {
            boolean ok = true;
            for(String str: split) {
                try {
                    int i = Integer.parseInt(str);
                    ok &= (i >= 1);
                } catch(NumberFormatException e) {
                    return false;
                }
            }
            return ok;
        }
        return false;
    }
}
