package github.eliotweber.webrtc_server;

public interface WebrtcConnectionHandler {

    default boolean getPassReconnect() {
        return true;
    }

    default boolean getShouldReconnect() {
        return true;
    }

    void onOpen();
    void onClose();

    void onError(String message);

    void onSignalMessage(String message);
    void onDataMessage(String message);

    void onReconnect();
    void onSetup();
}
