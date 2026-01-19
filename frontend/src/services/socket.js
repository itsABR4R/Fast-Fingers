import { Client } from '@stomp/stompjs';

// The URL must match your Spring Boot Endpoint (defined in WebSocketConfig.java)
const SOCKET_URL = import.meta.env.VITE_WS_URL || 'ws://localhost:8080/fastfingers-ws';

export const createStompClient = (onConnect, onError) => {
  const client = new Client({
    brokerURL: SOCKET_URL,
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
    onConnect: () => {
      console.log("Connected to WebSocket");
      onConnect();
    },
    onStompError: (frame) => {
      console.error('Broker reported error: ' + frame.headers['message']);
      console.error('Additional details: ' + frame.body);
      if (onError) onError(frame);
    },
  });

  return client;
};