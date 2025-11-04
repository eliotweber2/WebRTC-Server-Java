package github.eliotweber.webrtc_server;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import dev.onvoid.webrtc.CreateSessionDescriptionObserver;
import dev.onvoid.webrtc.SetSessionDescriptionObserver;
import dev.onvoid.webrtc.PeerConnectionFactory;
import dev.onvoid.webrtc.RTCConfiguration;
import dev.onvoid.webrtc.RTCPeerConnection;
import dev.onvoid.webrtc.RTCSessionDescription;
import dev.onvoid.webrtc.RTCIceCandidate;
import dev.onvoid.webrtc.PeerConnectionObserver;
import dev.onvoid.webrtc.RTCDataChannelInit;
import dev.onvoid.webrtc.RTCDataChannelObserver;
import dev.onvoid.webrtc.RTCPriorityType;
import dev.onvoid.webrtc.RTCDataChannel;
import dev.onvoid.webrtc.RTCDataChannelBuffer;

public class ConnectionManager {
    public String id;
    private RTCPeerConnection connection;
    public List<RTCIceCandidate> iceCandidates = new ArrayList<>();

    private WebrtcConnectionHandler handler;

    private RTCDataChannel signalingChannel;
    public RTCDataChannel dataChannel;

    Timer closingTimer;
    private static final int CLOSING_TIMER_LENGTH = 1000;

    private static final RTCDataChannelInit signalingChannelConfig = new RTCDataChannelInit();
    static {
        signalingChannelConfig.priority = RTCPriorityType.HIGH;
    }

    private static final RTCDataChannelInit dataChannelConfig = new RTCDataChannelInit();
    static {
        dataChannelConfig.ordered = false;
        dataChannelConfig.maxPacketLifeTime = 1000;
    }

    public ConnectionManager(String id, PeerConnectionFactory factory, RTCConfiguration config, WebrtcConnectionHandler handler) {
        this.id = id;
        this.handler = handler;
        this.setupConnection(factory, config);
    }

    public void setupConnection(PeerConnectionFactory factory, RTCConfiguration config) {
        this.connection = factory.createPeerConnection(config, new PeerConnectionObserver() {
            @Override
            public void onIceCandidate(RTCIceCandidate candidate) {
                //System.out.println("New ICE candidate for connection " + id + ": " + candidate);
                iceCandidates.add(candidate);
            }
        });

        signalingChannel = this.connection.createDataChannel("signaling", signalingChannelConfig);

        signalingChannel.registerObserver(new RTCDataChannelObserver() {
            @Override 
            public void onStateChange() {}

            @Override
            public void onBufferedAmountChange(long previousAmount) {}

            @Override
            public void onMessage(RTCDataChannelBuffer buffer) {
                ByteBuffer data = buffer.data;
                byte[] bytes = new byte[data.remaining()];
                data.get(bytes);
                String message = new String(bytes, StandardCharsets.UTF_8);
                onSignalMessage(message);
            }
        });

        dataChannel = this.connection.createDataChannel("data", dataChannelConfig);

        dataChannel.registerObserver(new RTCDataChannelObserver() {
            @Override 
            public void onStateChange() {}

            @Override
            public void onBufferedAmountChange(long previousAmount) {}

            @Override
            public void onMessage(RTCDataChannelBuffer buffer) {
                ByteBuffer data = buffer.data;
                byte[] bytes = new byte[data.remaining()];
                data.get(bytes);
                String message = new String(bytes, StandardCharsets.UTF_8);
                handler.onDataMessage(message);
            }
        });
    }

    private void onSignalMessage(String message) {
        String[] flags = message.split(" | ");

        String payload = flags[flags.length - 1];

        flags = Arrays.copyOf(flags, flags.length - 1);

        switch (flags[0]) {
            case "MESSAGE":
                handler.onSignalMessage(Arrays.copyOfRange(flags, 1, flags.length), payload);
                break;

            case "RECONNECT":
                handler.onReconnect();
                handler.onSetup();
                break;

            case "OPEN":
                handler.onOpen();
                if (this.handler.getPassReconnect()) {
                    this.sendSignaling("", new String[] {this.handler.getShouldReconnect()? "SHOULD_RECONNECT" : "NO_RECONNECT"}, true);
                }
                this.sendSignaling("", new String[] {"OPEN"}, true);
                break;

            case "CLOSE":
                handler.onClose();
                this.sendSignaling("", new String[] {"CONFIRM_CLOSE"}, true);
                break;

            case "CONFIRM_CLOSE":
                if (this.closingTimer == null) this.closingTimer.cancel();
                this.connection.close();
                break;

            case 
        }
    }

    public void sendSignaling(String message, String[] flags, boolean toConnectionManager) {
        StringBuilder fullMessage = new StringBuilder();

        if (!toConnectionManager) {
            fullMessage.append("MESSAGE | ");
        }

        for (String flag : flags) {
            fullMessage.append(flag).append(" | ");
        }

        fullMessage.append(message);

        ByteBuffer textBuffer = ByteBuffer.wrap(fullMessage.toString().getBytes(StandardCharsets.UTF_8));
        RTCDataChannelBuffer textChannelBuffer = new RTCDataChannelBuffer(textBuffer, false);

        try {
            signalingChannel.send(textChannelBuffer);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CompletableFuture<RTCSessionDescription> getOffer() {
        CompletableFuture<RTCSessionDescription> future = new CompletableFuture<>();
        connection.createOffer(WebrtcServerApplication.options, new CreateSessionDescriptionObserver() {
            public void onSuccess(RTCSessionDescription desc) {
                connection.setLocalDescription(desc, new SetSessionDescriptionObserver() {
                    @Override
                    public void onSuccess() {
                        future.complete(desc);
                    }

                    @Override
                    public void onFailure(String error) {
                        System.err.println("Failed to set local description: " + error);
                        future.completeExceptionally(new RuntimeException(error));
                    }
                });
            }

            public void onFailure(String error) {
                System.err.println("Failed to create offer: " + error);
            }
        });
        return future;
    }

    public void receiveOffer(RTCSessionDescription remoteOffer) {
        connection.setRemoteDescription(remoteOffer, new SetSessionDescriptionObserver() {
            @Override
            public void onSuccess() {}

            @Override
            public void onFailure(String error) {
                System.err.println("Failed to set remote description for connection " + id + ": " + error);
            }
        });
    }

    public void closeConnection() {
        this.closingTimer = new Timer();
        CloseBackup isClosing = new CloseBackup();
        isClosing.setConnection(this.connection);
        this.closingTimer.schedule(isClosing, CLOSING_TIMER_LENGTH);
    }
}

class CloseBackup extends TimerTask {
    RTCPeerConnection connection;

    @Override
    public void run() {
        System.out.println("Client did not confirm close. Closing anyway.");
        connection.close();
    }

    public void setConnection(RTCPeerConnection connection) {
        this.connection = connection;
    }
}