package github.eliotweber.webrtc_server;

public class DefaultHandler implements WebrtcConnectionHandler {

    public void onOpen() {
        System.out.println("Connection opened");
    }
    public void onClose() {
        System.out.println("Connection closed");
    }

    public void onError(String message) {
        System.err.println("Error: " + message);
    }

    public void onSignalMessage(String message) {
        System.out.println("Signal message: " + message);
    }
    public void onDataMessage(String message) {
        System.out.println("Data message: " + message);
    }

    public void onReconnect() {
        System.out.println("Reconnecting...");
    }
    public void onSetup() {
        System.out.println("Setup complete.");
    }
    
}
