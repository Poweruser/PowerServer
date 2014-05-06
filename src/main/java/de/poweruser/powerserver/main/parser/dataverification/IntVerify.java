package de.poweruser.powerserver.main.parser.dataverification;

public class IntVerify implements VerificationInterface {

    private final int min;
    private final int max;
    private int verifiedValue;

    public IntVerify(int min, int max) {
        this.min = min;
        this.max = max;
        this.verifiedValue = 0;
    }

    @Override
    public boolean verify(String data) {
        try {
            int i = Integer.parseInt(data);
            boolean result = i >= this.min && i <= this.max;
            if(result) {
                this.verifiedValue = i;
            }
            return result;
        } catch(NumberFormatException e) {
            return false;
        }
    }

    public int getVerifiedValue() {
        return this.verifiedValue;
    }

    @Override
    public IntVerify createCopy() {
        return new IntVerify(this.min, this.max);
    }
}
