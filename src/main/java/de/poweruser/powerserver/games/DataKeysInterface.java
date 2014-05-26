package de.poweruser.powerserver.games;

public interface DataKeysInterface {

    /**
     * Returns the key string that is assigned to this data key. This key string
     * will be used primarily by the parsers, but it may find use at a lot of
     * other places as well.
     * 
     * @return the key string assigned to this data key
     */

    public String getKeyString();

    /**
     * Verifies the passed String data against the verifier that is assigned to
     * this data key
     * 
     * @param The
     *            data to check as a String
     * @return true, if the data passes the checks of the verifier that is
     *         assigned to this data key
     *         false, if it does not pass the checks
     */

    public boolean verifyData(String data);
}
