import { useEffect, useRef, useState } from 'react';
import { createStompClient } from '../services/socket';

const useGameSocket = (roomId, username) => {
  const [messages, setMessages] = useState([]);
  const [players, setPlayers] = useState([]); // List of { username, progress, status }
  const [isConnected, setIsConnected] = useState(false);
  const [startText, setStartText] = useState(null);
  const [winner, setWinner] = useState(null);
  
  const clientRef = useRef(null);

  useEffect(() => {
    if (!roomId || !username) return;

    // 1. Initialize Client
    const client = createStompClient(
      () => {
        setIsConnected(true);
        subscribeToRoom();
      },
      (err) => console.error("Socket Error", err)
    );

    clientRef.current = client;
    client.activate();

    // 2. Subscribe Logic
    const subscribeToRoom = () => {
      // Listen for Game Updates (Progress, Elimination)
      client.subscribe(`/topic/game/${roomId}`, (message) => {
        const data = JSON.parse(message.body);

        if (data.type === 'START') {
          setStartText(data.text);
          setWinner(null);
        }

        if (data.type === 'PLAYER_UPDATE') {
          // Backend sends: { type: 'PLAYER_UPDATE', players: [...] }
          if (Array.isArray(data.players)) {
            setPlayers(data.players);
          }
        }

        if (data.type === 'FINISH') {
          setWinner(data.winner);
        }
      });

      // Listen for Chat
      client.subscribe(`/topic/chat/${roomId}`, (message) => {
        const data = JSON.parse(message.body);
        setMessages(prev => [...prev, data]);
      });

      // Notify server I have joined
      client.publish({
        destination: `/app/join/${roomId}`,
        body: JSON.stringify({ username })
      });
    };

    // Cleanup on Unmount
    return () => {
      if (client.active) client.deactivate();
    };
  }, [roomId, username]);

  // Actions exposed to UI
  const sendProgress = (wpm, progressPercentage) => {
    if (clientRef.current?.connected) {
      clientRef.current.publish({
        destination: `/app/progress/${roomId}`,
        body: JSON.stringify({ username, wpm, progress: progressPercentage })
      });
    }
  };

  const sendFinish = ({ wpm, accuracy, wordsTyped, duration }) => {
    if (clientRef.current?.connected) {
      clientRef.current.publish({
        destination: `/app/finish/${roomId}`,
        body: JSON.stringify({ username, wpm, accuracy, wordsTyped, duration })
      });
    }
  };

  const sendChat = (text) => {
    if (clientRef.current?.connected) {
      clientRef.current.publish({
        destination: `/app/chat/${roomId}`,
        body: JSON.stringify({ sender: username, content: text })
      });
    }
  };

  return { isConnected, players, messages, startText, winner, sendProgress, sendFinish, sendChat };
};

export default useGameSocket;