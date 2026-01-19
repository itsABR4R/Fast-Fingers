import { useState, useEffect, useCallback } from "react";

const useTypingEngine = (textToType, isCodeMode = false, testType = 'time', testValue = 30) => {
    const [userInput, setUserInput] = useState("");
    const [startTime, setStartTime] = useState(null);
    const [wpm, setWpm] = useState(0);
    const [rawWpm, setRawWpm] = useState(0);
    const [phase, setPhase] = useState("start"); // start, typing, finished
    const [wpmHistory, setWpmHistory] = useState([]); // Track WPM over time for chart
    const [timeRemaining, setTimeRemaining] = useState(testType === 'time' ? testValue : 0);

    // Parse text into characters with state tracking
    const characters = textToType.split("");

    // Parse text into words (for both practice and code mode now)
    const words = textToType.trim().split(/\s+/).filter(w => w.length > 0);

    // Track current position - this should match userInput.length
    const [charStates, setCharStates] = useState([]); // 'waiting', 'correct', 'incorrect'
    const [currentLineTop, setCurrentLineTop] = useState(0);

    // Stack for tracking auto-indent jumps (AOOP Req 2)
    const [indentJumpStack, setIndentJumpStack] = useState([]);

    // Total keystrokes for raw WPM and accuracy calculation
    const [totalTyped, setTotalTyped] = useState(0);

    // Character statistics for result screen
    const [charStats, setCharStats] = useState({
        correct: 0,
        incorrect: 0,
        missed: 0
    });

    // Stack for tracking typed characters (AOOP Req 2) - for proper backspace handling
    const [typedCharStack, setTypedCharStack] = useState([]);

    // Current character index is simply the length of user input
    const currentCharIndex = userInput.length;

    // Track current word index for words mode completion
    const activeWordIndex = userInput.trim().split(/\s+/).filter(w => w.length > 0).length - 1;

    // Calculate accuracy: (correct chars / total chars typed) * 100
    // Ensure it never exceeds 100% and handles 0 division
    const accuracy = totalTyped > 0
        ? Math.min(100, (charStats.correct / totalTyped) * 100)
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
        setTotalTyped(0);
        setTypedCharStack([]);
        setCharStats({ correct: 0, incorrect: 0, missed: 0 });
        setTimeRemaining(testType === 'time' ? testValue : 0); // Reset timer for time mode
    };

    // Sync timeRemaining when testValue changes (for time mode)
    // testValue = 0 means infinite mode (no timer)
    useEffect(() => {
        if (testType === 'time') {
            setTimeRemaining(testValue); // 0 for infinite, or actual seconds
        }
    }, [testType, testValue]);

    // Helper to find current word boundaries in non-code mode
    const getCurrentWordBoundary = (charIndex) => {
        if (isCodeMode) return null; // Only for practice mode

        let wordStart = charIndex;
        let wordEnd = charIndex;

        // Find start of current word (go backwards to last space or start)
        while (wordStart > 0 && characters[wordStart - 1] !== ' ') {
            wordStart--;
        }

        // Find end of current word (go forward to next space or end)
        while (wordEnd < characters.length && characters[wordEnd] !== ' ') {
            wordEnd++;
        }

        return { start: wordStart, end: wordEnd };
    };

    // Calculate indentation of next line (number of leading spaces)
    const getNextLineIndentation = (currentIndex) => {
        let indentCount = 0;
        let searchIndex = currentIndex + 1; // Start after newline

        while (searchIndex < characters.length && characters[searchIndex] === ' ') {
            indentCount++;
            searchIndex++;
        }

        return indentCount;
    };

    // Find next non-whitespace character from current position
    const findNextNonWhitespace = (fromIndex) => {
        let searchIndex = fromIndex;
        while (searchIndex < characters.length &&
            (characters[searchIndex] === ' ' || characters[searchIndex] === '\t')) {
            searchIndex++;
        }
        return searchIndex;
    };

    // Check if we're in the indentation area (after newline, only spaces/tabs before cursor)
    const isInIndentationArea = (index) => {
        // Look backwards from current index
        for (let i = index - 1; i >= 0; i--) {
            if (characters[i] === '\n') {
                // Found newline, check if everything between newline and cursor is whitespace
                for (let j = i + 1; j < index; j++) {
                    if (characters[j] !== ' ' && characters[j] !== '\t') {
                        return false; // Found non-whitespace, not in indent area
                    }
                }
                return true; // Only whitespace between newline and cursor
            }
        }
        // If we're at the very start of the file and only whitespace before cursor
        for (let i = 0; i < index; i++) {
            if (characters[i] !== ' ' && characters[i] !== '\t') {
                return false;
            }
        }
        return true;
    };

    const handleKeyDown = useCallback((e) => {
        if (phase === "finished") return;

        const { key } = e;

        // Prevent default Tab behavior in Code Mode (no longer used for manual skipping)
        if (key === "Tab" && isCodeMode) {
            e.preventDefault();
            return; // Tab does nothing now - indent jumping is fully automatic
        }

        if (key === "Backspace") {
            e.preventDefault();
            setUserInput((prev) => {
                if (prev.length === 0) return prev;

                // Check if last action was an automatic indent jump
                if (indentJumpStack.length > 0) {
                    const lastJump = indentJumpStack[indentJumpStack.length - 1];

                    // If we're at the position right after a jump, undo the entire jump
                    if (lastJump.type === 'indent') {
                        const jumpCount = lastJump.count;
                        const newInput = prev.slice(0, -jumpCount);

                        // Reset all jumped characters to waiting
                        setCharStates(prevStates => {
                            const newStates = [...prevStates];
                            for (let i = 0; i < jumpCount; i++) {
                                newStates[prev.length - 1 - i] = 'waiting';
                            }
                            return newStates;
                        });

                        // Pop from stack
                        setIndentJumpStack(prevStack => prevStack.slice(0, -1));

                        // Decrement totalTyped by the number of jumped characters
                        setTotalTyped(prevTotal => prevTotal - jumpCount);

                        return newInput;
                    }
                }

                // Normal backspace - delete one character
                const newInput = prev.slice(0, -1);
                const deletedIndex = prev.length - 1;

                // Pop from typed char stack and update stats
                setTypedCharStack(prevStack => {
                    if (prevStack.length > 0) {
                        const lastTyped = prevStack[prevStack.length - 1];

                        // Decrement the appropriate stat counter
                        setCharStats(prevStats => ({
                            ...prevStats,
                            correct: prevStats.correct - (lastTyped.type === 'correct' ? 1 : 0),
                            incorrect: prevStats.incorrect - (lastTyped.type === 'incorrect' ? 1 : 0),
                            missed: prevStats.missed - (lastTyped.type === 'missed' ? 1 : 0)
                        }));

                        return prevStack.slice(0, -1);
                    }
                    return prevStack;
                });

                // Reset the character state back to waiting
                setCharStates(prevStates => {
                    const newStates = [...prevStates];
                    if (deletedIndex < newStates.length) {
                        newStates[deletedIndex] = 'waiting';
                    }
                    return newStates;
                });

                // Decrement totalTyped by 1
                setTotalTyped(prevTotal => Math.max(0, prevTotal - 1));

                return newInput;
            });
            return;
        }

        // Handle Enter key as newline in Code Mode
        if (key === "Enter" && isCodeMode) {
            e.preventDefault();

            const currentIndex = userInput.length;
            const expectedChar = characters[currentIndex];

            // Only process if the expected character is a newline
            if (expectedChar === '\n') {
                // Treat Enter as typing the newline character
                setUserInput((prev) => {
                    // Mark newline as correct
                    setCharStates(prevStates => {
                        const newStates = [...prevStates];
                        newStates[currentIndex] = 'correct';
                        return newStates;
                    });

                    // Increment totalTyped for the newline character
                    setTotalTyped(prevTotal => prevTotal + 1);

                    const nextInput = prev + '\n';

                    // AUTOMATIC INDENT JUMPING - Calculate and jump to first non-whitespace
                    const indentCount = getNextLineIndentation(currentIndex);

                    if (indentCount > 0) {
                        // Auto-skip the indentation spaces
                        const indentChars = characters.slice(currentIndex + 1, currentIndex + 1 + indentCount).join('');

                        // Mark indentation characters as correct (pre-filled)
                        setCharStates(prevStates => {
                            const newStates = [...prevStates];
                            for (let i = 0; i < indentCount; i++) {
                                newStates[currentIndex + 1 + i] = 'correct';
                            }
                            return newStates;
                        });

                        // Push to stack for backspace tracking (AOOP Req 2: Stack)
                        setIndentJumpStack(prevStack => [...prevStack, { type: 'indent', count: indentCount }]);

                        // Increment totalTyped for the auto-skipped indentation characters
                        setTotalTyped(prevTotal => prevTotal + indentCount);

                        // Return input with newline + indentation (cursor jumps automatically)
                        return nextInput + indentChars;
                    }

                    return nextInput;
                });

                if (phase === "start") {
                    setPhase("typing");
                    setStartTime(Date.now());
                }
            }
            return;
        }

        if (key.length > 1) return; // Ignore Shift, Ctrl, Alt, etc.

        if (phase === "start") {
            setPhase("typing");
            setStartTime(Date.now());
        }

        setUserInput((prev) => {
            const currentIndex = prev.length; // Index of character we're typing
            const expectedChar = characters[currentIndex];

            // In Code Mode, prevent typing regular characters when expecting a newline
            // User MUST press Enter to advance to next line
            if (isCodeMode && expectedChar === '\n' && key !== '\n') {
                // Trying to type a regular character when newline is expected
                // Mark as incorrect but don't advance
                setCharStates(prevStates => {
                    const newStates = [...prevStates];
                    newStates[currentIndex] = 'incorrect';
                    return newStates;
                });

                // Don't add to input - force user to press Enter
                return prev;
            }

            // MISSED CHARACTER LOGIC (Practice Mode Only)
            // If user presses space while still within a word, count remaining chars as missed
            if (!isCodeMode && key === ' ' && expectedChar && expectedChar !== ' ') {
                const wordBoundary = getCurrentWordBoundary(currentIndex);
                if (wordBoundary) {
                    // Count characters from current position to end of word as missed
                    const missedCount = wordBoundary.end - currentIndex;

                    // Mark these characters as missed in charStates
                    setCharStates(prevStates => {
                        const newStates = [...prevStates];
                        for (let i = currentIndex; i < wordBoundary.end; i++) {
                            newStates[i] = 'incorrect'; // Visual feedback
                        }
                        return newStates;
                    });

                    // Update stats - add to missed count
                    setCharStats(prevStats => ({
                        ...prevStats,
                        missed: prevStats.missed + missedCount
                    }));

                    // Push to stack for backspace handling
                    setTypedCharStack(prevStack => [
                        ...prevStack,
                        { type: 'missed', char: key, count: missedCount }
                    ]);

                    // Increment total typed
                    setTotalTyped(prev => prev + 1);

                    // Continue to the space (skip the word)
                    return prev + key;
                }
            }

            // EXTRA CHARACTER LOGIC
            // If typing beyond the expected text length
            if (currentIndex >= characters.length) {
                // This is an extra character
                setCharStats(prevStats => ({
                    ...prevStats,
                    extra: prevStats.extra + 1
                }));

                // Push to stack
                setTypedCharStack(prevStack => [
                    ...prevStack,
                    { type: 'extra', char: key }
                ]);

                // Increment total typed
                setTotalTyped(prev => prev + 1);

                return prev + key;
            }

            // NORMAL CHARACTER LOGIC
            const isCorrect = key === expectedChar;
            setCharStates(prevStates => {
                const newStates = [...prevStates];
                newStates[currentIndex] = isCorrect ? 'correct' : 'incorrect';
                return newStates;
            });

            // Update character statistics
            setCharStats(prevStats => ({
                ...prevStats,
                correct: prevStats.correct + (isCorrect ? 1 : 0),
                incorrect: prevStats.incorrect + (isCorrect ? 0 : 1)
            }));

            // Push to stack for backspace handling
            setTypedCharStack(prevStack => [
                ...prevStack,
                { type: isCorrect ? 'correct' : 'incorrect', char: key }
            ]);

            // Increment total typed characters (for accuracy and raw WPM)
            setTotalTyped(prev => prev + 1);

            const nextInput = prev + key;

            // Check if finished (mode-specific completion logic)
            if (testType === 'words') {
                // Words mode: finish when exact word count is reached
                // Count completed words (words followed by space or end of text)
                const completedWords = nextInput.trim().split(/\s+/).filter(w => w.length > 0).length;

                // Check if we've typed the last character of the last word
                if (nextInput.length >= characters.length) {
                    setPhase("finished");
                } else if (completedWords >= testValue && key === ' ') {
                    // Also finish if we've completed the target word count
                    setPhase("finished");
                }
            }
            // Time mode: finish when timer hits zero (handled in timer effect)

            return nextInput;
        });
    }, [phase, characters, currentCharIndex, isCodeMode, indentJumpStack, userInput]);

    // WPM Calculator with history tracking (every 1 second)
    useEffect(() => {
        if (phase === "typing" && startTime) {
            const interval = setInterval(() => {
                const timeElapsed = (Date.now() - startTime) / 60000;

                // Net WPM: (Correct Chars / 5) / Time
                // Considers only correct characters as useful work
                const netWordsTyped = charStats.correct / 5;
                const currentNetWpm = Math.max(0, Math.round(netWordsTyped / timeElapsed));

                // Raw WPM: (Total Keystrokes / 5) / Time
                // Considers all typing effort including errors
                const rawWordsTyped = totalTyped / 5;
                const currentRawWpm = Math.max(0, Math.round(rawWordsTyped / timeElapsed));

                setWpm(currentNetWpm);
                setRawWpm(currentRawWpm);
                setWpmHistory(prev => [...prev, currentNetWpm]);
            }, 1000); // 1 second interval for chart data points
            return () => clearInterval(interval);
        }
    }, [userInput, startTime, phase, charStats, totalTyped]);

    // Timer countdown for time mode (1-second intervals)
    // Skip countdown if testValue = 0 (infinite mode)
    useEffect(() => {
        if (testType === 'time' && phase === 'typing' && timeRemaining > 0 && testValue > 0) {
            const timerInterval = setInterval(() => {
                setTimeRemaining(prev => {
                    const newTime = prev - 1;

                    // End game when time runs out
                    if (newTime <= 0) {
                        setPhase('finished');
                        return 0;
                    }

                    return newTime;
                });
            }, 1000);

            return () => clearInterval(timerInterval);
        }
    }, [testType, phase, timeRemaining, testValue]);

    // Save stats to backend when finished (Req 3)
    useEffect(() => {
        if (phase === "finished") {
            const saveStats = async () => {
                try {
                    const statsData = {
                        mode: isCodeMode ? "CODE" : "PRACTICE",
                        wpm: wpm,
                        rawWpm: rawWpm,
                        accuracy: accuracy,
                        wordsTyped: Math.round(charStats.correct / 5),
                        duration: Date.now() - startTime,
                        characters: charStats,
                        timestamp: new Date().toISOString()
                    };

                    // Import API service dynamically or assume it's available via fetch
                    // Using fetch here to avoid dependency issues within hook or passed arg
                    await fetch('http://localhost:8080/api/scores', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                        },
                        body: JSON.stringify(statsData)
                    });
                    console.log("Stats saved to backend scores.dat");
                } catch (error) {
                    console.error("Failed to save stats:", error);
                }
            };
            saveStats();
        }
    }, [phase, wpm, rawWpm, accuracy, charStats, startTime, isCodeMode]);

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
        charStats,
        timeRemaining,
        testType,
        testValue
    };
};

export default useTypingEngine;