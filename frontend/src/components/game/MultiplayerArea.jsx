import React, { useEffect, useMemo, useRef, useState } from "react";
import { clsx } from "clsx";
import useTypingEngine from "../../hooks/useTypingEngine";
import GhostCaret from "./GhostCaret";

const MultiplayerArea = ({
  username,
  roomId,
  players,
  startText,
  winner,
  sendProgress,
  sendFinish,
}) => {
  const [isFocused, setIsFocused] = useState(false);
  const [textToType, setTextToType] = useState("Waiting for room...\n(need 2 players to start)");

  const focusedInputRef = useRef(null);
  const lastSentRef = useRef(0);
  const hasSentFinishRef = useRef(false);

  // Once server sends START text, lock it in.
  useEffect(() => {
    if (startText) {
      setTextToType(startText);
      hasSentFinishRef.current = false;
    }
  }, [startText]);

  const wordsCount = useMemo(() => {
    return textToType.trim().split(/\s+/).filter(Boolean).length || 0;
  }, [textToType]);

  const {
    wpm,
    rawWpm,
    accuracy,
    phase,
    handleKeyDown,
    resetEngine,
    words,
    characters,
    currentCharIndex,
    charStates,
    charStats,
  } = useTypingEngine(textToType, false, "words", wordsCount);

  // Reset typing engine when the shared text changes.
  useEffect(() => {
    resetEngine();
    focusInput();
  }, [textToType]);

  const focusInput = () => {
    setIsFocused(true);
    focusedInputRef.current?.focus();
  };

  const handleBlur = () => setIsFocused(false);

  const totalChars = characters.length || 1;
  const progressPercentage = Math.min(100, Math.floor((currentCharIndex / totalChars) * 100));

  // Send progress updates (throttled).
  useEffect(() => {
    if (!startText) return; // don't spam before START

    const now = Date.now();
    if (now - lastSentRef.current > 150) {
      lastSentRef.current = now;
      sendProgress(wpm, progressPercentage);
    }

    // Ensure 100% is sent once when finished.
    if (phase === "finished" && !hasSentFinishRef.current) {
      hasSentFinishRef.current = true;
      sendProgress(wpm, 100);
      sendFinish({
        wpm,
        accuracy,
        wordsTyped: wordsCount,
        duration: 0,
      });
    }
  }, [wpm, progressPercentage, phase, startText]);

  // Save score to backend when finished (multiplayer)
  useEffect(() => {
    if (phase === "finished" && !hasSentFinishRef.current && startText) {
      const saveScore = async () => {
        try {
          // Get current user from localStorage
          const userStr = localStorage.getItem('user');
          let currentUsername = null;
          let userId = null;

          if (userStr) {
            try {
              const user = JSON.parse(userStr);
              if (user && !user.isGuest) {
                currentUsername = user.username;
                userId = user.id;
              }
            } catch (e) {
              console.error("Error parsing user data:", e);
            }
          }

          // Only save if user is logged in
          if (currentUsername && userId) {
            const isWinner = winner === currentUsername;

            const scoreData = {
              username: currentUsername,
              userId: userId,
              mode: "MULTIPLAYER",
              wpm: wpm,
              accuracy: accuracy,
              wordsTyped: wordsCount,
              duration: 0,
              isWin: isWinner
            };

            await fetch('http://localhost:8080/api/scores', {
              method: 'POST',
              headers: {
                'Content-Type': 'application/json',
              },
              body: JSON.stringify(scoreData)
            });

            console.log(`Multiplayer score saved for ${currentUsername}`);
          }
        } catch (error) {
          console.error("Failed to save multiplayer score:", error);
        }
      };

      saveScore();
    }
  }, [phase, wpm, accuracy, wordsCount, winner, username, startText]);

  const opponents = useMemo(() => {
    return (players || []).filter((p) => p.username !== username);
  }, [players, username]);

  // Cursor UI (simple)
  const charRefs = useRef([]);
  const containerRef = useRef(null);
  const [cursorPosition, setCursorPosition] = useState({ left: 0, top: 0 });

  useEffect(() => {
    if (!charRefs.current[currentCharIndex] || !containerRef.current) return;
    const charEl = charRefs.current[currentCharIndex];
    const containerRect = containerRef.current.getBoundingClientRect();
    const charRect = charEl.getBoundingClientRect();
    setCursorPosition({
      left: charRect.left - containerRect.left,
      top: charRect.top - containerRect.top,
    });
  }, [currentCharIndex, textToType]);

  const getCharClass = (state) => {
    switch (state) {
      case "correct":
        return "text-gray-100";
      case "incorrect":
        return "text-red-500";
      default:
        return "text-gray-500";
    }
  };

  const showOverlay = !startText || winner;

  return (
    <div
      className="relative w-full flex flex-col items-center justify-center outline-none min-h-[50vh]"
      onClick={focusInput}
      tabIndex={0}
    >
      <input
        ref={focusedInputRef}
        type="text"
        className="absolute opacity-0 top-0"
        onKeyDown={handleKeyDown}
        onBlur={handleBlur}
        autoFocus
        disabled={!startText || !!winner}
      />

      {/* STATS HEADER */}
      <div className={clsx("flex w-full max-w-4xl justify-between items-end mb-6", { "opacity-40": !isFocused })}>
        <div className="text-3xl font-bold text-yellow-400">
          <div className="text-xs text-gray-500 font-normal uppercase tracking-widest mb-1">WPM</div>
          {wpm}
        </div>
        <div className="text-right">
          <div className="text-xs text-gray-500 font-normal uppercase tracking-widest mb-1">ROOM</div>
          <div className="text-gray-300 font-mono text-sm">{roomId}</div>
        </div>
      </div>

      {/* TEXT AREA */}
      <div ref={containerRef} className="relative w-full max-w-4xl">
        <div className={clsx("relative transition-all duration-300", { "blur-[4px] opacity-40": !isFocused && !winner })}>
          {/* My caret */}
          {startText && !winner && (
            <div
              className="absolute z-10 bg-yellow-400 animate-pulse"
              style={{
                left: `${cursorPosition.left}px`,
                top: `${cursorPosition.top}px`,
                width: "2px",
                height: "2rem",
                transition:
                  "left 0.15s cubic-bezier(0.4, 0, 0.2, 1), top 0.15s cubic-bezier(0.4, 0, 0.2, 1)",
              }}
            />
          )}

          {/* Opponent ghost carets */}
          {startText && opponents.map((op) => {
            const idx = Math.floor(((op.progress || 0) / 100) * totalChars);
            return (
              <GhostCaret
                key={op.username}
                text={textToType}
                progress={idx}
                name={op.username}
              />
            );
          })}

          {/* Render text (practice style) */}
          <div
            className="text-2xl font-mono text-left flex flex-wrap"
            style={{ lineHeight: "2rem", width: "100%" }}
          >
            {(() => {
              let charIndex = 0;
              return words.map((word, wordIdx) => (
                <div
                  key={wordIdx}
                  className="flex"
                  style={{ marginRight: "0.6rem", marginBottom: "0.5rem" }}
                >
                  {word.split("").map((char, charIdxInWord) => {
                    const currentIndex = charIndex++;
                    return (
                      <span
                        key={charIdxInWord}
                        ref={(el) => (charRefs.current[currentIndex] = el)}
                        className={clsx("relative", getCharClass(charStates[currentIndex]))}
                      >
                        {char}
                      </span>
                    );
                  })}
                  {wordIdx < words.length - 1 && (() => {
                    const spaceIndex = charIndex++;
                    return (
                      <span
                        key={`space-${wordIdx}`}
                        ref={(el) => (charRefs.current[spaceIndex] = el)}
                        className={clsx("relative", getCharClass(charStates[spaceIndex]))}
                      >
                        {" "}
                      </span>
                    );
                  })()}
                </div>
              ));
            })()}
          </div>
        </div>
      </div>

      {/* Waiting / Finish overlay */}
      {showOverlay && (
        <div className="absolute inset-0 z-20 flex items-center justify-center backdrop-blur-[2px]">
          <div className="bg-[#323437]/95 border border-gray-700 rounded-xl p-6 text-center max-w-md">
            {!startText && (
              <>
                <div className="text-gray-200 font-bold mb-2">Waiting for players…</div>
                <div className="text-gray-400 text-sm font-mono">
                  Room starts when {2} players join.
                </div>
                <div className="mt-4 text-xs text-gray-500">
                  Tip: open another browser tab to simulate a second player.
                </div>
              </>
            )}

            {winner && (
              <>
                <div className="text-gray-200 font-bold mb-2">Race finished</div>
                <div className="text-yellow-400 text-xl font-black mb-3">🏆 {winner}</div>
                <div className="text-gray-400 text-sm font-mono">
                  Your WPM: {wpm} | Accuracy: {accuracy.toFixed(1)}%
                </div>
                <button
                  className="mt-5 px-4 py-2 rounded-lg bg-gray-800 hover:bg-gray-700 text-gray-200 text-sm"
                  onClick={() => {
                    resetEngine();
                    hasSentFinishRef.current = false;
                  }}
                >
                  Practice again (same room)
                </button>
              </>
            )}
          </div>
        </div>
      )}

      {/* Footer */}
      <div className="mt-8 text-xs text-gray-500 font-mono">
        Progress: {progressPercentage}% | Correct: {charStats.correct} | Incorrect: {charStats.incorrect}
      </div>
    </div>
  );
};

export default MultiplayerArea;
