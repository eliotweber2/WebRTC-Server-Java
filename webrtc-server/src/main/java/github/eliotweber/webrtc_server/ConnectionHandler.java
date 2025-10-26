package github.eliotweber.webrtc_server;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import dev.onvoid.webrtc.CreateSessionDescriptionObserver;
import dev.onvoid.webrtc.SetSessionDescriptionObserver;
import dev.onvoid.webrtc.PeerConnectionFactory;
import dev.onvoid.webrtc.RTCConfiguration;
import dev.onvoid.webrtc.RTCPeerConnection;
import dev.onvoid.webrtc.RTCSessionDescription;
import dev.onvoid.webrtc.RTCIceCandidate;
import dev.onvoid.webrtc.PeerConnectionObserver;
import dev.onvoid.webrtc.RTCDataChannelInit;
import dev.onvoid.webrtc.RTCPriorityType;

public class ConnectionHandler {
    public String id;
    private RTCPeerConnection connection;
    public List<RTCIceCandidate> iceCandidates = new ArrayList<>();

    private static final RTCDataChannelInit signalingChannelConfig = new RTCDataChannelInit();
    static {
        signalingChannelConfig.priority = RTCPriorityType.HIGH;
    }

    private static final RTCDataChannelInit dataChannelConfig = new RTCDataChannelInit();
    static {
        dataChannelConfig.ordered = false;
        dataChannelConfig.maxPacketLifeTime = 1000;
    }

    public ConnectionHandler(String id, PeerConnectionFactory factory, RTCConfiguration config) {
        this.id = id;
        this.setupConnection(factory, config);
    }

    private void setupConnection(PeerConnectionFactory factory, RTCConfiguration config) {
        this.connection = factory.createPeerConnection(config, new PeerConnectionObserver() {
            @Override
            public void onIceCandidate(RTCIceCandidate candidate) {
                iceCandidates.add(candidate);
            }
        });

        this.connection.createDataChannel("signaling", signalingChannelConfig);
        this.connection.createDataChannel("data", dataChannelConfig);
    }

    public CompletableFuture<RTCSessionDescription> getOffer() {
        CompletableFuture<RTCSessionDescription> future = new CompletableFuture<>();
        connection.createOffer(WebrtcServerApplication.options, new CreateSessionDescriptionObserver() {
            public void onSuccess(RTCSessionDescription desc) {
                connection.setLocalDescription(desc, new SetSessionDescriptionObserver() {
                    @Override
                    public void onSuccess() {
                        System.out.println("Local description set successfully");
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
        connection.setRemoteDescription(remoteOffer, null);
    }
}