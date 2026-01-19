import React from "react";
import { clsx } from "clsx";

const CodeBlock = ({ text, userInput }) => {
  return (
    <pre className="text-left font-mono text-xl leading-relaxed whitespace-pre-wrap text-gray-500">
      {text.split("").map((char, index) => {
        const userChar = userInput[index];
        let className = "text-gray-600"; // Default

        if (userChar) {
          className = userChar === char ? "text-gray-100" : "text-red-500 bg-red-900/20";
        }
        
        // Highlight current character
        if (index === userInput.length) {
          return (
            <span key={index} className="bg-yellow-400/20 border-l-2 border-yellow-400 text-gray-200">
              {char}
            </span>
          );
        }

        return <span key={index} className={className}>{char}</span>;
      })}
    </pre>
  );
};

export default CodeBlock;