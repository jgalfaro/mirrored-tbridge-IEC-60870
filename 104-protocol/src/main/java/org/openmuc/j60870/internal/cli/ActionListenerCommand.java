package main.java.org.openmuc.j60870.internal.cli;

public interface ActionListenerCommand {

    public void actionCalled(String actionKey) throws ActionException;

    public void quit();

}
