# FastFingers - Complete Project Guide

> **A MonkeyType-inspired competitive typing platform demonstrating Advanced Object-Oriented Programming concepts**

---

## 📖 Table of Contents

1. [Project Overview](#1-project-overview)
2. [Core Features & Mechanics](#2-core-features--mechanics)
3. [AOOP Requirement Implementation](#3-aoop-requirement-implementation)
4. [Data Flow & Statistics](#4-data-flow--statistics)
5. [Setup & Running](#5-setup--running)

---

## 1. Project Overview

### 1.1 High-Level Description

**FastFingers** is a full-stack competitive typing platform that combines the engaging gameplay of MonkeyType with robust backend architecture. The application provides multiple typing modes (Practice, Code, Multiplayer) with real-time performance tracking, character-level feedback, and persistent statistics.

### 1.2 Tech Stack Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      FRONTEND LAYER                         │
│  React 18 + Vite + Tailwind CSS + Recharts                 │
│  - Character-level typing engine (useTypingEngine.js)      │
│  - Real-time WPM/Accuracy calculation                      │
│  - Smooth animations & visual feedback                     │
└─────────────────────────────────────────────────────────────┘
                              ↕ HTTP/WebSocket
┌─────────────────────────────────────────────────────────────┐
│                      BACKEND LAYER                          │
│  Spring Boot 3.2 + Java 17                                 │
│  - REST API (TypingController.java)                        │
│  - WebSocket (GameWebSocketController.java)                │
│  - File I/O (ScoreManager.java, CodeSnippetLoader.java)    │
└─────────────────────────────────────────────────────────────┘
                              ↕ Java Sockets
┌─────────────────────────────────────────────────────────────┐
│                   MULTIPLAYER LAYER                         │
│  Java Socket Server (GameServer.java)                      │
│  - ClientHandler threads (one per player)                  │
│  - GameSession threads (match management)                  │
│  - Real-time progress broadcasting                         │
└─────────────────────────────────────────────────────────────┘
```

**Key Technologies:**
- **Spring Boot**: RESTful APIs, WebSocket support, dependency injection
- **React (Vite)**: Fast development, hot module replacement
- **Tailwind CSS**: Utility-first styling, responsive design
- **Java Sockets**: Low-level multiplayer networking
- **WebSocket**: Browser-compatible real-time communication

---

## 2. Core Features & Mechanics

### 2.1 Typing Engine Architecture

The typing engine (`useTypingEngine.js`) is the heart of the application, implementing character-level tracking with precise state management.

#### Character State Machine

```javascript
// Three possible states for each character
'waiting'    → Character not yet typed
'correct'    → Character typed correctly
'incorrect'  → Character typed incorrectly
```

#### Stack-Based Backspace Logic (AOOP Req 2)

The engine uses a **Stack** data structure (`typedCharStack`) to enable intelligent backspace functionality:

```javascript
// Stack structure for each typed character
{
  type: 'correct' | 'incorrect' | 'missed',
  char: 'a',
  count: 1  // For missed characters (multiple chars skipped)
}
```

**Backspace Behavior:**
1. Pop from `typedCharStack`
2. Decrement appropriate stat counter (`correct`, `incorrect`, or `missed`)
3. Reset character state to `'waiting'`
4. Decrement `totalTyped` counter

**Special Case - Auto-Indent Backspace:**
```javascript
// In code mode, pressing backspace after auto-indent jump
// undoes the ENTIRE indent at once (not character-by-character)
if (indentJumpStack.length > 0) {
  const lastJump = indentJumpStack[indentJumpStack.length - 1];
  const jumpCount = lastJump.count;
  // Delete all jumped characters in one operation
  const newInput = prev.slice(0, -jumpCount);
}
```

#### Character Classification Logic

**1. Correct Characters:**
```javascript
const isCorrect = key === expectedChar;
if (isCorrect) {
  charStats.correct++;
  charStates[currentIndex] = 'correct';
}
```

**2. Incorrect Characters:**
```javascript
if (!isCorrect) {
  charStats.incorrect++;
  charStates[currentIndex] = 'incorrect';
}
```

**3. Missed Characters (Practice Mode Only):**

Missed characters occur when a user presses **Space** before completing a word:

```javascript
// User presses space at position 3 in "hello"
// Expected: "hello"
// Typed:    "hel "
// Result:   "lo" are marked as MISSED (2 characters)

if (key === ' ' && expectedChar !== ' ') {
  const wordBoundary = getCurrentWordBoundary(currentIndex);
  const missedCount = wordBoundary.end - currentIndex;
  
  charStats.missed += missedCount;
  // Mark remaining word characters as 'incorrect' visually
  for (let i = currentIndex; i < wordBoundary.end; i++) {
    charStates[i] = 'incorrect';
  }
}
```

**4. Extra Characters (Typing Beyond Text):**
```javascript
// User types more characters than the provided text
if (currentIndex >= characters.length) {
  charStats.extra++;
  // Still counted in totalTyped for raw WPM calculation
}
```

### 2.2 Game Modes

#### Mode 1: Timer Mode (Interrupt-Driven)

**Mechanism:** Background thread counts down from selected time (15/30/60 seconds or ∞)

```javascript
// Timer countdown effect (1-second intervals)
useEffect(() => {
  if (testType === 'time' && phase === 'typing' && timeRemaining > 0 && testValue > 0) {
    const timerInterval = setInterval(() => {
      setTimeRemaining(prev => {
        const newTime = prev - 1;
        
        // INTERRUPT: End game when time runs out
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
```

**Completion Trigger:** Timer reaches 0 → `setPhase('finished')`

**Infinite Mode (∞):**
- `testValue = 0` signals infinite mode
- Timer countdown is skipped: `testValue > 0` check prevents countdown
- Game continues until manually stopped with Tab key

#### Mode 2: Word Mode (Index-Driven)

**Mechanism:** Tracks word completion count, ends when target reached

```javascript
// Word count completion logic
if (testType === 'words') {
  const completedWords = nextInput.trim().split(/\s+/).filter(w => w.length > 0).length;
  
  // Completion triggers:
  // 1. Typed all characters
  if (nextInput.length >= characters.length) {
    setPhase('finished');
  }
  // 2. Completed target word count
  else if (completedWords >= testValue && key === ' ') {
    setPhase('finished');
  }
}
```

**Completion Trigger:** `activeWordIndex >= testValue` OR all characters typed

**Word Count Options:** 10, 25, 50, 100 words

### 2.3 Code Mode - Indentation Preservation

Code mode introduces special handling for programming snippets with proper indentation.

#### Auto-Jump Logic

When the user presses **Enter** after a newline, the system automatically jumps over indentation spaces:

```javascript
// User presses Enter at end of line
if (key === "Enter" && expectedChar === '\n') {
  // 1. Type the newline character
  const nextInput = prev + '\n';
  
  // 2. Calculate next line's indentation
  const indentCount = getNextLineIndentation(currentIndex);
  // Example: "    public void foo()" has 4 leading spaces
  
  if (indentCount > 0) {
    // 3. Auto-skip indentation characters
    const indentChars = characters.slice(
      currentIndex + 1, 
      currentIndex + 1 + indentCount
    ).join('');
    
    // 4. Mark indentation as 'correct' (pre-filled)
    for (let i = 0; i < indentCount; i++) {
      charStates[currentIndex + 1 + i] = 'correct';
    }
    
    // 5. Push to indentJumpStack for backspace handling (AOOP Req 2)
    setIndentJumpStack(prevStack => [
      ...prevStack, 
      { type: 'indent', count: indentCount }
    ]);
    
    // 6. Return input with newline + indentation
    return nextInput + indentChars;
  }
}
```

**Visual Effect:** Cursor jumps from end of line directly to first non-whitespace character of next line

**Backspace Behavior:** Pressing backspace after auto-jump removes entire indentation at once

#### Newline Enforcement

In code mode, users **MUST** press Enter to advance to the next line:

```javascript
// Prevent typing regular characters when newline is expected
if (isCodeMode && expectedChar === '\n' && key !== '\n') {
  // Mark as incorrect but don't advance
  charStates[currentIndex] = 'incorrect';
  return prev; // Don't add to input
}
```

---

## 3. AOOP Requirement Implementation

### 3.1 Collections Framework (Requirement 2)

#### ArrayList - Word Bank Storage

**Location:** `TypingEngine.java`

```java
public class TypingEngine {
    // AOOP Req 2: ArrayList for word bank storage
    private final ArrayList<Word> wordBank;
    
    public TypingEngine() {
        this.wordBank = new ArrayList<>();
        loadWordsFromFile();
    }
    
    /**
     * Get random words using Collections.shuffle() and subList()
     * AOOP Requirement 2: Demonstrates ArrayList usage with Collections framework
     */
    public List<Word> getRandomWords(int count) {
        // Create copy to preserve original order
        ArrayList<Word> shuffledWords = new ArrayList<>(wordBank);
        
        // Randomize using Collections.shuffle() (AOOP Req 2)
        Collections.shuffle(shuffledWords);
        
        // Extract exact count using subList() (AOOP Req 2)
        List<Word> selectedWords = new ArrayList<>(
            shuffledWords.subList(0, Math.min(count, wordBank.size()))
        );
        
        return selectedWords;
    }
}
```

**Usage Flow:**
1. Frontend requests: `GET /api/game/text?lang=english&count=50`
2. Backend calls: `typingEngine.getRandomWords(50)`
3. ArrayList is shuffled and 50 words extracted
4. Words joined with spaces and returned to frontend

#### Stack - Typing History Tracking

**Location:** `useTypingEngine.js` (Frontend)

```javascript
// AOOP Req 2: Stack for tracking typed characters
const [typedCharStack, setTypedCharStack] = useState([]);

// Push to stack on each keystroke
setTypedCharStack(prevStack => [
  ...prevStack,
  { type: isCorrect ? 'correct' : 'incorrect', char: key }
]);

// Pop from stack on backspace
setTypedCharStack(prevStack => {
  if (prevStack.length > 0) {
    const lastTyped = prevStack[prevStack.length - 1];
    // Update stats based on popped character
    return prevStack.slice(0, -1); // Pop operation
  }
  return prevStack;
});
```

**Stack - Auto-Indent Jump Tracking**

**Location:** `useTypingEngine.js` (Code Mode)

```javascript
// AOOP Req 2: Stack for tracking auto-indent jumps
const [indentJumpStack, setIndentJumpStack] = useState([]);

// Push when auto-jumping indentation
setIndentJumpStack(prevStack => [
  ...prevStack, 
  { type: 'indent', count: indentCount }
]);

// Pop when backspacing after jump
setIndentJumpStack(prevStack => prevStack.slice(0, -1));
```

#### HashSet - Unique Words Tracking

**Location:** `PracticeMode.java`

```java
public class PracticeMode {
    // AOOP Req 2: Set for tracking unique words typed
    private final Set<String> uniqueWordsTyped;
    
    public PracticeMode() {
        this.uniqueWordsTyped = new HashSet<>();
    }
    
    /**
     * Record a correctly typed word
     * Uses Set to track unique words (AOOP Req 2)
     */
    public void recordCorrectWord(String word) {
        uniqueWordsTyped.add(word.toLowerCase());
        currentWordIndex++;
    }
    
    public int getUniqueWordCount() {
        return uniqueWordsTyped.size();
    }
}
```

#### Queue - Upcoming Words Management

**Location:** `PerformanceTracker.java`

```java
public class PerformanceTracker {
    // AOOP Req 2: Queue for upcoming words
    private final Queue<Word> upcomingWords;
    
    public PerformanceTracker() {
        this.upcomingWords = new LinkedList<>();
    }
    
    /**
     * Add words to queue (AOOP Req 2)
     */
    public void addUpcomingWords(List<Word> words) {
        upcomingWords.addAll(words);
    }
    
    /**
     * Get next word from queue (AOOP Req 2)
     */
    public Word getNextWord() {
        return upcomingWords.poll(); // Dequeue operation
    }
}
```

### 3.2 File I/O Operations (Requirement 3)

#### FileReader - Loading Code Snippets

**Location:** `CodeSnippetLoader.java`

```java
/**
 * Load code snippet from file using FileReader (AOOP Req 3)
 */
public String loadSnippet(String language, String filename) throws IOException {
    String path = String.format("snippets/%s/%s", language, filename);
    
    try (InputStream is = getClass().getClassLoader().getResourceAsStream(path);
         InputStreamReader isr = new InputStreamReader(is);
         FileReader reader = new FileReader(file);  // AOOP Req 3
         BufferedReader br = new BufferedReader(reader)) {
        
        StringBuilder content = new StringBuilder();
        String line;
        
        while ((line = br.readLine()) != null) {
            content.append(line).append("\n");
        }
        
        return content.toString();
    }
}
```

**Snippet Files:**
- `src/main/resources/snippets/java/BubbleSort.java`
- `src/main/resources/snippets/java/HelloWorld.java`
- `src/main/resources/snippets/java/LinkedList.java`

#### ObjectOutputStream - Saving User Statistics

**Location:** `ScoreManager.java`

```java
/**
 * Save UserStats to binary file using ObjectOutputStream (AOOP Req 3)
 */
public void saveStats(UserStats stats) throws IOException {
    try (ObjectOutputStream oos = new ObjectOutputStream(
            new FileOutputStream(STATS_FILE))) {  // AOOP Req 3
        
        oos.writeObject(stats);  // Serialize UserStats object
        System.out.println("[ScoreManager] Stats saved to scores.dat");
    }
}
```

**UserStats Class (Serializable):**

```java
public class UserStats implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String username;
    private double bestWPM;
    private double averageWPM;
    private double averageAccuracy;
    private int totalGames;
    private List<GameRecord> gameHistory;  // ArrayList of game records
    
    // Getters, setters, and methods
}
```

**Data Flow:**
1. Frontend completes test → sends stats to `POST /api/scores`
2. Backend creates/updates `UserStats` object
3. `ScoreManager.saveStats()` serializes to `scores.dat` using ObjectOutputStream
4. File persists between application restarts

#### ObjectInputStream - Loading User Statistics

**Location:** `ScoreManager.java`

```java
/**
 * Load UserStats from binary file using ObjectInputStream (AOOP Req 3)
 */
public UserStats loadStats() throws IOException, ClassNotFoundException {
    try (ObjectInputStream ois = new ObjectInputStream(
            new FileInputStream(STATS_FILE))) {  // AOOP Req 3
        
        UserStats stats = (UserStats) ois.readObject();  // Deserialize
        System.out.println("[ScoreManager] Stats loaded from scores.dat");
        return stats;
    }
}
```

#### FileWriter - Exporting Match History

**Location:** `ScoreManager.java`

```java
/**
 * Export match history to text file using FileWriter (AOOP Req 3)
 */
public void exportMatchHistory(UserStats stats) throws IOException {
    try (FileWriter writer = new FileWriter(MATCH_HISTORY_FILE);  // AOOP Req 3
         BufferedWriter bw = new BufferedWriter(writer)) {
        
        bw.write("=== Match History for " + stats.getUsername() + " ===\n");
        bw.write("Total Games: " + stats.getTotalGames() + "\n\n");
        
        for (GameRecord record : stats.getGameHistory()) {
            bw.write(String.format("%s | WPM: %.1f | Accuracy: %.1f%%\n",
                record.getTimestamp(), record.getWpm(), record.getAccuracy()));
        }
    }
}
```

### 3.3 Threading (Requirement 4)

#### Thread 1: Game Timer (Frontend)

**Location:** `useTypingEngine.js`

```javascript
// Background thread for timer countdown (AOOP Req 4)
useEffect(() => {
  if (testType === 'time' && phase === 'typing' && timeRemaining > 0) {
    const timerInterval = setInterval(() => {
      setTimeRemaining(prev => prev - 1);
    }, 1000);  // Runs every 1 second in background
    
    return () => clearInterval(timerInterval);  // Cleanup
  }
}, [testType, phase, timeRemaining]);
```

**Thread Behavior:**
- Runs independently in background
- Updates `timeRemaining` state every second
- Triggers game end when timer reaches 0
- Automatically cleaned up when component unmounts

#### Thread 2: WPM Calculator (Frontend)

**Location:** `useTypingEngine.js`

```javascript
// Background thread for WPM calculation (AOOP Req 4)
useEffect(() => {
  if (phase === "typing" && startTime) {
    const interval = setInterval(() => {
      const timeElapsed = (Date.now() - startTime) / 60000;
      
      // Calculate Net WPM
      const netWordsTyped = charStats.correct / 5;
      const currentNetWpm = Math.round(netWordsTyped / timeElapsed);
      
      // Calculate Raw WPM
      const rawWordsTyped = totalTyped / 5;
      const currentRawWpm = Math.round(rawWordsTyped / timeElapsed);
      
      setWpm(currentNetWpm);
      setRawWpm(currentRawWpm);
      setWpmHistory(prev => [...prev, currentNetWpm]);  // For graph
    }, 1000);  // Updates every 1 second
    
    return () => clearInterval(interval);
  }
}, [userInput, startTime, phase, charStats, totalTyped]);
```

**Thread Behavior:**
- Calculates WPM every second
- Captures WPM snapshots for result graph
- Runs concurrently with typing and timer threads

### 3.4 Socket Programming (Requirement 5)

#### Java Socket Server - Multiplayer Game

**Location:** `GameServer.java`

```java
/**
 * Multiplayer game server using Java Sockets (AOOP Req 5)
 */
public class GameServer {
    private static final int PORT = 9090;
    private ServerSocket serverSocket;
    private List<ClientHandler> clients = new ArrayList<>();
    
    public void start() throws IOException {
        serverSocket = new ServerSocket(PORT);
        System.out.println("[GameServer] Listening on port " + PORT);
        
        // Accept client connections (AOOP Req 5: Socket Programming)
        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("[GameServer] New client connected");
            
            // Create thread for each client (AOOP Req 4: Threading)
            ClientHandler handler = new ClientHandler(clientSocket, this);
            clients.add(handler);
            new Thread(handler).start();
        }
    }
}
```

#### ClientHandler - Thread-Per-Client Model

**Location:** `ClientHandler.java`

```java
/**
 * Handles individual client connection (AOOP Req 4: Threading + Req 5: Sockets)
 */
public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private GameServer server;
    
    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            
            String message;
            while ((message = in.readLine()) != null) {
                // Parse JSON message
                GameMessage msg = parseMessage(message);
                
                // Broadcast to other players
                server.broadcast(msg, this);
            }
        } catch (IOException e) {
            System.err.println("[ClientHandler] Connection error: " + e.getMessage());
        }
    }
}
```

#### Socket-to-WebSocket Bridge

**Architecture:**

```
┌─────────────┐                    ┌──────────────┐
│   Browser   │ ←─── WebSocket ───→│ Spring Boot  │
│  (React)    │                    │   Backend    │
└─────────────┘                    └──────────────┘
                                           ↕
                                    Java Socket
                                           ↕
                                   ┌──────────────┐
                                   │ GameServer   │
                                   │ (Port 9090)  │
                                   └──────────────┘
```

**WebSocket Controller:**

```java
@Controller
public class GameWebSocketController {
    @MessageMapping("/game/progress")
    @SendTo("/topic/game")
    public GameMessage handleProgress(GameMessage message) {
        // Receive from WebSocket, forward to Socket server
        socketClient.send(message);
        return message;
    }
}
```

---

## 4. Data Flow & Statistics

### 4.1 WPM Calculation Formulas

#### Net WPM (Displayed WPM)

**Formula:**
```
Net WPM = (Correct Characters / 5) / Time in Minutes
```

**Implementation:**
```javascript
const timeElapsed = (Date.now() - startTime) / 60000;  // Convert ms to minutes
const netWordsTyped = charStats.correct / 5;  // 5 characters = 1 word
const currentNetWpm = Math.round(netWordsTyped / timeElapsed);
```

**Example:**
- Correct characters: 250
- Time elapsed: 60 seconds (1 minute)
- Net WPM = (250 / 5) / 1 = **50 WPM**

#### Raw WPM (Includes Errors)

**Formula:**
```
Raw WPM = (Total Keystrokes / 5) / Time in Minutes
```

**Implementation:**
```javascript
const rawWordsTyped = totalTyped / 5;  // All keystrokes including errors
const currentRawWpm = Math.round(rawWordsTyped / timeElapsed);
```

**Example:**
- Total keystrokes: 300 (includes 50 errors)
- Time elapsed: 60 seconds (1 minute)
- Raw WPM = (300 / 5) / 1 = **60 WPM**

**Relationship:**
```
Raw WPM ≥ Net WPM
Difference = Error penalty
```

### 4.2 Accuracy Calculation

**Formula:**
```
Accuracy = (Correct Characters / Total Keystrokes) × 100%
```

**Implementation:**
```javascript
const accuracy = totalTyped > 0
  ? Math.min(100, (charStats.correct / totalTyped) * 100)
  : 100;
```

**Example:**
- Correct characters: 250
- Total keystrokes: 300
- Accuracy = (250 / 300) × 100 = **83.33%**

**Edge Cases:**
- No keystrokes: Accuracy = 100% (default)
- Perfect typing: Accuracy = 100% (capped)

### 4.3 Character Statistics Breakdown

```javascript
charStats = {
  correct: 250,    // Typed correctly
  incorrect: 40,   // Typed incorrectly
  missed: 10,      // Skipped by pressing space early
  extra: 0         // Typed beyond text length (removed in current version)
}

// Validation:
totalTyped = correct + incorrect + missed + extra
```

### 4.4 Second-by-Second Performance Capture

**Data Structure:**
```javascript
wpmHistory = [0, 12, 24, 35, 42, 48, 50, 51, 50, 49]
//           [0s, 1s, 2s, 3s, 4s, 5s, 6s, 7s, 8s, 9s]
```

**Capture Mechanism:**
```javascript
// WPM calculator runs every 1 second
const interval = setInterval(() => {
  const currentNetWpm = calculateNetWPM();
  
  // Append to history array for graph
  setWpmHistory(prev => [...prev, currentNetWpm]);
}, 1000);
```

**Graph Rendering:**

The `wpmHistory` array is passed to `ResultScreen.jsx` which uses **Recharts** to visualize performance:

```jsx
<LineChart data={wpmHistory.map((wpm, index) => ({ second: index, wpm }))}>
  <XAxis dataKey="second" label="Time (seconds)" />
  <YAxis label="WPM" />
  <Line type="monotone" dataKey="wpm" stroke="#facc15" />
</LineChart>
```

**Graph Features:**
- X-axis: Time in seconds
- Y-axis: WPM value
- Shows performance fluctuation over time
- Helps identify consistency and peak performance

### 4.5 Result Screen Data Flow

```
┌─────────────────────────────────────────────────────────────┐
│                    useTypingEngine.js                       │
│  - Tracks: wpm, rawWpm, accuracy, charStats, wpmHistory    │
└─────────────────────────────────────────────────────────────┘
                              ↓
                    phase === 'finished'
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                     TypingArea.jsx                          │
│  Renders: <ResultScreen                                    │
│    wpm={wpm}                                                │
│    rawWpm={rawWpm}                                          │
│    accuracy={accuracy}                                      │
│    testType={`${testType} ${testValue}`}                   │
│    characters={charStats}                                   │
│    wpmHistory={wpmHistory}                                  │
│  />                                                         │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                    ResultScreen.jsx                         │
│  Displays:                                                  │
│  - WPM (large, prominent)                                   │
│  - Raw WPM (smaller, secondary)                             │
│  - Accuracy percentage                                      │
│  - Test type ("time 30" or "words 50")                      │
│  - Character breakdown (correct/incorrect/missed)           │
│  - WPM graph (Recharts LineChart)                           │
└─────────────────────────────────────────────────────────────┘
                              ↓
                    POST /api/scores
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                  ScoreManager.java                          │
│  - Saves to scores.dat via ObjectOutputStream              │
│  - Updates UserStats (best WPM, average, history)          │
└─────────────────────────────────────────────────────────────┘
```

---

## 5. Setup & Running

### 5.1 Prerequisites

Ensure you have the following installed:

- **Java 17+** - `java -version`
- **Maven 3.9+** - `mvn -version`
- **Node.js 18+** - `node -v`
- **npm 9+** - `npm -v`

### 5.2 Installation Steps

#### Step 1: Clone Repository
```bash
git clone <repository-url>
cd Fast-Fingers
```

#### Step 2: Backend Setup
```bash
# Navigate to backend directory
cd backend

# Install dependencies and compile
mvn clean install

# Verify installation
mvn dependency:tree
```

#### Step 3: Frontend Setup
```bash
# Navigate to frontend directory
cd ../frontend

# Install dependencies
npm install

# Verify installation
npm list
```

### 5.3 Running the Application

#### Option 1: Development Mode (Recommended)

**Terminal 1 - Backend Server:**
```bash
cd backend
mvn spring-boot:run
```
✅ Backend running on: `http://localhost:8080`

**Terminal 2 - Frontend Server:**
```bash
cd frontend
npm run dev
```
✅ Frontend running on: `http://localhost:5173`

**Access Application:** Open browser to `http://localhost:5173`

#### Option 2: Production Build

**Build Frontend:**
```bash
cd frontend
npm run build
# Creates optimized build in frontend/dist
```

**Run Backend (serves frontend):**
```bash
cd backend
mvn spring-boot:run
```
✅ Application running on: `http://localhost:8080`

### 5.4 Running Multiplayer Server (Optional)

**Start Socket Server:**
```bash
cd backend
mvn exec:java -Dexec.mainClass="com.typinggame.network.GameServer"
```
✅ Socket server listening on: `ws://localhost:9090`

**Connect Clients:**
- Open multiple browser tabs to `http://localhost:5173`
- Click "MULTIPLAYER" button
- Enter room code to join match

### 5.5 Verifying Installation

#### Test Backend API:
```bash
# Test word retrieval
curl http://localhost:8080/api/game/text?lang=english&count=10

# Test code snippet
curl http://localhost:8080/api/game/text?lang=java
```

#### Test Frontend:
1. Open `http://localhost:5173`
2. Click typing area to focus
3. Start typing
4. Verify WPM updates in real-time
5. Press Tab to restart

### 5.6 Project Structure Reference

```
Fast-Fingers/
├── backend/
│   ├── src/main/java/com/typinggame/
│   │   ├── api/
│   │   │   └── TypingController.java      # REST endpoints
│   │   ├── engine/
│   │   │   ├── TypingEngine.java          # Word bank (ArrayList)
│   │   │   └── PerformanceTracker.java    # Stack/Queue/Set
│   │   ├── io/
│   │   │   ├── ScoreManager.java          # ObjectOutputStream/InputStream
│   │   │   └── CodeSnippetLoader.java     # FileReader
│   │   ├── network/
│   │   │   ├── GameServer.java            # Socket server
│   │   │   └── ClientHandler.java         # Thread-per-client
│   │   └── websocket/
│   │       └── GameWebSocketController.java
│   └── pom.xml
│
├── frontend/
│   ├── src/
│   │   ├── hooks/
│   │   │   ├── useTypingEngine.js         # Core typing logic
│   │   │   └── useGameConfig.js           # Mode configuration
│   │   ├── components/game/
│   │   │   ├── TypingArea.jsx             # Main game component
│   │   │   ├── ResultScreen.jsx           # Results display
│   │   │   └── ModeToolbar.jsx            # Mode selection
│   │   └── App.jsx
│   └── package.json
│
└── README.md
```

---

## 📊 Key Metrics Summary

| Metric | Formula | Purpose |
|--------|---------|---------|
| **Net WPM** | `(Correct Chars / 5) / Minutes` | Effective typing speed |
| **Raw WPM** | `(Total Keystrokes / 5) / Minutes` | Total typing effort |
| **Accuracy** | `(Correct / Total) × 100%` | Typing precision |
| **Missed Chars** | Count when space pressed early | Word skip penalty |

---

## 🎓 AOOP Requirements Checklist

- ✅ **Req 1:** Inheritance & Polymorphism - `Word implements Comparable<Word>`
- ✅ **Req 2:** Collections - ArrayList (word bank), Stack (backspace), HashSet (unique words), Queue (upcoming words)
- ✅ **Req 3:** File I/O - FileReader (snippets), ObjectOutputStream (stats), ObjectInputStream (load stats), FileWriter (history)
- ✅ **Req 4:** Threading - Timer thread, WPM calculator thread, ClientHandler threads
- ✅ **Req 5:** Socket Programming - GameServer, ClientHandler, Socket-to-WebSocket bridge

---

**Happy Typing! ⌨️**
