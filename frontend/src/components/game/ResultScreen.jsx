import React from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { clsx } from 'clsx';

const ResultScreen = ({
    wpm,
    rawWpm,
    accuracy,
    testType = 'time 30',
    language = 'english',
    characters = { correct: 0, incorrect: 0, missed: 0 },
    consistency = 0,
    wpmHistory = [],
    onNextTest,
    onRestart,
    onSettings
}) => {
    // Calculate consistency if not provided
    const calculatedConsistency = consistency || calculateConsistency(wpmHistory);

    // Format WPM history data for chart
    const chartData = wpmHistory.map((wpmValue, index) => ({
        time: index,
        wpm: wpmValue,
        raw: rawWpm || wpmValue
    }));

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-[#323437] animate-fadeIn">
            <div className="w-full max-w-4xl px-8">

                {/* Key Stats - Large Display */}
                <div className="flex justify-center gap-16 mb-12">
                    <div className="text-center">
                        <div className="text-7xl font-bold text-yellow-400 mb-2">
                            {Math.round(wpm)}
                        </div>
                        <div className="text-sm text-gray-500 uppercase tracking-widest">
                            WPM
                        </div>
                    </div>

                    <div className="text-center">
                        <div className="text-7xl font-bold text-yellow-400 mb-2">
                            {Math.round(accuracy)}%
                        </div>
                        <div className="text-sm text-gray-500 uppercase tracking-widest">
                            ACC
                        </div>
                    </div>
                </div>

                {/* Detailed Breakdown */}
                <div className="grid grid-cols-5 gap-6 mb-8">
                    <StatCard label="test type" value={testType} />
                    <StatCard label="raw" value={Math.round(rawWpm || wpm)} />
                    <StatCard
                        className="col-span-2"
                        label="characters"
                        value={
                            <div className="flex gap-4 text-xl group cursor-help relative items-center w-full">
                                <div className="flex flex-col items-center relative">
                                    <span className="text-green-400 font-bold">{characters.correct}</span>
                                    <span className="text-[10px] text-gray-500 uppercase opacity-0 group-hover:opacity-100 transition-opacity absolute -bottom-4 left-1/2 -translate-x-1/2 whitespace-nowrap">correct</span>
                                </div>
                                <span className="text-gray-600 text-sm">/</span>
                                <div className="flex flex-col items-center relative">
                                    <span className="text-red-400 font-bold">{characters.incorrect}</span>
                                    <span className="text-[10px] text-gray-500 uppercase opacity-0 group-hover:opacity-100 transition-opacity absolute -bottom-4 left-1/2 -translate-x-1/2 whitespace-nowrap">wrong</span>
                                </div>
                                <span className="text-gray-600 text-sm">/</span>
                                <div className="flex flex-col items-center relative">
                                    <span className="text-blue-400 font-bold">{characters.missed}</span>
                                    <span className="text-[10px] text-gray-500 uppercase opacity-0 group-hover:opacity-100 transition-opacity absolute -bottom-4 left-1/2 -translate-x-1/2 whitespace-nowrap">missed</span>
                                </div>
                            </div>
                        }
                    />
                    <StatCard label="consistency" value={`${calculatedConsistency}%`} />
                </div>

                {/* WPM Chart */}
                {chartData.length > 0 && (
                    <div className="mb-8 bg-[#2c2e31] rounded-lg p-6 border border-gray-700">
                        <ResponsiveContainer width="100%" height={200}>
                            <LineChart data={chartData}>
                                <CartesianGrid strokeDasharray="3 3" stroke="#3a3c3f" />
                                <XAxis
                                    dataKey="time"
                                    stroke="#646669"
                                    tick={{ fill: '#646669', fontSize: 12 }}
                                />
                                <YAxis
                                    stroke="#646669"
                                    tick={{ fill: '#646669', fontSize: 12 }}
                                />
                                <Tooltip
                                    contentStyle={{
                                        backgroundColor: '#2c2e31',
                                        border: '1px solid #646669',
                                        borderRadius: '4px',
                                        color: '#d1d0c5'
                                    }}
                                />
                                {/* Raw WPM - Yellow Line */}
                                <Line
                                    type="monotone"
                                    dataKey="raw"
                                    stroke="#e2b714"
                                    strokeWidth={2}
                                    dot={false}
                                    name="Raw"
                                />
                                {/* WPM - Gray Line */}
                                <Line
                                    type="monotone"
                                    dataKey="wpm"
                                    stroke="#646669"
                                    strokeWidth={2}
                                    dot={false}
                                    name="WPM"
                                />
                            </LineChart>
                        </ResponsiveContainer>
                    </div>
                )}

                {/* Action Buttons */}
                <div className="flex justify-center gap-4">
                    <ActionButton
                        icon="↻"
                        label="next test"
                        onClick={onNextTest}
                    />
                    <ActionButton
                        icon="⟲"
                        label="restart test"
                        onClick={onRestart}
                    />
                    <ActionButton
                        icon="⚙"
                        label="settings"
                        onClick={onSettings}
                    />
                </div>

                {/* Keyboard Shortcuts */}
                <div className="flex gap-6 text-sm text-gray-500">
                    <div>
                        <span className="bg-gray-800 px-2 py-1 rounded border border-gray-700">Tab</span> - next test
                    </div>{' • '}
                    <span className="bg-gray-800 px-2 py-1 rounded border border-gray-700">Enter</span> - restart
                </div>
            </div>

            {/* CSS Animation */}
            <style jsx>{`
        @keyframes fadeIn {
          from {
            opacity: 0;
            transform: translateY(20px);
          }
          to {
            opacity: 1;
            transform: translateY(0);
          }
        }
        .animate-fadeIn {
          animation: fadeIn 0.4s ease-out;
        }
      `}</style>
        </div>
    );
};

// Stat Card Component
const StatCard = ({ label, value, className = "" }) => (
    <div className={`bg-[#2c2e31] rounded-lg p-4 border border-gray-700 ${className}`}>
        <div className="text-xs text-gray-500 uppercase tracking-widest mb-2">
            {label}
        </div>
        <div className="text-xl text-gray-300 font-mono">
            {value}
        </div>
    </div>
);

// Action Button Component
const ActionButton = ({ icon, label, onClick }) => (
    <button
        onClick={onClick}
        className={clsx(
            "flex flex-col items-center gap-2 px-6 py-4",
            "bg-[#2c2e31] rounded-lg border border-gray-700",
            "hover:bg-[#3a3c3f] hover:border-yellow-400",
            "transition-all duration-200",
            "group"
        )}
    >
        <span className="text-2xl text-gray-500 group-hover:text-yellow-400 transition-colors">
            {icon}
        </span>
        <span className="text-xs text-gray-500 uppercase tracking-widest group-hover:text-gray-300 transition-colors">
            {label}
        </span>
    </button>
);

// Helper function to calculate consistency
const calculateConsistency = (wpmHistory) => {
    if (wpmHistory.length < 2) return 100;

    const mean = wpmHistory.reduce((a, b) => a + b, 0) / wpmHistory.length;
    const variance = wpmHistory.reduce((sum, wpm) => sum + Math.pow(wpm - mean, 2), 0) / wpmHistory.length;
    const stdDev = Math.sqrt(variance);

    // Convert to consistency percentage (lower std dev = higher consistency)
    const consistency = Math.max(0, 100 - (stdDev / mean) * 100);
    return Math.round(consistency);
};

export default ResultScreen;
