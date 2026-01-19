import React, { useEffect, useRef, useState } from "react";
import { useSearchParams, useNavigate } from "react-router-dom";
import useTypingEngine from "../../hooks/useTypingEngine";
import useGameConfig from "../../hooks/useGameConfig";
import ResultScreen from "./ResultScreen";
import ModeToolbar from "./ModeToolbar";
import api from "../../services/api";
import { clsx } from "clsx";

const TypingArea = () => {
  const [textToType, setTextToType] = useState("Loading...");
  const [isFocused, setIsFocused] = useState(false);
  const [searchParams] = useSearchParams();
  const [cursorPosition, setCursorPosition] = useState({ left: 0, top: 0 });

  const navigate = useNavigate();
  const isCodeMode = searchParams.get("mode") === "code";

  // Game configuration (time/words mode)
  const {
    testType,
    testValue,
    setTimeMode,
    setInfiniteMode,
    setWordsMode,
    resetConfig
  } = useGameConfig();

  const {
    userInput,
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
    wpmHistory,
    charStats,
    timeRemaining
  } = useTypingEngine(textToType, isCodeMode, testType, testValue);
  const focusedInputRef = useRef(null);
  const charRefs = useRef([]);
  const containerRef = useRef(null);

  useEffect(() => {
    fetchNewSnippet();
  }, [searchParams]);

  // Update cursor position when currentCharIndex changes
  useEffect(() => {
    if (charRefs.current[currentCharIndex] && containerRef.current) {
      const charElement = charRefs.current[currentCharIndex];
      const containerRect = containerRef.current.getBoundingClientRect();
      const charRect = charElement.getBoundingClientRect();

      // Calculate relative position
      const relativeLeft = charRect.left - containerRect.left;
      const relativeTop = charRect.top - containerRect.top;

      setCursorPosition({
        left: relativeLeft,
        top: relativeTop
      });
    }
  }, [currentCharIndex, textToType]);

  const fetchNewSnippet = async () => {
    try {
      if (isCodeMode) {
        const response = await api.get("/game/text?lang=java");
        setTextToType(response.data);
      } else {
        // Use testValue for word count in words mode, default to 50 for time mode
        const wordCount = testType === 'words' ? testValue : 50;
        const response = await api.get(`/game/text?lang=english&count=${wordCount}`);
        setTextToType(response.data);
      }
      resetEngine();
      focusInput();
    } catch (error) {
      if (isCodeMode) {
        setTextToType(`public class FastFingers {\n  public static void main(String[] args) {\n    System.out.println("Server Offline");\n  }\n}`);
      } else {
        setTextToType("the quick brown fox jumps over the lazy dog and runs through the forest with great speed and agility");
      }
      resetEngine();
    }
  };

  // Fetch new snippet when mode or value changes
  useEffect(() => {
    fetchNewSnippet();
  }, [testType, testValue]);

  const focusInput = () => {
    setIsFocused(true);
    focusedInputRef.current?.focus();
  };

  const handleBlur = () => { setIsFocused(false); };

  const handleGlobalKeys = (e) => {
    // Tab to restart
    if (e.key === 'Tab') {
      e.preventDefault();
      resetEngine();
      resetConfig();
      fetchNewSnippet();
    }
    if (!isFocused) focusInput();
  };

  // Get CSS class for character state
  const getCharClass = (state) => {
    switch (state) {
      case 'correct':
        return 'text-gray-100 transition-colors duration-150 ease-in';
      case 'incorrect':
        return 'text-red-500 transition-colors duration-150 ease-in animate-shake';
      default:
        return 'text-gray-500';
    }
  };

  // Show ResultScreen when test is finished
  if (phase === 'finished') {
    return (
      <ResultScreen
        wpm={wpm}
        rawWpm={rawWpm}
        accuracy={accuracy}
        testType={`${testType} ${testValue}`}
        language={isCodeMode ? "java" : "english"}
        characters={charStats}
        wpmHistory={wpmHistory}
        onNextTest={fetchNewSnippet}
        onRestart={resetEngine}
        onSettings={() => navigate('/')}
      />
    );
  }

  return (
    <div
      className="relative w-full flex flex-col items-center justify-center outline-none min-h-[50vh]"
      onClick={focusInput}
      onKeyDown={handleGlobalKeys}
      tabIndex={0}
    >
      <input
        ref={focusedInputRef}
        type="text"
        className="absolute opacity-0 top-0"
        onKeyDown={handleKeyDown}
        onBlur={handleBlur}
        autoFocus
      />

      {/* MODE TOOLBAR - Above overlay */}
      <div className="relative z-30">
        <ModeToolbar
          testType={testType}
          testValue={testValue}
          onTimeSelect={setTimeMode}
          onInfiniteSelect={setInfiniteMode}
          onWordsSelect={setWordsMode}
          disabled={phase === 'typing'}
          isCodeMode={isCodeMode}
        />
      </div>

      {!isFocused && phase !== 'finished' && (
        <div className="absolute inset-0 z-20 flex items-center justify-center cursor-pointer backdrop-blur-[2px]" style={{ top: '80px' }}>
          <div className="flex items-center gap-3 text-gray-400 bg-[#323437]/90 px-6 py-3 rounded-lg shadow-2xl border border-gray-700">
            <span className="animate-pulse text-yellow-400">🖱</span>
            <span>Click to Focus</span>
          </div>
        </div>
      )}

      {/* STATS HEADER */}
      <div className={clsx("flex w-full max-w-4xl justify-between items-end mb-8 transition-opacity duration-300", { "opacity-0": !isFocused && phase !== 'finished' })}>
        <div className="text-3xl font-bold text-yellow-400">
          <div className="text-xs text-gray-500 font-normal uppercase tracking-widest mb-1">WPM</div>
          {wpm}
        </div>

        {/* Timer Display (Time Mode Only) */}
        {testType === 'time' && phase !== 'start' && (
          <div className="text-2xl font-bold text-gray-300">
            <div className="text-xs text-gray-500 font-normal uppercase tracking-widest mb-1">TIME</div>
            {testValue === 0 ? '∞' : `${timeRemaining}s`}
          </div>
        )}

        {isCodeMode && (
          <div className="px-3 py-1 bg-gray-800 rounded border border-gray-700 text-xs text-green-400 font-mono tracking-widest">
            JAVA
          </div>
        )}
      </div>

      {/* MAIN TEXT AREA - Full Visible Block */}
      <div
        ref={containerRef}
        className="relative w-full max-w-4xl"
      >
        <div
          className={clsx(
            "relative transition-all duration-300",
            { "blur-[4px] opacity-40": !isFocused && phase !== 'finished' }
          )}
        >
          {/* Smooth Cursor with Diagonal Glide */}
          {phase !== 'finished' && (
            <div
              className="absolute z-10 bg-yellow-400 animate-pulse"
              style={{
                left: `${cursorPosition.left}px`,
                top: `${cursorPosition.top}px`,
                width: isCodeMode ? '10px' : '2px',
                height: '2rem',
                // Smooth diagonal transition when jumping from line end to indented start
                transition: 'left 0.15s cubic-bezier(0.4, 0, 0.2, 1), top 0.15s cubic-bezier(0.4, 0, 0.2, 1)',
                opacity: isCodeMode ? 0.5 : 1
              }}
            />
          )}

          {/* Full Text Block with pre-wrap */}
          <div
            className={clsx(
              "text-2xl font-mono text-left",
              isCodeMode ? "whitespace-pre-wrap" : "flex flex-wrap"
            )}
            style={{
              lineHeight: '2rem',
              width: '100%'
            }}
          >
            {isCodeMode ? (
              // Code mode: use pre-wrap to preserve newlines and indentation
              characters.map((char, index) => (
                <span
                  key={index}
                  ref={el => charRefs.current[index] = el}
                  className={clsx("relative", getCharClass(charStates[index]))}
                >
                  {char}
                </span>
              ))
            ) : (
              // Practice mode: word-based with wrapping
              (() => {
                let charIndex = 0;
                return words.map((word, wordIdx) => (
                  <div
                    key={wordIdx}
                    className="flex"
                    style={{ marginRight: '0.6rem', marginBottom: '0.5rem' }}
                  >
                    {word.split("").map((char, charIdxInWord) => {
                      const currentIndex = charIndex++;
                      return (
                        <span
                          key={charIdxInWord}
                          ref={el => charRefs.current[currentIndex] = el}
                          className={clsx("relative", getCharClass(charStates[currentIndex]))}
                        >
                          {char}
                        </span>
                      );
                    })}
                    {/* Add space character after each word except the last */}
                    {wordIdx < words.length - 1 && (() => {
                      const spaceIndex = charIndex++;
                      return (
                        <span
                          key={`space-${wordIdx}`}
                          ref={el => charRefs.current[spaceIndex] = el}
                          className={clsx("relative", getCharClass(charStates[spaceIndex]))}
                        >
                          {' '}
                        </span>
                      );
                    })()}
                  </div>
                ));
              })()
            )}
          </div>
        </div>
      </div>

      {/* RESTART HINT */}
      <div className={clsx("flex items-center gap-4 mt-12 text-sm text-gray-500 font-mono transition-opacity duration-300", { "opacity-0": !isFocused })}>
        <div className="flex items-center gap-2">
          <span className="bg-gray-800 px-2 py-1 rounded text-gray-400 border border-gray-700">Tab</span>
          <span>- restart</span>
        </div>
        {isCodeMode && (
          <div className="flex items-center gap-2">
            <span className="bg-gray-800 px-2 py-1 rounded text-gray-400 border border-gray-700">Enter</span>
            <span>- auto-jump indent</span>
          </div>
        )}
      </div>

      {/* MODE BUTTONS - Same as HomePage */}
      <div className="w-full max-w-4xl px-8 mt-8">
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          {/* Practice Mode */}
          <div
            onClick={() => navigate('/')}
            className="relative group cursor-pointer bg-[#2c2e31] hover:bg-[#323437] border border-transparent hover:border-yellow-500/50 rounded-xl p-4 transition-all duration-300 ease-out flex flex-col items-center justify-center gap-2 w-full h-24 overflow-hidden"
          >
            <div className="text-2xl text-yellow-400 group-hover:scale-110 transition duration-300">⌨</div>
            <div className="text-xs font-bold text-gray-400 group-hover:text-gray-200 uppercase tracking-widest">PRACTICE</div>
          </div>

          {/* Code Mode */}
          <div
            onClick={() => navigate('/practice?mode=code')}
            className="relative group cursor-pointer bg-[#2c2e31] hover:bg-[#323437] border border-transparent hover:border-green-500/50 rounded-xl p-4 transition-all duration-300 ease-out flex flex-col items-center justify-center gap-2 w-full h-24 overflow-hidden"
          >
            <div className="text-2xl text-green-400 group-hover:scale-110 transition duration-300">&lt;/&gt;</div>
            <div className="text-xs font-bold text-gray-400 group-hover:text-gray-200 uppercase tracking-widest">CODE MODE</div>
          </div>

          {/* Multiplayer */}
          <div
            onClick={() => {
              const isLoggedIn = !!localStorage.getItem("token");
              navigate(isLoggedIn ? '/room' : '/login');
            }}
            className="relative group cursor-pointer bg-[#2c2e31] hover:bg-[#323437] border border-transparent hover:border-blue-500/50 rounded-xl p-4 transition-all duration-300 ease-out flex flex-col items-center justify-center gap-2 w-full h-24 overflow-hidden"
          >
            <div className="text-2xl text-blue-400 group-hover:scale-110 transition duration-300">👥</div>
            <div className="text-xs font-bold text-gray-400 group-hover:text-gray-200 uppercase tracking-widest">MULTIPLAYER</div>
            {!localStorage.getItem("token") && (
              <div className="absolute inset-0 bg-black/60 backdrop-blur-[1px] flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity duration-200">
                <span className="text-xs font-mono text-gray-300 bg-black/80 px-2 py-1 rounded">Login Required</span>
              </div>
            )}
          </div>

          {/* Battle Royale */}
          <div
            onClick={() => {
              const isLoggedIn = !!localStorage.getItem("token");
              navigate(isLoggedIn ? '/battle-royale' : '/login');
            }}
            className="relative group cursor-pointer bg-[#2c2e31] hover:bg-[#323437] border border-transparent hover:border-red-500/50 rounded-xl p-4 transition-all duration-300 ease-out flex flex-col items-center justify-center gap-2 w-full h-24 overflow-hidden"
          >
            <div className="text-2xl text-red-400 group-hover:scale-110 transition duration-300">⚔</div>
            <div className="text-xs font-bold text-gray-400 group-hover:text-gray-200 uppercase tracking-widest">BATTLE ROYALE</div>
            {!localStorage.getItem("token") && (
              <div className="absolute inset-0 bg-black/60 backdrop-blur-[1px] flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity duration-200">
                <span className="text-xs font-mono text-gray-300 bg-black/80 px-2 py-1 rounded">Login Required</span>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* CSS for shake animation */}
      <style jsx>{`
        @keyframes shake {
          0%, 100% { transform: translateX(0); }
          25% { transform: translateX(-2px); }
          75% { transform: translateX(2px); }
        }
        .animate-shake {
          animation: shake 0.3s ease-in-out;
        }
      `}</style>
    </div>
  );
};

export default TypingArea;