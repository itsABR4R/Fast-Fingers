// Game State
let currentMode = null;
let socket = null;
let playerId = null;
let gameWords = [];
let currentWordIndex = 0;
let myLives = 3;
let opponentLives = 3;
let gameStarted = false;
let startTime = null;
let correctChars = 0;
let totalChars = 0;

// DOM Elements
const modeMenu = document.getElementById('modeMenu');
const gameArea = document.getElementById('gameArea');
const currentModeElement = document.getElementById('currentMode');
const wordsElement = document.getElementById('words');
const typingInput = document.getElementById('typingInput');
const myLivesElement = document.getElementById('myLives');
const opponentLivesElement = document.getElementById('opponentLives');
const myWPMElement = document.getElementById('myWPM');
const myAccuracyElement = document.getElementById('myAccuracy');
const opponentWPMElement = document.getElementById('opponentWPM');
const opponentAccuracyElement = document.getElementById('opponentAccuracy');
const timerElement = document.getElementById('timer');
const statusText = document.getElementById('statusText');
const eliminatedOverlay = document.getElementById('eliminatedOverlay');
const victoryOverlay = document.getElementById('victoryOverlay');
const gameEndOverlay = document.getElementById('gameEndOverlay');

/**
 * Select game mode and initialize
 */
function selectMode(mode) {
    currentMode = mode;
    console.log('[Game] Selected mode:', mode);

    // Update UI
    modeMenu.classList.add('hidden');
    gameArea.classList.remove('hidden');

    // Update mode badge
    const modeNames = {
        'PRACTICE': 'Practice Mode',
        'VS_BOT': 'Vs Bot Mode',
        'VS_FRIEND': 'Vs Friend Mode',
        'ELIMINATION': 'Elimination Mode (3 Lives)'
    };
    currentModeElement.textContent = modeNames[mode];

    // Show/hide opponent stats based on mode
    const opponentStats = document.querySelector('.opponent-stats');
    if (mode === 'PRACTICE') {
        opponentStats.style.display = 'none';
    } else {
        opponentStats.style.display = 'flex';
    }

    // Show/hide lives based on mode
    const livesElements = document.querySelectorAll('.lives');
    if (mode === 'ELIMINATION') {
        livesElements.forEach(el => el.style.display = 'flex');
    } else {
        livesElements.forEach(el => el.style.display = 'none');
    }

    // Initialize game
    initializeGame(mode);
}

/**
 * Return to mode selection menu
 */
function returnToMenu() {
    // Reset game state
    resetGame();

    // Hide overlays
    eliminatedOverlay.classList.remove('show');
    victoryOverlay.classList.remove('show');
    gameEndOverlay.classList.remove('show');

    // Show menu, hide game
    gameArea.classList.add('hidden');
    modeMenu.classList.remove('hidden');

    // Disconnect socket if connected
    if (socket) {
        socket.close();
        socket = null;
    }
}

/**
 * Initialize game for selected mode
 */
async function initializeGame(mode) {
    resetGame();

    // Generate player ID
    playerId = 'P' + Math.random().toString(36).substr(2, 9);

    // Connect based on mode
    if (mode === 'VS_FRIEND' || mode === 'ELIMINATION') {
        connectToGameServer(mode);
    } else if (mode === 'VS_BOT') {
        initializeBotMode();
    } else {
        initializePracticeMode();
    }

    // Fetch words from backend
    await fetchWords();

    // Focus input
    typingInput.focus();
}

/**
 * Connect to GameServer via WebSocket (for multiplayer modes)
 */
function connectToGameServer(mode) {
    statusText.textContent = 'Connecting...';

    // For demo: simulate connection
    // In production, use: new WebSocket('ws://localhost:9090')
    setTimeout(() => {
        statusText.textContent = 'Connected';
        document.querySelector('.status-dot').classList.add('connected');

        // Send mode header
        console.log('[Socket] START_MODE:' + mode);

        // Simulate opponent for demo
        if (mode === 'ELIMINATION') {
            simulateOpponent();
        }
    }, 500);
}

/**
 * Initialize bot mode
 */
function initializeBotMode() {
    statusText.textContent = 'Bot Ready';
    document.querySelector('.status-dot').classList.add('connected');
    simulateOpponent();
}

/**
 * Initialize practice mode
 */
function initializePracticeMode() {
    statusText.textContent = 'Ready';
    document.querySelector('.status-dot').classList.add('connected');
}

/**
 * Fetch words from Spring Boot backend
 */
async function fetchWords() {
    try {
        const response = await fetch('/api/words/random?count=50');
        const data = await response.json();
        gameWords = data.words;
        displayWords();
        console.log('[Game] Loaded', gameWords.length, 'words');
    } catch (error) {
        console.error('[Game] Error fetching words:', error);
        // Fallback words
        gameWords = ['the', 'quick', 'brown', 'fox', 'jumps', 'over', 'lazy', 'dog'];
        displayWords();
    }
}

/**
 * Display words
 */
function displayWords() {
    wordsElement.innerHTML = '';
    gameWords.forEach((word, index) => {
        const wordElement = document.createElement('div');
        wordElement.className = 'word';
        if (index === 0) wordElement.classList.add('active');

        word.split('').forEach(char => {
            const charElement = document.createElement('span');
            charElement.className = 'char';
            charElement.textContent = char;
            wordElement.appendChild(charElement);
        });

        wordsElement.appendChild(wordElement);
    });
}

/**
 * Handle typing
 */
typingInput.addEventListener('input', (e) => {
    if (!gameStarted) {
        gameStarted = true;
        startTime = Date.now();
        startTimer();
    }

    const typedText = e.target.value;
    const currentWord = gameWords[currentWordIndex];
    const wordElement = wordsElement.children[currentWordIndex];
    const chars = wordElement.querySelectorAll('.char');

    // Update character colors
    chars.forEach((char, index) => {
        if (index < typedText.length) {
            if (typedText[index] === currentWord[index]) {
                char.classList.add('correct');
                char.classList.remove('wrong');
                if (!char.dataset.counted) {
                    correctChars++;
                    char.dataset.counted = 'true';
                }
            } else {
                char.classList.add('wrong');
                char.classList.remove('correct');
                char.dataset.counted = 'false';
            }
            totalChars = Math.max(totalChars, index + 1);
        } else {
            char.classList.remove('correct', 'wrong');
            char.dataset.counted = 'false';
        }
    });

    updateStats();
});

/**
 * Handle word submission (Space or Enter)
 */
typingInput.addEventListener('keydown', (e) => {
    if (e.key === ' ' || e.key === 'Enter') {
        e.preventDefault();
        submitWord();
    }
});

/**
 * Submit current word
 */
function submitWord() {
    const typedText = typingInput.value.trim();
    const currentWord = gameWords[currentWordIndex];
    const wordElement = wordsElement.children[currentWordIndex];

    const isCorrect = typedText === currentWord;

    if (!isCorrect && currentMode === 'ELIMINATION') {
        loseLife('me');
    }

    // Mark completed
    wordElement.classList.remove('active');
    wordElement.classList.add('completed');

    // Next word
    currentWordIndex++;
    if (currentWordIndex < gameWords.length) {
        wordsElement.children[currentWordIndex].classList.add('active');
        typingInput.value = '';
    } else {
        endGame('completed');
    }
}

/**
 * Lose a life
 */
function loseLife(player) {
    if (player === 'me') {
        if (myLives > 0) {
            myLives--;
            updateLivesDisplay('me');
            if (myLives === 0) {
                showEliminated();
            }
        }
    } else {
        if (opponentLives > 0) {
            opponentLives--;
            updateLivesDisplay('opponent');
            if (opponentLives === 0) {
                showVictory();
            }
        }
    }
}

/**
 * Update lives display
 */
function updateLivesDisplay(player) {
    const livesElement = player === 'me' ? myLivesElement : opponentLivesElement;
    const lives = player === 'me' ? myLives : opponentLives;
    const hearts = livesElement.querySelectorAll('.heart');

    hearts.forEach((heart, index) => {
        if (index >= lives) {
            heart.classList.add('lost');
        }
    });
}

/**
 * Update stats
 */
function updateStats() {
    if (!startTime) return;

    const elapsed = (Date.now() - startTime) / 1000 / 60;
    const wpm = Math.round((currentWordIndex / elapsed) || 0);
    const accuracy = totalChars > 0 ? Math.round((correctChars / totalChars) * 100) : 100;

    myWPMElement.textContent = wpm;
    myAccuracyElement.textContent = accuracy;
}

/**
 * Start timer
 */
function startTimer() {
    setInterval(() => {
        if (!startTime) return;
        const elapsed = Math.floor((Date.now() - startTime) / 1000);
        const minutes = Math.floor(elapsed / 60);
        const seconds = elapsed % 60;
        timerElement.textContent = `${minutes}:${seconds.toString().padStart(2, '0')}`;
    }, 1000);
}

/**
 * Simulate opponent (for demo)
 */
function simulateOpponent() {
    setInterval(() => {
        if (!gameStarted) return;

        const randomWPM = Math.floor(Math.random() * 30) + 40;
        opponentWPMElement.textContent = randomWPM;

        const randomAccuracy = Math.floor(Math.random() * 10) + 90;
        opponentAccuracyElement.textContent = randomAccuracy;

        if (currentMode === 'ELIMINATION' && Math.random() < 0.05 && opponentLives > 0) {
            loseLife('opponent');
        }
    }, 2000);
}

/**
 * Show eliminated overlay
 */
function showEliminated() {
    eliminatedOverlay.classList.add('show');
    typingInput.disabled = true;
}

/**
 * Show victory overlay
 */
function showVictory() {
    document.getElementById('finalWPM').textContent = myWPMElement.textContent;
    document.getElementById('finalAccuracy').textContent = myAccuracyElement.textContent;
    victoryOverlay.classList.add('show');
    typingInput.disabled = true;
}

/**
 * End game
 */
function endGame(reason) {
    document.getElementById('endWPM').textContent = myWPMElement.textContent;
    document.getElementById('endAccuracy').textContent = myAccuracyElement.textContent;

    if (reason === 'completed') {
        document.getElementById('gameEndTitle').textContent = '🎉 Completed! 🎉';
        document.getElementById('gameEndMessage').textContent = 'You finished all words!';
    }

    gameEndOverlay.classList.add('show');
    typingInput.disabled = true;
}

/**
 * Reset game state
 */
function resetGame() {
    gameStarted = false;
    startTime = null;
    currentWordIndex = 0;
    correctChars = 0;
    totalChars = 0;
    myLives = 3;
    opponentLives = 3;

    typingInput.value = '';
    typingInput.disabled = false;

    myWPMElement.textContent = '0';
    myAccuracyElement.textContent = '100';
    opponentWPMElement.textContent = '0';
    opponentAccuracyElement.textContent = '100';
    timerElement.textContent = '0:00';

    // Reset lives display
    document.querySelectorAll('.heart').forEach(heart => {
        heart.classList.remove('lost');
    });
}

// Keyboard shortcuts
document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && !modeMenu.classList.contains('hidden')) {
        returnToMenu();
    }
});
