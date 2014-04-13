package de.poweruser.powerserver.main.parser.dataverification;

public class IntVerify implements VerificationInterface {

    private final int min;
    private final int max;

    public IntVerify(int min, int max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public boolean verify(String data) {
        try {
            int i = Integer.parseInt(data);
            return (i >= this.min && i <= this.max);
        } catch(NumberFormatException e) {
            return false;
        }
    }
}
