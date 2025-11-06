package github.eliotweber.webrtc_server;

public abstract class WebrtcConnectionHandler {

    public boolean reconnect = true;
    public ConnectionManager connectionManager;

    public abstract void onClose();

    public abstract void onError(String message);

    public abstract void onSignalMessage(String[] flags, String payload);
    public abstract void onDataMessage(String message);

    @SuppressWarnings("unused")
    private void sendSignaling(String message, String[] flags) {
        this.connectionManager.sendSignaling(message, flags, false);
    }

    @SuppressWarnings("unused")
    private void sendData(String message) {
        this.connectionManager.sendData(message);
    }

    public abstract void onReconnect();
    public abstract void onSetup();
}
