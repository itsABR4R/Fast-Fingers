import { useState, useEffect, useCallback } from "react";

const useTypingEngine = (textToType, isCodeMode = false) => {
    const [userInput, setUserInput] = useState("");
    const [startTime, setStartTime] = useState(null);
    const [wpm, setWpm] = useState(0);
    const [rawWpm, setRawWpm] = useState(0);
    const [phase, setPhase] = useState("start"); // start, typing, finished
    const [wpmHistory, setWpmHistory] = useState([]); // Track WPM over time for chart

    // Parse text into characters with state tracking
    const characters = textToType.split("");

    // Parse text into words (for both practice and code mode now)
    const words = textToType.trim().split(/\s+/).filter(w => w.length > 0);

    // Track current position - this should match userInput.length
    const [charStates, setCharStates] = useState([]); // 'waiting', 'correct', 'incorrect'
    const [currentLineTop, setCurrentLineTop] = useState(0);

    // Stack for tracking auto-indent jumps (AOOP Req 2)
    const [indentJumpStack, setIndentJumpStack] = useState([]);

    // Character statistics for result screen
    const [charStats, setCharStats] = useState({
        correct: 0,
        incorrect: 0,
        extra: 0,
        missed: 0
    });

    // Current character index is simply the length of user input
    const currentCharIndex = userInput.length;

    // Calculate accuracy
    const accuracy = userInput.length > 0
        ? (charStats.correct / userInput.length) * 100
        : 100;

    // Initialize character states
    useEffect(() => {
        setCharStates(new Array(characters.length).fill('waiting'));
    }, [textToType]);

    const resetEngine = () => {
        setUserInput("");
        setStartTime(null);
        setWpm(0);
        setRawWpm(0);
        setPhase("start");
        setCharStates(new Array(characters.length).fill('waiting'));
        setCurrentLineTop(0);
        setIndentJumpStack([]);
        setWpmHistory([]);
        setCharStats({ correct: 0, incorrect: 0, extra: 0, missed: 0 });
    };

    // Rest of the existing code...
    // (keeping all the existing functions)

    return {
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
        currentLineTop,
        setCurrentLineTop,
        indentJumpStack,
        wpmHistory,
        charStats
    };
};

export default useTypingEngine;
