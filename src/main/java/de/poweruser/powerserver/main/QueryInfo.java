package de.poweruser.powerserver.main;

public class QueryInfo {
    private int id;
    private int part;
    private boolean isFinal;

    public QueryInfo(int id, int part) {
        this.id = id;
        this.part = part;
        this.isFinal = false;
    }

    public int getId() {
        return this.id;
    }

    public int getPart() {
        return this.part;
    }

    public boolean isFinal() {
        return this.isFinal;
    }

    public void setFinal() {
        this.isFinal = true;
    }
}
