import { useState, useCallback } from 'react';

/**
 * Hook to manage game configuration (mode, test type, test value)
 * Provides exclusive toggle between Time and Words modes
 */
const useGameConfig = () => {
    const [testType, setTestType] = useState('time'); // 'time' | 'words'
    const [testValue, setTestValue] = useState(30); // 15/30/60 for time, 10/25/100 for words
    const [timeRemaining, setTimeRemaining] = useState(30); // Only used in time mode

    // Set time mode with specific duration
    const setTimeMode = useCallback((seconds) => {
        setTestType('time');
        setTestValue(seconds);
        setTimeRemaining(seconds);
    }, []);

    // Set infinite mode (no timer)
    const setInfiniteMode = useCallback(() => {
        setTestType('time');
        setTestValue(0); // 0 means infinite
        setTimeRemaining(0);
    }, []);

    // Set words mode with specific word count
    const setWordsMode = useCallback((wordCount) => {
        setTestType('words');
        setTestValue(wordCount);
        setTimeRemaining(0); // Not used in words mode
    }, []);

    // Reset to default
    const resetConfig = useCallback(() => {
        setTestType('time');
        setTestValue(30);
        setTimeRemaining(30);
    }, []);

    return {
        testType,
        testValue,
        timeRemaining,
        setTimeRemaining,
        setTimeMode,
        setInfiniteMode,
        setWordsMode,
        resetConfig
    };
};

export default useGameConfig;
