package de.poweruser.powerserver.main.parser.dataverification;

public class StringVerify implements VerificationInterface {

    private final String[] options;
    private final boolean caseSensitive;

    public StringVerify(String[] options, boolean caseSenstive) {
        this.options = options;
        this.caseSensitive = caseSenstive;
    }

    @Override
    public boolean verify(String data) {
        for(String str: this.options) {
            if(this.caseSensitive && str.equals(data)) {
                return true;
            } else if(!this.caseSensitive && str.equalsIgnoreCase(data)) { return true; }
        }
        return false;
    }

    @Override
    public StringVerify createCopy() {
        return new StringVerify(this.options, this.caseSensitive);
    }
}
