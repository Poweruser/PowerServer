package de.poweruser.powerserver.main.parser.dataverification;

public class StringLengthVerify implements VerificationInterface {

    private int min;
    private int max;

    public StringLengthVerify(int min, int max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public boolean verify(String data) {
        return data.length() >= this.min && data.length() <= this.max;
    }

    @Override
    public StringLengthVerify createCopy() {
        return new StringLengthVerify(this.min, this.max);
    }
}
