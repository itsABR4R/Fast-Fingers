import { useState, useEffect } from 'react';
import api from '../services/api';

const useGhostReplay = (replayId) => {
  const [ghostProgress, setGhostProgress] = useState(0);
  const [isPlaying, setIsPlaying] = useState(false);
  const [replayData, setReplayData] = useState([]);

  // 1. Load Replay Data
  useEffect(() => {
    if (replayId) {
      api.get(`/replays/${replayId}`).then(res => {
        setReplayData(res.data); // Expecting [{time: 100, char: 'a'}, ...]
      });
    }
  }, [replayId]);

  // 2. Playback Logic
  useEffect(() => {
    if (!isPlaying || replayData.length === 0) return;

    let startTime = Date.now();
    let currentIndex = 0;

    const interval = setInterval(() => {
      const elapsed = Date.now() - startTime;
      
      // Fast forward to current time
      while (currentIndex < replayData.length && replayData[currentIndex].time <= elapsed) {
        currentIndex++;
      }
      
      setGhostProgress(currentIndex);

      if (currentIndex >= replayData.length) {
        setIsPlaying(false);
        clearInterval(interval);
      }
    }, 50); // Check every 50ms

    return () => clearInterval(interval);
  }, [isPlaying, replayData]);

  const startGhost = () => {
    setGhostProgress(0);
    setIsPlaying(true);
  };

  return { ghostProgress, startGhost };
};

export default useGhostReplay;