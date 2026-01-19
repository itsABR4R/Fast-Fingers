import React from 'react';
import { clsx } from 'clsx';

const ModeToolbar = ({
    testType,
    testValue,
    onTimeSelect,
    onInfiniteSelect,
    onWordsSelect,
    disabled = false,
    isCodeMode = false
}) => {
    const timeOptions = [15, 30, 60];
    const wordOptions = [10, 25, 50, 100];

    return (
        <div className="flex items-center justify-center gap-6 mb-8">
            {/* Time Mode */}
            <div className="flex items-center gap-2">
                <span className="text-gray-500 text-sm mr-2">⏱</span>
                {timeOptions.map((seconds) => (
                    <button
                        key={`time-${seconds}`}
                        onClick={() => onTimeSelect(seconds)}
                        disabled={disabled}
                        className={clsx(
                            'px-4 py-2 rounded-lg font-mono text-sm transition-all duration-200',
                            'hover:bg-gray-700',
                            testType === 'time' && testValue === seconds
                                ? 'bg-yellow-400 text-gray-900 font-bold'
                                : 'bg-gray-800 text-gray-400',
                            disabled && 'opacity-50 cursor-not-allowed'
                        )}
                    >
                        {seconds}
                    </button>
                ))}
                {/* Infinite Mode Button */}
                <button
                    onClick={onInfiniteSelect}
                    disabled={disabled}
                    className={clsx(
                        'px-4 py-2 rounded-lg font-mono text-sm transition-all duration-200',
                        'hover:bg-gray-700',
                        testType === 'time' && testValue === 0
                            ? 'bg-yellow-400 text-gray-900 font-bold'
                            : 'bg-gray-800 text-gray-400',
                        disabled && 'opacity-50 cursor-not-allowed'
                    )}
                    title="Infinite mode - no timer"
                >
                    ∞
                </button>
            </div>

            {/* Words Mode - Hidden in Code Mode */}
            {!isCodeMode && (
                <>
                    {/* Separator */}
                    <div className="h-8 w-px bg-gray-700"></div>

                    <div className="flex items-center gap-2">
                        <span className="text-gray-500 text-sm mr-2">📝</span>
                        {wordOptions.map((count) => (
                            <button
                                key={`words-${count}`}
                                onClick={() => onWordsSelect(count)}
                                disabled={disabled}
                                className={clsx(
                                    'px-4 py-2 rounded-lg font-mono text-sm transition-all duration-200',
                                    'hover:bg-gray-700',
                                    testType === 'words' && testValue === count
                                        ? 'bg-yellow-400 text-gray-900 font-bold'
                                        : 'bg-gray-800 text-gray-400',
                                    disabled && 'opacity-50 cursor-not-allowed'
                                )}
                            >
                                {count}
                            </button>
                        ))}
                    </div>
                </>
            )}
        </div>
    );
};

export default ModeToolbar;
