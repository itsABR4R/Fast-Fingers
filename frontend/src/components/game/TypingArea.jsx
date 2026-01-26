import React, { useState, useEffect, useRef } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import authService from '../../services/authService';
import useTypingEngine from "../../hooks/useTypingEngine";
import useGameConfig from "../../hooks/useGameConfig";
import ResultScreen from "./ResultScreen";
import ModeToolbar from "./ModeToolbar";
import api from "../../services/api";
import { clsx } from "clsx";

// Keep it simple: store only the last completed run per mode.
const ghostStorageKey = (isCodeMode) => (isCodeMode ? "ff_ghost_last_code" : "ff_ghost_last_practice");

// Binary search: return the last timeline point with t <= elapsedMs.
const ghostIndexAt = (timeline, elapsedMs) => {
  if (!timeline || timeline.length === 0) return 0;
  if (elapsedMs <= timeline[0].t) return timeline[0].i;
  const last = timeline[timeline.length - 1];
  if (elapsedMs >= last.t) return last.i;

  let lo = 0;
  let hi = timeline.length - 1;
  while (lo <= hi) {
    const mid = (lo + hi) >> 1;
    if (timeline[mid].t <= elapsedMs) lo = mid + 1;
    else hi = mid - 1;
  }
  return timeline[Math.max(0, hi)].i;
};

const TypingArea = () => {
  const [textToType, setTextToType] = useState("Loading...");
  const [isFocused, setIsFocused] = useState(false);
  const [searchParams] = useSearchParams();
  const [cursorPosition, setCursorPosition] = useState({ left: 0, top: 0 });

  // Ghost caret (replay of your previous run, shown on restart)
  const [ghostActive, setGhostActive] = useState(false);
  const [ghostIndex, setGhostIndex] = useState(0);
  const [ghostCursorPosition, setGhostCursorPosition] = useState({ left: 0, top: 0, visible: false });

  const lastRunRef = useRef(null);           // last completed run (this session / localStorage)
  const recordStartRef = useRef(null);       // performance.now() at start of typing
  const recordTimelineRef = useRef([]);      // [{t,i}] - ms since start, caret index

  const ghostRunRef = useRef(null);          // run data currently being replayed
  const ghostStartRef = useRef(null);        // performance.now() when replay starts
  const ghostRafRef = useRef(null);
  const ghostLastIdxRef = useRef(-1);

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

  // Helpers to manage ghost state (no popups, no ceremony)
  const clearGhost = () => {
    setGhostActive(false);
    setGhostIndex(0);
    setGhostCursorPosition({ left: 0, top: 0, visible: false });
    ghostRunRef.current = null;
    ghostStartRef.current = null;
    ghostLastIdxRef.current = -1;
    if (ghostRafRef.current) {
      cancelAnimationFrame(ghostRafRef.current);
      ghostRafRef.current = null;
    }
  };

  const armGhostFromLastRun = () => {
    const run = lastRunRef.current;
    if (!run) return;
    if (run.isCodeMode !== isCodeMode) return;
    if (run.text !== textToType) return;
    if (!Array.isArray(run.timeline) || run.timeline.length === 0) return;

    ghostRunRef.current = run;
    setGhostIndex(0);
    setGhostActive(true);
  };

  // Load last run (if any). We don't autoplay it, we just keep it around for restart.
  useEffect(() => {
    try {
      const raw = localStorage.getItem(ghostStorageKey(isCodeMode));
      if (raw) lastRunRef.current = JSON.parse(raw);
    } catch {
      // ignore
    }
  }, [isCodeMode]);

  useEffect(() => {
    fetchNewSnippet();
  }, [searchParams]);

  // Track and store your last run timeline (for ghost replay)
  useEffect(() => {
    if (phase === 'start') {
      recordStartRef.current = null;
      recordTimelineRef.current = [];
      // Don't clearGhost here: restart should keep ghostActive armed.
      ghostStartRef.current = null;
      ghostLastIdxRef.current = -1;
      if (ghostRafRef.current) {
        cancelAnimationFrame(ghostRafRef.current);
        ghostRafRef.current = null;
      }
      setGhostIndex(0);
      return;
    }

    if (phase === 'typing' && recordStartRef.current == null) {
      recordStartRef.current = performance.now();
      recordTimelineRef.current = [{ t: 0, i: 0 }];
    }

    if (phase === 'finished') {
      // Save last completed run for this mode so restart can replay it.
      const timeline = recordTimelineRef.current;
      if (timeline && timeline.length > 0) {
        const runData = {
          text: textToType,
          isCodeMode,
          testType,
          testValue,
          timeline,
          finishedAt: Date.now()
        };

        lastRunRef.current = runData;
        try {
          localStorage.setItem(ghostStorageKey(isCodeMode), JSON.stringify(runData));
        } catch {
          // ignore
        }
      }
    }
  }, [phase, isCodeMode, testType, testValue, textToType]);

  // Append timeline points as the caret moves (includes backspace)
  useEffect(() => {
    if (phase !== 'typing') return;
    if (recordStartRef.current == null) return;

    const t = Math.max(0, performance.now() - recordStartRef.current);
    const timeline = recordTimelineRef.current;
    const last = timeline[timeline.length - 1];

    if (!last || last.i !== currentCharIndex) {
      timeline.push({ t: Math.round(t), i: currentCharIndex });
    }
  }, [currentCharIndex, phase]);

  // Ghost replay: start ticking when the new run actually starts typing
  useEffect(() => {
    if (!ghostActive) return;
    if (!ghostRunRef.current) return;
    if (phase !== 'typing') return;

    const timeline = ghostRunRef.current.timeline || [];
    if (timeline.length === 0) return;

    ghostStartRef.current = performance.now();
    ghostLastIdxRef.current = -1;

    const lastT = timeline[timeline.length - 1].t;

    const tick = () => {
      const elapsed = performance.now() - ghostStartRef.current;
      const idx = ghostIndexAt(timeline, elapsed);

      if (idx !== ghostLastIdxRef.current) {
        ghostLastIdxRef.current = idx;
        setGhostIndex(idx);
      }

      if (elapsed <= lastT && phase === 'typing') {
        ghostRafRef.current = requestAnimationFrame(tick);
      }
    };

    ghostRafRef.current = requestAnimationFrame(tick);

    return () => {
      if (ghostRafRef.current) {
        cancelAnimationFrame(ghostRafRef.current);
        ghostRafRef.current = null;
      }
    };
  }, [ghostActive, phase, textToType]);

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

  // Update ghost cursor position when ghostIndex changes
  useEffect(() => {
    if (!ghostActive || !containerRef.current) {
      setGhostCursorPosition({ left: 0, top: 0, visible: false });
      return;
    }

    const containerRect = containerRef.current.getBoundingClientRect();
    const el = charRefs.current[ghostIndex];

    // If index is past the end, park it after the last character
    if (!el) {
      const lastEl = charRefs.current[charRefs.current.length - 1];
      if (!lastEl) {
        setGhostCursorPosition({ left: 0, top: 0, visible: false });
        return;
      }
      const lastRect = lastEl.getBoundingClientRect();
      setGhostCursorPosition({
        left: lastRect.right - containerRect.left,
        top: lastRect.top - containerRect.top,
        visible: true
      });
      return;
    }

    const rect = el.getBoundingClientRect();
    setGhostCursorPosition({
      left: rect.left - containerRect.left,
      top: rect.top - containerRect.top,
      visible: true
    });
  }, [ghostIndex, ghostActive, textToType]);

  const fetchNewSnippet = async () => {
    try {
      // New text = new run. Don't drag an old ghost across a different test.
      clearGhost();
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
      clearGhost();
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

  function focusInput() {
    setIsFocused(true);
    focusedInputRef.current?.focus();
  }

  function handleBlur() {
    setIsFocused(false);
  }

  const handleGlobalKeys = (e) => {
    // Tab to restart
    if (e.key === 'Tab') {
      e.preventDefault();
      // Tab is "next test" behavior here; don't replay ghost across different text.
      clearGhost();
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

  // Result screen actions
  const handleRestartTest = () => {
    // Arm ghost replay from the just-finished run, then reset.
    armGhostFromLastRun();
    resetEngine();
    // React can be... slow to put focus back unless we nudge it.
    setTimeout(() => focusInput(), 0);
  };

  const handleNextTest = () => {
    clearGhost();
    fetchNewSnippet();
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
        onNextTest={handleNextTest}
        onRestart={handleRestartTest}
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
          {/* Ghost caret: replay of your previous run (appears after you finish and restart) */}
          {ghostActive && ghostCursorPosition.visible && phase !== 'finished' && (
            <div
              className="absolute z-5 pointer-events-none"
              style={{
                left: `${ghostCursorPosition.left}px`,
                top: `${ghostCursorPosition.top}px`,
                height: '2rem',
                transition: 'left 0.08s linear, top 0.08s linear',
                opacity: 0.6
              }}
            >
              <div className="h-8 border-l-2 border-blue-300" />
              <div className="absolute -top-6 -left-2 text-xs text-blue-200 bg-gray-800/80 px-1 rounded font-mono">
                ghost
              </div>
            </div>
          )}

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
      <div className="relative z-50 w-full max-w-4xl px-8 mt-8" style={{ pointerEvents: 'auto', position: 'relative' }}>
        <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
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
              const isLoggedIn = authService.isAuthenticated() && !authService.isGuest();
              navigate(isLoggedIn ? '/room' : '/login');
            }}
            className="relative group cursor-pointer bg-[#2c2e31] hover:bg-[#323437] border border-transparent hover:border-blue-500/50 rounded-xl p-4 transition-all duration-300 ease-out flex flex-col items-center justify-center gap-2 w-full h-24 overflow-hidden"
          >
            <div className="text-2xl text-blue-400 group-hover:scale-110 transition duration-300">👥</div>
            <div className="text-xs font-bold text-gray-400 group-hover:text-gray-200 uppercase tracking-widest">MULTIPLAYER</div>
            {!(authService.isAuthenticated() && !authService.isGuest()) && (
              <div className="absolute inset-0 bg-black/60 backdrop-blur-[1px] flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity duration-200">
                <span className="text-xs font-mono text-gray-300 bg-black/80 px-2 py-1 rounded">Login Required</span>
              </div>
            )}
          </div>


        </div>
      </div>

      {/* CSS for shake animation (plain <style>; "jsx" attribute triggers React warning) */}
      <style>{`
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