package de.poweruser.powerserver.main.parser.dataverification;

import de.poweruser.powerserver.main.QueryInfo;

public class QueryIdFormatVerify implements VerificationInterface {

    public QueryInfo verifiedQueryInfo;

    @Override
    public boolean verify(String data) {
        this.verifiedQueryInfo = null;
        String[] split = data.split("\\.");
        if(split.length == 2) {
            boolean ok = true;
            int[] values = new int[2];
            for(int i = 0; i < 2; i++) {
                String str = split[i];
                try {
                    values[i] = Integer.parseInt(str);
                    ok &= (values[i] >= 1);
                } catch(NumberFormatException e) {
                    return false;
                }
            }
            if(ok) {
                this.verifiedQueryInfo = new QueryInfo(values[0], values[1]);
            }
            return ok;
        }
        return false;
    }

    public QueryInfo getVerifiedQueryInfo() {
        return this.verifiedQueryInfo;
    }
}
