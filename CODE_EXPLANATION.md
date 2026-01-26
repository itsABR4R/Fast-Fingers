# FastFingers - Code Explanation Guide

> **Detailed code snippets and explanations for key components: Threading, Multiplayer, and Spring Boot**

---

## Table of Contents

1. [File Paths Reference](#file-paths-reference)
2. [Threading Implementation](#1-threading-implementation)
3. [Multiplayer System](#2-multiplayer-system)
4. [Spring Boot Backend](#3-spring-boot-backend)
5. [MongoDB Integration](#4-mongodb-integration)

---

## File Paths Reference

Quick reference to all files mentioned in this guide:

### Frontend Files

**Hooks (Custom React Hooks)**
- `frontend/src/hooks/useTypingEngine.js` - Main typing engine with WPM calculator and timer threads
- `frontend/src/hooks/useGameSocket.js` - WebSocket connection management for multiplayer
- `frontend/src/hooks/useGameConfig.js` - Game configuration state management

**Components**
- `frontend/src/components/game/TypingArea.jsx` - Main typing interface for practice/code modes
- `frontend/src/components/game/MultiplayerArea.jsx` - Multiplayer typing interface with ghost carets
- `frontend/src/components/game/ResultScreen.jsx` - Results display after game completion
- `frontend/src/components/game/GhostCaret.jsx` - Ghost caret component for showing other players
- `frontend/src/components/layout/Navbar.jsx` - Navigation bar component

**Pages**
- `frontend/src/pages/HomePage.jsx` - Main landing page
- `frontend/src/pages/Login.jsx` - Login and signup page
- `frontend/src/pages/ProfilePage.jsx` - User profile and statistics page
- `frontend/src/pages/GameRoom.jsx` - Multiplayer room page

**Services**
- `frontend/src/services/authService.js` - Authentication utilities (login, signup, logout)
- `frontend/src/services/api.js` - Axios HTTP client configuration
- `frontend/src/services/socket.js` - WebSocket client setup

**Main Files**
- `frontend/src/App.jsx` - Main application component with routing
- `frontend/src/main.jsx` - Application entry point

### Backend Files

**Controllers (REST API Endpoints)**
- `src/main/java/com/typinggame/api/AuthController.java` - Authentication endpoints (signup, login)
- `src/main/java/com/typinggame/api/ProfileController.java` - User profile and game history endpoints
- `src/main/java/com/typinggame/api/TypingController.java` - Game text generation and score saving
- `src/main/java/com/typinggame/api/GameWebSocketController.java` - WebSocket endpoints for multiplayer

**Services (Business Logic)**
- `src/main/java/com/typinggame/service/AuthService.java` - Authentication business logic

**Domain Models (MongoDB Documents)**
- `src/main/java/com/typinggame/domain/User.java` - User model with statistics
- `src/main/java/com/typinggame/domain/GameRecord.java` - Game record model

**Repositories (Database Access)**
- `src/main/java/com/typinggame/repository/UserRepository.java` - User database queries
- `src/main/java/com/typinggame/repository/GameRecordRepository.java` - Game record database queries

**Configuration**
- `src/main/resources/application.properties` - MongoDB and server configuration
- `src/main/java/com/typinggame/TypingGameApplication.java` - Spring Boot main application

**Build Configuration**
- `pom.xml` - Maven dependencies and build configuration

---

## 1. Threading Implementation

### 1.1 WPM Calculator Thread (Frontend)

**Location:** `frontend/src/hooks/useTypingEngine.js`

This thread runs in the background to calculate WPM every 200 milliseconds.

```javascript
// WPM Calculator with history tracking (more responsive updates)
useEffect(() => {
    if (phase === "typing" && startTime) {
        const interval = setInterval(() => {
            const timeElapsed = (Date.now() - startTime) / 60000; // in minutes

            // Only calculate if at least 0.5 seconds have passed to avoid division issues
            if (timeElapsed < 0.0083) return; // 0.0083 minutes = 0.5 seconds

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
            
            // Only add to history every second for chart (to avoid too many data points)
            const secondsElapsed = Math.floor((Date.now() - startTime) / 1000);
            if (wpmHistory.length < secondsElapsed) {
                setWpmHistory(prev => [...prev, currentNetWpm]);
            }
        }, 200); // Update every 200ms for smoother, more responsive display
        
        return () => clearInterval(interval);
    }
}, [userInput, startTime, phase, charStats, totalTyped, wpmHistory.length]);
```

**How it works:**

1. **Thread Creation**: `setInterval()` creates a background thread that runs every 200ms
2. **Time Calculation**: Gets elapsed time since typing started
3. **Safety Check**: Waits at least 0.5 seconds before calculating to avoid division by very small numbers
4. **WPM Formula**: 
   - Net WPM = (Correct Characters ÷ 5) ÷ Time in Minutes
   - Raw WPM = (Total Keystrokes ÷ 5) ÷ Time in Minutes
5. **State Updates**: Updates WPM values in React state
6. **History Tracking**: Adds WPM snapshot every second for the performance graph
7. **Cleanup**: `clearInterval()` stops the thread when component unmounts

**Why 200ms?**
- Fast enough to feel real-time (updates 5 times per second)
- Not too fast to cause performance issues
- Smooth visual experience for the user

---

### 1.2 Timer Countdown Thread (Frontend)

**Location:** `frontend/src/hooks/useTypingEngine.js`

This thread counts down the timer in time-based mode.

```javascript
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
```

**How it works:**

1. **Conditions**: Only runs if:
   - Test type is "time"
   - User is currently typing
   - Time remaining > 0
   - Not infinite mode (testValue > 0)

2. **Thread Creation**: `setInterval()` runs every 1000ms (1 second)

3. **Countdown Logic**:
   - Decrements `timeRemaining` by 1 each second
   - When timer hits 0, sets phase to "finished"
   - Game automatically ends

4. **Cleanup**: Stops the timer when:
   - Component unmounts
   - User finishes early
   - Mode changes

**Example Timeline:**
```
30 seconds → 29 → 28 → ... → 1 → 0 → GAME OVER
```

---

### 1.3 Thread Coordination

Both threads run **concurrently** (at the same time):

```
Time: 0s    1s    2s    3s    4s    5s
      |     |     |     |     |     |
Timer: 30 → 29 → 28 → 27 → 26 → 25  (runs every 1000ms)
      |||||||||||||||||||||||||||||
WPM:  ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑  (runs every 200ms)
      0  12  24  35  42  48  50  51
```

**Key Points:**
- WPM thread updates 5 times faster than timer
- Both threads are independent
- React handles state updates safely
- No race conditions because React batches updates

---

## 2. Multiplayer System

### 2.1 WebSocket Connection Hook

**Location:** `frontend/src/hooks/useGameSocket.js`

This custom hook manages the WebSocket connection for multiplayer.

```javascript
const useGameSocket = (roomId, username) => {
  const [messages, setMessages] = useState([]);
  const [players, setPlayers] = useState([]); // List of { username, progress, status }
  const [isConnected, setIsConnected] = useState(false);
  const [startText, setStartText] = useState(null);
  const [winner, setWinner] = useState(null);
  
  const clientRef = useRef(null);

  useEffect(() => {
    if (!roomId || !username) return;

    // 1. Initialize Client
    const client = createStompClient(
      () => {
        setIsConnected(true);
        subscribeToRoom();
      },
      (err) => console.error("Socket Error", err)
    );

    clientRef.current = client;
    client.activate();

    // 2. Subscribe Logic
    const subscribeToRoom = () => {
      // Listen for Game Updates (Progress, Elimination)
      client.subscribe(`/topic/game/${roomId}`, (message) => {
        const data = JSON.parse(message.body);

        if (data.type === 'START') {
          setStartText(data.text);
          setWinner(null);
        }

        if (data.type === 'PLAYER_UPDATE') {
          // Backend sends: { type: 'PLAYER_UPDATE', players: [...] }
          if (Array.isArray(data.players)) {
            setPlayers(data.players);
          }
        }

        if (data.type === 'FINISH') {
          setWinner(data.winner);
        }
      });

      // Notify server I have joined
      client.publish({
        destination: `/app/join/${roomId}`,
        body: JSON.stringify({ username })
      });
    };

    // Cleanup on Unmount
    return () => {
      if (client.active) client.deactivate();
    };
  }, [roomId, username]);

  // Actions exposed to UI
  const sendProgress = (wpm, progressPercentage) => {
    if (clientRef.current?.connected) {
      clientRef.current.publish({
        destination: `/app/progress/${roomId}`,
        body: JSON.stringify({ username, wpm, progress: progressPercentage })
      });
    }
  };

  const sendFinish = ({ wpm, accuracy, wordsTyped, duration }) => {
    if (clientRef.current?.connected) {
      clientRef.current.publish({
        destination: `/app/finish/${roomId}`,
        body: JSON.stringify({ username, wpm, accuracy, wordsTyped, duration })
      });
    }
  };

  return { isConnected, players, messages, startText, winner, sendProgress, sendFinish };
};
```

**How it works:**

1. **Connection Setup**:
   - Creates STOMP client (WebSocket protocol)
   - Connects to backend WebSocket server
   - Sets `isConnected` to true when ready

2. **Subscription**:
   - Subscribes to `/topic/game/{roomId}` to receive updates
   - Listens for three message types:
     - `START`: Game begins, receive text to type
     - `PLAYER_UPDATE`: Other players' progress
     - `FINISH`: Someone won the race

3. **Joining Room**:
   - Sends username to `/app/join/{roomId}`
   - Server adds player to room
   - Broadcasts to other players

4. **Sending Progress**:
   - Every 150ms, sends current WPM and progress %
   - Server broadcasts to all players in room
   - Other players see your ghost caret move

5. **Cleanup**:
   - Disconnects when component unmounts
   - Prevents memory leaks

**Message Flow:**
```
Player 1                Server                Player 2
   |                      |                      |
   |--- JOIN (room_1) --->|                      |
   |                      |<--- JOIN (room_1) ---|
   |<-- START (text) -----|---- START (text) --->|
   |                      |                      |
   |-- PROGRESS (45%) --->|                      |
   |                      |--- PROGRESS (45%) -->|
   |                      |<-- PROGRESS (38%) ---|
   |<- PROGRESS (38%) ----|                      |
   |                      |                      |
   |--- FINISH (100%) --->|                      |
   |<--- WINNER (P1) -----|---- WINNER (P1) ---->|
```

---

### 2.2 Ghost Caret Component

**Location:** `frontend/src/components/game/GhostCaret.jsx`

This component shows where other players are typing.

```javascript
const GhostCaret = ({ text, progress, name }) => {
  const [position, setPosition] = useState({ left: 0, top: 0 });
  const charRefs = useRef([]);

  // Calculate position based on progress
  useEffect(() => {
    const charIndex = Math.min(progress, text.length - 1);
    const charEl = charRefs.current[charIndex];
    
    if (charEl) {
      const rect = charEl.getBoundingClientRect();
      setPosition({
        left: rect.left,
        top: rect.top
      });
    }
  }, [progress, text]);

  return (
    <>
      {/* Invisible character references */}
      <div style={{ position: 'absolute', visibility: 'hidden' }}>
        {text.split('').map((char, i) => (
          <span key={i} ref={el => charRefs.current[i] = el}>
            {char}
          </span>
        ))}
      </div>

      {/* Ghost caret */}
      <div
        className="absolute z-10 bg-blue-400 opacity-60"
        style={{
          left: `${position.left}px`,
          top: `${position.top}px`,
          width: '2px',
          height: '2rem',
          transition: 'left 0.15s ease-out, top 0.15s ease-out'
        }}
      >
        {/* Player name label */}
        <div className="absolute -top-6 left-0 text-xs text-blue-400 whitespace-nowrap">
          {name}
        </div>
      </div>
    </>
  );
};
```

**How it works:**

1. **Character Mapping**:
   - Creates invisible `<span>` elements for each character
   - Stores references in `charRefs` array
   - Used to calculate exact positions

2. **Position Calculation**:
   - Converts progress % to character index
   - Gets position of that character element
   - Updates ghost caret position

3. **Smooth Animation**:
   - CSS transition: `0.15s ease-out`
   - Caret smoothly moves as player types
   - Feels natural and responsive

4. **Visual Design**:
   - Blue color with 60% opacity
   - Player name shown above caret
   - 2px wide, 2rem tall (same as real caret)

**Example:**
```
Progress: 45% of 100 characters = character index 45

Text: "hello world this is a test..."
       ^                              (index 0)
                    ^                 (index 45 - ghost caret here)
```

---

### 2.3 Multiplayer Score Saving

**Location:** `frontend/src/components/game/MultiplayerArea.jsx`

Automatically saves scores when multiplayer game finishes.

```javascript
// Save score to backend when finished (multiplayer)
useEffect(() => {
  if (phase === "finished" && !hasSentFinishRef.current && startText) {
    const saveScore = async () => {
      try {
        // Get current user from localStorage
        const userStr = localStorage.getItem('user');
        let currentUsername = null;
        let userId = null;
        
        if (userStr) {
          try {
            const user = JSON.parse(userStr);
            if (user && !user.isGuest) {
              currentUsername = user.username;
              userId = user.id;
            }
          } catch (e) {
            console.error("Error parsing user data:", e);
          }
        }

        // Only save if user is logged in
        if (currentUsername && userId) {
          const isWinner = winner === currentUsername;
          
          const scoreData = {
            username: currentUsername,
            userId: userId,
            mode: "MULTIPLAYER",
            wpm: wpm,
            accuracy: accuracy,
            wordsTyped: wordsCount,
            duration: 0,
            isWin: isWinner
          };

          await fetch('http://localhost:8080/api/scores', {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
            },
            body: JSON.stringify(scoreData)
          });

          console.log(`Multiplayer score saved for ${currentUsername}`);
        }
      } catch (error) {
        console.error("Failed to save multiplayer score:", error);
      }
    };

    saveScore();
  }
}, [phase, wpm, accuracy, wordsCount, winner, username, startText]);
```

**How it works:**

1. **Trigger**: Runs when `phase` becomes "finished"
2. **User Check**: Gets logged-in user from localStorage
3. **Guest Filter**: Only saves if user is not a guest
4. **Winner Detection**: Checks if this player won (`winner === currentUsername`)
5. **Score Data**: Creates object with:
   - Username and userId
   - Mode: "MULTIPLAYER"
   - WPM and accuracy
   - Win status (true/false)
6. **API Call**: Sends to backend `/api/scores` endpoint
7. **Backend Processing**: Updates user stats and saves to MongoDB

---

## 3. Spring Boot Backend

### 3.1 Authentication Controller

**Location:** `src/main/java/com/typinggame/api/AuthController.java`

Handles user signup and login.

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/auth/signup - Register a new user
     */
    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@RequestBody Map<String, String> requestBody) {
        Map<String, Object> response = new HashMap<>();

        try {
            String username = requestBody.get("username");
            String email = requestBody.get("email");
            String password = requestBody.get("password");

            User user = authService.register(username, email, password);

            response.put("success", true);
            response.put("message", "Account created successfully");

            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("username", user.getUsername());
            userData.put("email", user.getEmail());
            response.put("user", userData);

            System.out.println("[AuthController] User registered: " + username);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error creating account: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * POST /api/auth/login - Login user
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> requestBody) {
        Map<String, Object> response = new HashMap<>();

        try {
            String username = requestBody.get("username");
            String password = requestBody.get("password");

            User user = authService.login(username, password);

            if (user != null) {
                response.put("success", true);
                response.put("message", "Login successful");

                Map<String, Object> userData = new HashMap<>();
                userData.put("id", user.getId());
                userData.put("username", user.getUsername());
                userData.put("email", user.getEmail());
                response.put("user", userData);

                System.out.println("[AuthController] User logged in: " + username);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Invalid username or password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error during login: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
```

**How it works:**

**Signup Flow:**
1. **Receive Request**: Frontend sends JSON with username, email, password
2. **Extract Data**: Get values from request body
3. **Call Service**: `authService.register()` validates and creates user
4. **Handle Errors**: 
   - `IllegalArgumentException`: Username/email exists → 400 Bad Request
   - Other errors → 500 Internal Server Error
5. **Success Response**: Return user data with 200 OK

**Login Flow:**
1. **Receive Request**: Frontend sends username and password
2. **Call Service**: `authService.login()` checks credentials
3. **Validation**: Returns user if password matches, null otherwise
4. **Response**:
   - Valid: Return user data with 200 OK
   - Invalid: Return error with 401 Unauthorized

**Annotations Explained:**
- `@RestController`: Marks class as REST API controller
- `@RequestMapping("/api/auth")`: Base URL for all endpoints
- `@PostMapping("/signup")`: Maps POST requests to `/api/auth/signup`
- `@RequestBody`: Converts JSON to Java Map
- `@Autowired`: Injects AuthService dependency

---

### 3.2 Authentication Service

**Location:** `src/main/java/com/typinggame/service/AuthService.java`

Business logic for user authentication.

```java
@Service
public class AuthService {

    private final UserRepository userRepository;

    @Autowired
    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Register a new user.
     * 
     * @return User object if successful
     * @throws IllegalArgumentException if validation fails
     */
    public User register(String username, String email, String password) {
        // Validate inputs
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        // Check if username already exists
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Create and save new user
        User user = new User(username, email, password);
        return userRepository.save(user);
    }

    /**
     * Login user with username and password.
     * 
     * @return User object if credentials are valid, null otherwise
     */
    public User login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Simple plain text password comparison
            if (user.getPassword().equals(password)) {
                return user;
            }
        }

        return null;
    }

    /**
     * Get user profile by username.
     */
    public User getUserProfile(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    /**
     * Update user statistics.
     */
    public User updateUser(User user) {
        return userRepository.save(user);
    }
}
```

**How it works:**

**Register Method:**
1. **Input Validation**: Checks if username, email, password are not empty
2. **Uniqueness Check**: 
   - Queries MongoDB to see if username exists
   - Queries MongoDB to see if email exists
3. **User Creation**: Creates new User object with provided data
4. **Save to Database**: `userRepository.save()` inserts into MongoDB
5. **Return**: Returns saved user object with generated ID

**Login Method:**
1. **Find User**: Queries MongoDB by username
2. **Check Existence**: If user not found, return null
3. **Password Check**: Simple string comparison (plain text)
4. **Return**: User object if valid, null if invalid

**Note:** This is a **simple implementation** for educational purposes. In production:
- Use password hashing (BCrypt)
- Use JWT tokens for authentication
- Add rate limiting
- Add email verification

---

### 3.3 Profile Controller

**Location:** `src/main/java/com/typinggame/api/ProfileController.java`

Handles user profile and game history endpoints.

```java
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final AuthService authService;
    private final GameRecordRepository gameRecordRepository;

    @Autowired
    public ProfileController(AuthService authService, GameRecordRepository gameRecordRepository) {
        this.authService = authService;
        this.gameRecordRepository = gameRecordRepository;
    }

    /**
     * GET /api/profile/{username} - Get user profile and statistics
     */
    @GetMapping("/{username}")
    public ResponseEntity<Map<String, Object>> getUserProfile(@PathVariable String username) {
        try {
            User user = authService.getUserProfile(username);

            if (user == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("createdAt", user.getCreatedAt());
            response.put("totalGames", user.getTotalGames());
            response.put("bestWPM", user.getBestWPM());
            response.put("avgWPM", user.getAvgWPM());
            response.put("totalWins", user.getTotalWins());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error fetching profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * GET /api/profile/{username}/history - Get user's game history
     */
    @GetMapping("/{username}/history")
    public ResponseEntity<Map<String, Object>> getGameHistory(@PathVariable String username) {
        try {
            User user = authService.getUserProfile(username);

            if (user == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            List<GameRecord> gameRecords = gameRecordRepository.findTop20ByUserIdOrderByTimestampDesc(user.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("username", username);
            response.put("totalRecords", gameRecords.size());
            response.put("games", gameRecords);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error fetching game history: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
```

**How it works:**

**Get Profile Endpoint:**
1. **Extract Username**: `@PathVariable` gets username from URL
2. **Query Database**: Find user by username
3. **Check Existence**: Return 404 if user not found
4. **Build Response**: Create map with all user statistics
5. **Return**: Send JSON response with 200 OK

**Get History Endpoint:**
1. **Find User**: Query by username
2. **Query Game Records**: 
   - `findTop20ByUserIdOrderByTimestampDesc(userId)`
   - Gets last 20 games
   - Sorted by newest first
3. **Build Response**: Include games array
4. **Return**: Send JSON with game history

**URL Examples:**
```
GET /api/profile/speedtyper
→ Returns user stats

GET /api/profile/speedtyper/history
→ Returns last 20 games
```

---

## 4. MongoDB Integration

### 4.1 User Domain Model

**Location:** `src/main/java/com/typinggame/domain/User.java`

```java
@Document(collection = "users")
public class User {
    
    @Id
    private String id;
    
    private String username;
    private String email;
    private String password;
    private LocalDateTime createdAt;
    
    // Statistics
    private int totalGames = 0;
    private double bestWPM = 0.0;
    private double avgWPM = 0.0;
    private int totalWins = 0;
    
    public User() {
        this.createdAt = LocalDateTime.now();
    }
    
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.createdAt = LocalDateTime.now();
    }
    
    /**
     * Update user statistics after a game
     */
    public void updateStats(double wpm, boolean isWin) {
        totalGames++;
        
        // Update best WPM if this game was better
        if (wpm > bestWPM) {
            bestWPM = wpm;
        }
        
        // Calculate new average WPM
        // Formula: (old_avg * old_count + new_wpm) / new_count
        avgWPM = ((avgWPM * (totalGames - 1)) + wpm) / totalGames;
        
        // Increment wins if player won
        if (isWin) {
            totalWins++;
        }
    }
    
    // Getters and setters...
}
```

**How it works:**

**Annotations:**
- `@Document(collection = "users")`: Maps to MongoDB "users" collection
- `@Id`: Marks field as MongoDB document ID (auto-generated)

**Constructor:**
- Sets `createdAt` to current timestamp
- Initializes statistics to 0

**updateStats Method:**
1. **Increment Games**: `totalGames++`
2. **Update Best**: If new WPM > old best, replace it
3. **Calculate Average**: 
   - Multiply old average by old count
   - Add new WPM
   - Divide by new count
4. **Update Wins**: Increment if `isWin` is true

**Example:**
```
Game 1: WPM = 50
→ totalGames = 1, avgWPM = 50, bestWPM = 50

Game 2: WPM = 70
→ totalGames = 2, avgWPM = 60, bestWPM = 70
  Calculation: (50 * 1 + 70) / 2 = 60

Game 3: WPM = 65
→ totalGames = 3, avgWPM = 61.67, bestWPM = 70
  Calculation: (60 * 2 + 65) / 3 = 61.67
```

---

### 4.2 User Repository

**Location:** `src/main/java/com/typinggame/repository/UserRepository.java`

```java
public interface UserRepository extends MongoRepository<User, String> {
    
    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
}
```

**How it works:**

**Spring Data Magic:**
- Spring automatically implements these methods
- Method names follow naming convention
- No need to write SQL or MongoDB queries

**Method Breakdown:**

1. **findByUsername**:
   - Spring generates: `db.users.findOne({ username: "value" })`
   - Returns `Optional<User>` (may be empty)

2. **existsByUsername**:
   - Spring generates: `db.users.count({ username: "value" }) > 0`
   - Returns boolean (true/false)

**Usage Example:**
```java
// Check if username exists
boolean exists = userRepository.existsByUsername("speedtyper");

// Find user
Optional<User> userOpt = userRepository.findByUsername("speedtyper");
if (userOpt.isPresent()) {
    User user = userOpt.get();
    System.out.println(user.getEmail());
}

// Save user
User newUser = new User("john", "john@example.com", "pass123");
userRepository.save(newUser);
```

---

### 4.3 Score Saving with MongoDB

**Location:** `src/main/java/com/typinggame/api/TypingController.java`

```java
@PostMapping("/scores")
public ResponseEntity<Map<String, Object>> saveScore(@RequestBody Map<String, Object> scoreData) {
    Map<String, Object> response = new HashMap<>();

    try {
        String username = (String) scoreData.get("username");
        String userId = (String) scoreData.get("userId");
        
        // Only save if user is logged in
        if (username == null || userId == null) {
            response.put("success", false);
            response.put("message", "Guest scores are not saved");
            return ResponseEntity.ok(response);
        }

        // Extract score data
        String mode = (String) scoreData.get("mode");
        double wpm = ((Number) scoreData.get("wpm")).doubleValue();
        double accuracy = ((Number) scoreData.get("accuracy")).doubleValue();
        int wordsTyped = ((Number) scoreData.get("wordsTyped")).intValue();
        boolean isWin = scoreData.containsKey("isWin") ? (boolean) scoreData.get("isWin") : false;

        // Create game record
        GameRecord record = new GameRecord();
        record.setUserId(userId);
        record.setUsername(username);
        record.setWpm(wpm);
        record.setAccuracy(accuracy);
        record.setWordsTyped(wordsTyped);
        record.setGameMode(mode);
        record.setTimestamp(LocalDateTime.now());

        // Save to database
        gameRecordRepository.save(record);

        // Update user statistics
        User user = authService.getUserById(userId);
        if (user != null) {
            user.updateStats(wpm, isWin);
            authService.updateUser(user);
        }

        response.put("success", true);
        response.put("message", "Score saved successfully");
        System.out.println("[TypingController] Score saved for " + username);

        return ResponseEntity.ok(response);

    } catch (Exception e) {
        response.put("success", false);
        response.put("message", "Error saving score: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
```

**How it works:**

1. **Receive Score Data**: Frontend sends JSON with game results
2. **Guest Check**: If no username/userId, don't save (guest mode)
3. **Extract Data**: Get WPM, accuracy, mode, etc. from request
4. **Create GameRecord**:
   - New document for this specific game
   - Includes all game details
   - Timestamp set to now
5. **Save Record**: Insert into `game_records` collection
6. **Update User Stats**:
   - Find user by ID
   - Call `user.updateStats(wpm, isWin)`
   - Save updated user document
7. **Response**: Return success message

**MongoDB Operations:**
```javascript
// 1. Insert game record
db.game_records.insertOne({
  userId: "507f1f77bcf86cd799439011",
  username: "speedtyper",
  wpm: 85.2,
  accuracy: 96.5,
  gameMode: "MULTIPLAYER",
  timestamp: ISODate("2026-01-26T12:30:00Z")
})

// 2. Update user stats
db.users.updateOne(
  { _id: "507f1f77bcf86cd799439011" },
  { 
    $set: { 
      totalGames: 16,
      bestWPM: 95.5,
      avgWPM: 78.8,
      totalWins: 6
    }
  }
)
```

---

## Summary

This document covered the three main technical areas:

### Threading
- **WPM Calculator**: Updates every 200ms for real-time feedback
- **Timer Countdown**: Runs every 1 second, ends game at 0
- **Concurrent Execution**: Both threads run independently

### Multiplayer
- **WebSocket Connection**: Real-time communication with server
- **Ghost Carets**: Visual representation of other players
- **Score Saving**: Automatic save with win detection

### Spring Boot
- **REST Controllers**: Handle HTTP requests (signup, login, profile)
- **Services**: Business logic (authentication, validation)
- **MongoDB Integration**: Automatic query generation, document mapping
- **Statistics Tracking**: Real-time updates to user stats

All components work together to create a seamless, real-time typing game experience!
