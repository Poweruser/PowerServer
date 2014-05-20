package de.poweruser.powerserver.main;

public interface CombineableInterface<T> {

    public T combine(T combineable);

    public void update(T combineable);

}
