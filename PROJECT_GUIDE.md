# FastFingers - Complete Project Guide

> **A modern typing game with user accounts, multiplayer racing, and real-time statistics**

---

## 📖 Table of Contents

1. [What is FastFingers?](#1-what-is-fastfingers)
2. [Key Features](#2-key-features)
3. [How It Works](#3-how-it-works)
4. [Technical Architecture](#4-technical-architecture)
5. [Setup & Running](#5-setup--running)

---

## 1. What is FastFingers?

FastFingers is a **typing speed game** where you can:
- Practice typing to improve your speed and accuracy
- Create an account to track your progress over time
- Race against other players in real-time multiplayer mode
- Type code snippets to practice programming
- View detailed statistics and game history

Think of it like a racing game, but instead of driving, you're typing!

---

## 2. Key Features

### 🎮 Game Modes

#### Practice Mode (Time-Based)
- Choose how long you want to type: **15, 30, 60 seconds, or unlimited**
- Type random words as fast as you can
- See your WPM (Words Per Minute) update in real-time
- Get instant feedback on mistakes (red = wrong, white = correct)

#### Practice Mode (Word-Based)
- Choose how many words to type: **10, 25, 50, or 100 words**
- Perfect for quick practice sessions
- Game ends when you finish all the words

#### Code Mode
- Practice typing real code snippets (Java, Python, JavaScript)
- **Smart indentation**: When you press Enter, the cursor automatically jumps to the right position
- **Newline enforcement**: You must press Enter to go to the next line (just like real coding!)
- Great for programmers who want to improve their coding speed

#### Multiplayer Mode (Race Track)
- Race against other players in real-time
- See **ghost carets** showing where other players are typing
- First person to finish wins! 🏆
- Your username appears on the race track
- Guests get random names like "Guest_742"

### 👤 User Accounts & Profiles

#### Sign Up & Login
- Create an account with username, email, and password
- Or play as a **guest** without creating an account
- Simple and fast authentication

#### User Profile Page
Shows your typing statistics:
- **Average WPM**: Your typical typing speed
- **Best WPM**: Your personal record
- **Total Games**: How many games you've played
- **Total Wins**: How many multiplayer races you've won
- **Game History**: See your last 20 games with details (date, mode, WPM, accuracy)

#### Guest Mode
- Click "Continue as Guest" to play without an account
- Your scores won't be saved to the database
- Perfect for trying out the game

### 📊 Real-Time Statistics

#### Live WPM Counter
- Updates **every 200 milliseconds** (super fast!)
- Starts calculating after just **0.5 seconds** of typing
- Shows your current typing speed as you type

#### Accuracy Tracking
- Shows percentage of correct characters
- Formula: `(Correct Characters / Total Typed) × 100%`
- Updates in real-time

#### Character Feedback
- **White text** = Typed correctly ✓
- **Red text** = Typed incorrectly ✗
- **Gray text** = Not typed yet
- Skipped words are counted as "missed" but don't turn red

#### Performance Graph
- See your WPM over time in a line chart
- Yellow line shows your speed throughout the test
- Helps you see if you're getting faster or slower

### 🏁 Multiplayer Features

#### Real-Time Racing
- Connect to a game room (e.g., "room_1")
- Wait for 2+ players to join
- Everyone types the same text
- See other players' progress in real-time

#### Ghost Carets
- **What they are**: Visual indicators showing where other players are typing
- Each player has a different colored ghost caret
- Shows their username above the caret
- Moves as they type, so you can see who's ahead

#### Race Progress Bar
- Top of the screen shows all players
- Your name is highlighted
- Progress bars show how far everyone has typed
- Updates smoothly as you race

#### Winner Announcement
- First player to finish wins
- Winner's name displayed with a trophy 🏆
- Your final WPM and accuracy shown
- Option to practice again in the same room

### 💾 Data Persistence (MongoDB)

#### What Gets Saved
When you complete a game while logged in:
- Your WPM and accuracy
- Number of words typed
- Game mode (Practice, Code, or Multiplayer)
- Whether you won (for multiplayer)
- Timestamp of when you played

#### Automatic Stats Updates
- **Best WPM**: Automatically updated if you beat your record
- **Average WPM**: Calculated across all your games
- **Total Games**: Increments with each game
- **Total Wins**: Increments when you win multiplayer races

#### Game History
- Last 20 games are saved
- Shows: Date, Mode, WPM, Accuracy, Words Typed
- Sorted by most recent first

### 🎨 User Interface Features

#### Responsive Design
- Works on desktop and laptop screens
- Clean, modern dark theme
- Smooth animations and transitions

#### Visual Feedback
- Typing cursor blinks in yellow
- Smooth cursor movement as you type
- Blur effect when not focused (click to focus)
- Loading skeletons while data loads

#### Navigation
- **Home**: Main typing area
- **Practice**: Same as home
- **Code Mode**: Programming practice
- **Multiplayer**: Race against others
- **Profile**: View your stats (click user icon)
- **Login/Logout**: Top right corner

#### Result Screen
After completing a test, you see:
- Large WPM and Accuracy display
- Test type (e.g., "time 15")
- Character breakdown (correct/incorrect/missed)
- Consistency percentage
- Performance graph
- Buttons: "Next Test" and "Restart Test"

---

## 3. How It Works

### The Typing Engine

#### Character-by-Character Tracking
Every letter you type is tracked individually:
1. You press a key
2. The system checks if it matches the expected character
3. The character is marked as correct (white) or incorrect (red)
4. Statistics are updated (correct count, incorrect count, total typed)
5. WPM is recalculated

#### Smart Backspace
When you press backspace:
- The last character is removed
- Its state resets to "not typed yet" (gray)
- Statistics are updated (decrements correct/incorrect count)
- In code mode, backspace after auto-indent removes the entire indent at once

#### Word Skipping (Practice Mode)
If you press **Space** before finishing a word:
- Remaining characters in that word are counted as "missed"
- The word doesn't turn red (just tracked in stats)
- You move on to the next word
- Accuracy is affected by missed characters

### WPM Calculation

#### Formula
```
WPM = (Correct Characters ÷ 5) ÷ Time in Minutes
```

**Why divide by 5?**
- Standard typing measurement: 1 word = 5 characters
- Example: "hello" = 5 characters = 1 word

#### Example
- You type 250 correct characters in 60 seconds (1 minute)
- WPM = (250 ÷ 5) ÷ 1 = **50 WPM**

#### Real-Time Updates
- Calculated every 200ms (5 times per second)
- Minimum 0.5 seconds before showing a value
- Updates smoothly as you type

### Accuracy Calculation

#### Formula
```
Accuracy = (Correct Characters ÷ Total Typed) × 100%
```

#### Example
- Correct: 250 characters
- Total typed: 300 characters (includes 50 mistakes)
- Accuracy = (250 ÷ 300) × 100 = **83.3%**

### Code Mode Mechanics

#### Auto-Indentation
When typing code, indentation is handled automatically:

**Example:**
```java
public class Example {
    public void hello() {
        System.out.println("Hi");
    }
}
```

**What happens:**
1. You type `public class Example {`
2. Press **Enter**
3. Cursor automatically jumps to the correct indentation (4 spaces)
4. You start typing `public void hello()` immediately
5. No need to manually type the spaces!

**Backspace behavior:**
- Press backspace → removes all 4 spaces at once
- Returns cursor to the previous line

#### Newline Enforcement
- When a newline is expected, you **must** press Enter
- Typing regular characters won't work
- This mimics real coding behavior

### Multiplayer Mechanics

#### How a Race Works

**Step 1: Joining**
- You navigate to `/room` (multiplayer page)
- Connect to a room (e.g., "room_1")
- Your username is sent to the server

**Step 2: Waiting**
- Screen shows "Waiting for players..."
- Need at least 2 players to start
- Tip: Open another browser tab to test with yourself!

**Step 3: Race Start**
- Server sends the same text to all players
- Everyone sees: "3... 2... 1... GO!"
- Race begins!

**Step 4: Racing**
- You type as fast as you can
- Your progress is sent to the server every 150ms
- Server broadcasts your progress to other players
- You see their ghost carets moving

**Step 5: Finish**
- First player to complete the text wins
- Winner announcement appears
- Your score is saved to MongoDB (if logged in)

#### Ghost Caret System

**What you see:**
```
[Your Cursor] ← Yellow blinking cursor (you)
[Player_123] ← Blue ghost caret (opponent)
[Guest_456] ← Green ghost caret (another opponent)
```

**How it works:**
1. Each player's progress percentage is sent to the server
2. Server broadcasts to all players
3. Your browser calculates where to show each ghost caret
4. Ghost carets move smoothly as players type
5. Username appears above each ghost caret

**Visual Design:**
- Different color for each player
- Semi-transparent so you can see the text
- Smooth animation when moving
- Shows player name on hover

### Authentication Flow

#### Sign Up
1. Click user icon → "Sign Up" tab
2. Enter username, email, password
3. Frontend sends to `POST /api/auth/signup`
4. Backend checks if username/email already exists
5. If unique, creates new user in MongoDB
6. User data saved to localStorage
7. Redirected to home page

#### Login
1. Click user icon → "Login" tab
2. Enter username and password
3. Frontend sends to `POST /api/auth/login`
4. Backend checks credentials (plain text comparison)
5. If correct, returns user data
6. User data saved to localStorage
7. Redirected to home page

#### Guest Mode
1. Click "Continue as Guest"
2. Sets `isGuest: true` in localStorage
3. Redirected to home page
4. Can play games, but stats won't save

#### Logout
1. Go to profile page
2. Click "Logout" button
3. User data cleared from localStorage
4. Redirected to home page

### Score Saving

#### Practice/Code Mode
When you finish a game:
1. Frontend gets user from localStorage
2. If logged in (not guest):
   - Sends score to `POST /api/scores`
   - Includes: username, userId, mode, WPM, accuracy, etc.
   - Backend saves to MongoDB
   - User stats automatically updated
3. If guest:
   - Score not saved
   - Console logs "Guest stats not saved"

#### Multiplayer Mode
When race finishes:
1. Frontend checks if you won
2. If logged in:
   - Sends score with `mode: "MULTIPLAYER"`
   - Includes `isWin: true/false`
   - Backend saves to MongoDB
   - Win count updated if you won
3. If guest:
   - Score not saved

---

## 4. Technical Architecture

### Frontend (React + Vite)

#### Main Technologies
- **React 18**: UI framework
- **Vite**: Fast build tool and dev server
- **Tailwind CSS**: Styling
- **Recharts**: Performance graphs
- **React Router**: Page navigation

#### Key Components

**TypingArea.jsx**
- Main typing interface
- Handles keyboard input
- Displays text with character states
- Shows WPM, accuracy, timer
- Renders result screen when done

**MultiplayerArea.jsx**
- Multiplayer typing interface
- Connects to WebSocket
- Shows ghost carets for other players
- Displays progress bars
- Handles race logic

**ResultScreen.jsx**
- Shows final statistics
- Displays performance graph
- Buttons for next test / restart
- Calculates consistency percentage

**ProfilePage.jsx**
- Fetches user data from backend
- Displays statistics cards
- Shows game history table
- Logout button

**Login.jsx**
- Sign up and login forms
- Form validation
- Guest mode button
- Error handling

**Navbar.jsx**
- Navigation links
- User icon (goes to profile if logged in)
- Responsive design

#### Custom Hooks

**useTypingEngine.js**
- Core typing logic
- Character state management
- WPM calculation (every 200ms)
- Timer countdown
- Backspace handling with stack
- Auto-indent logic for code mode
- Score saving when finished

**useGameSocket.js**
- WebSocket connection management
- Sends/receives multiplayer messages
- Handles player updates
- Winner detection

**useGameConfig.js**
- Manages game settings
- Test type (time/words)
- Test value (duration/count)
- Language selection

#### Services

**authService.js**
- `signup(username, email, password)` - Create account
- `login(username, password)` - Authenticate
- `logout()` - Clear session
- `getCurrentUser()` - Get logged-in user
- `isAuthenticated()` - Check if logged in
- `isGuest()` - Check if guest mode
- `setGuestMode()` - Enable guest mode

**api.js**
- Axios instance for API calls
- Base URL: `http://localhost:8080/api`
- Handles HTTP requests

### Backend (Spring Boot + MongoDB)

#### Main Technologies
- **Spring Boot 3.2**: Backend framework
- **Java 17**: Programming language
- **MongoDB**: NoSQL database
- **Spring Data MongoDB**: Database integration
- **WebSocket**: Real-time communication

#### Domain Models

**User.java**
```java
@Document(collection = "users")
public class User {
    @Id
    private String id;
    private String username;
    private String email;
    private String password;  // Plain text (educational project)
    private LocalDateTime createdAt;
    
    // Statistics
    private int totalGames;
    private double bestWPM;
    private double avgWPM;
    private int totalWins;
    
    // Method to update stats after each game
    public void updateStats(double wpm, boolean isWin) {
        totalGames++;
        if (wpm > bestWPM) bestWPM = wpm;
        avgWPM = ((avgWPM * (totalGames - 1)) + wpm) / totalGames;
        if (isWin) totalWins++;
    }
}
```

**GameRecord.java**
```java
@Document(collection = "game_records")
public class GameRecord {
    @Id
    private String id;
    private String userId;
    private String username;
    private double wpm;
    private double accuracy;
    private int wordsTyped;
    private String gameMode;  // PRACTICE, CODE, MULTIPLAYER
    private long duration;
    private LocalDateTime timestamp;
}
```

#### Repositories

**UserRepository.java**
```java
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
```

**GameRecordRepository.java**
```java
public interface GameRecordRepository extends MongoRepository<GameRecord, String> {
    List<GameRecord> findByUserIdOrderByTimestampDesc(String userId);
    List<GameRecord> findTop20ByUserIdOrderByTimestampDesc(String userId);
}
```

#### Services

**AuthService.java**
- Handles user registration and login
- Validates username/email uniqueness
- Simple password comparison (no hashing for simplicity)
- Returns user data on success

#### REST Controllers

**AuthController.java**
- `POST /api/auth/signup` - Create new user
- `POST /api/auth/login` - Authenticate user

**ProfileController.java**
- `GET /api/profile/{username}` - Get user profile
- `GET /api/profile/{username}/history` - Get game history

**TypingController.java**
- `GET /api/game/text` - Get random words
- `POST /api/scores` - Save game score
  - Creates GameRecord
  - Updates User statistics
  - Saves to MongoDB

**GameWebSocketController.java**
- `@MessageMapping("/game/join")` - Player joins room
- `@MessageMapping("/game/progress")` - Player progress update
- `@SendTo("/topic/game")` - Broadcast to all players

### Database (MongoDB)

#### Collections

**users**
```javascript
{
  _id: ObjectId("..."),
  username: "speedtyper",
  email: "speed@example.com",
  password: "mypassword",
  createdAt: ISODate("2026-01-26T12:00:00Z"),
  totalGames: 15,
  bestWPM: 95.5,
  avgWPM: 78.3,
  totalWins: 5
}
```

**game_records**
```javascript
{
  _id: ObjectId("..."),
  userId: "507f1f77bcf86cd799439011",
  username: "speedtyper",
  wpm: 85.2,
  accuracy: 96.5,
  wordsTyped: 50,
  gameMode: "MULTIPLAYER",
  duration: 35000,
  timestamp: ISODate("2026-01-26T12:30:00Z")
}
```

#### Why MongoDB?
- **Flexible schema**: Easy to add new fields
- **Fast queries**: Great for real-time stats
- **JSON-like documents**: Works well with JavaScript frontend
- **No complex joins**: Simple data structure

### WebSocket Communication

#### Message Types

**PLAYER_JOIN**
```json
{
  "type": "PLAYER_JOIN",
  "username": "speedtyper",
  "roomId": "room_1"
}
```

**PLAYER_UPDATE** (Progress)
```json
{
  "type": "PLAYER_UPDATE",
  "players": [
    {
      "username": "speedtyper",
      "wpm": 75,
      "progress": 45
    },
    {
      "username": "Guest_123",
      "wpm": 68,
      "progress": 38
    }
  ]
}
```

**GAME_START**
```json
{
  "type": "GAME_START",
  "text": "the quick brown fox jumps over the lazy dog",
  "roomId": "room_1"
}
```

**GAME_FINISH**
```json
{
  "type": "GAME_FINISH",
  "winner": "speedtyper",
  "wpm": 95.5,
  "accuracy": 98.2
}
```

### Data Flow Example

#### Complete Game Flow (Logged-in User)

1. **User logs in**
   - Frontend: `authService.login("john", "pass123")`
   - Backend: `POST /api/auth/login`
   - MongoDB: Query users collection
   - Response: User data
   - Frontend: Save to localStorage

2. **User starts practice mode**
   - Frontend: Select "time 30"
   - Frontend: Request text from backend
   - Backend: `GET /api/game/text?count=50`
   - Backend: Return random words
   - Frontend: Display text, start timer

3. **User types**
   - Every keystroke updates character states
   - WPM calculated every 200ms
   - Accuracy updated in real-time
   - Timer counts down

4. **Timer reaches 0**
   - Phase changes to "finished"
   - Result screen appears
   - Score sent to backend automatically

5. **Score saving**
   - Frontend: `POST /api/scores` with user data
   - Backend: Create GameRecord in MongoDB
   - Backend: Update User statistics
   - Backend: Save both documents
   - Frontend: Console log "Stats saved"

6. **User views profile**
   - Frontend: Navigate to `/profile`
   - Frontend: `GET /api/profile/john`
   - Backend: Query users collection
   - Response: User stats
   - Frontend: Display statistics

7. **User views history**
   - Frontend: `GET /api/profile/john/history`
   - Backend: Query game_records collection
   - Backend: Sort by timestamp, limit 20
   - Response: Array of game records
   - Frontend: Display in table

---

## 5. Setup & Running

### Prerequisites

1. **Java 17 or higher**
   - Check: `java -version`
   - Download: https://www.oracle.com/java/technologies/downloads/

2. **Node.js 16 or higher**
   - Check: `node -v`
   - Download: https://nodejs.org/

3. **MongoDB**
   - Download: https://www.mongodb.com/try/download/community
   - Or use Docker: `docker run -d -p 27017:27017 mongo`

4. **Maven** (usually comes with Java)
   - Check: `mvn -v`

### Installation Steps

#### 1. Start MongoDB

**Option A: Local MongoDB**
```bash
# Windows
mongod

# Mac/Linux
sudo systemctl start mongod
```

**Option B: Docker**
```bash
docker run -d -p 27017:27017 --name mongodb mongo
```

**Verify MongoDB is running:**
```bash
mongo --eval "db.version()"
```

#### 2. Start Backend (Spring Boot)

```bash
# Navigate to project root
cd C:\Users\Administrator\Desktop\Fast-Fingers

# Install dependencies and build
mvn clean install

# Run the application
mvn spring-boot:run
```

**Backend will start on:** `http://localhost:8080`

**Check if it's running:**
- Open browser: `http://localhost:8080/api/game/text?count=10`
- Should see random words

#### 3. Start Frontend (React)

```bash
# Navigate to frontend folder
cd frontend

# Install dependencies (first time only)
npm install

# Start development server
npm run dev
```

**Frontend will start on:** `http://localhost:5173`

**Open in browser:** `http://localhost:5173`

### Testing the Application

#### Test 1: Guest Mode
1. Open `http://localhost:5173`
2. Click "Continue as Guest" (or just start typing)
3. Type the words shown
4. See your WPM update in real-time
5. Complete the test
6. View results screen

#### Test 2: Create Account
1. Click user icon (top right)
2. Click "Sign Up" tab
3. Enter username, email, password
4. Click "Register"
5. Should redirect to home page

#### Test 3: Play and Save Score
1. Make sure you're logged in
2. Play a typing game
3. Complete the test
4. Open browser console (F12)
5. Should see: "Stats saved to MongoDB for [username]"

#### Test 4: View Profile
1. Click user icon (top right)
2. Should go to profile page
3. See your statistics (WPM, games played, etc.)
4. See game history table

#### Test 5: Multiplayer
1. Navigate to `http://localhost:5173/room`
2. Open another browser tab (or incognito window)
3. Also navigate to `/room`
4. Both should connect to "room_1"
5. Game starts when 2 players join
6. Type and race!
7. See ghost carets moving

#### Test 6: MongoDB Verification
1. Open MongoDB Compass
2. Connect to `mongodb://localhost:27017`
3. Open `fastfingers` database
4. Check `users` collection - see your account
5. Check `game_records` collection - see your games

### Common Issues

#### MongoDB not connecting
**Error:** `Connection refused`
**Solution:**
```bash
# Check if MongoDB is running
mongo --eval "db.version()"

# If not, start it
mongod
```

#### Backend won't start
**Error:** `Port 8080 already in use`
**Solution:**
```bash
# Find process using port 8080
netstat -ano | findstr :8080

# Kill the process (Windows)
taskkill /PID <process_id> /F
```

#### Frontend won't start
**Error:** `EADDRINUSE: address already in use`
**Solution:**
```bash
# Kill process on port 5173
npx kill-port 5173

# Or use a different port
npm run dev -- --port 3000
```

#### Scores not saving
**Check:**
1. MongoDB is running
2. Backend console shows "Stats saved to MongoDB"
3. User is logged in (not guest)
4. Browser console for errors

### Project Structure

```
Fast-Fingers/
├── src/main/java/com/typinggame/
│   ├── domain/
│   │   ├── User.java                    # User model
│   │   └── GameRecord.java              # Game record model
│   ├── repository/
│   │   ├── UserRepository.java          # User database queries
│   │   └── GameRecordRepository.java    # Game record queries
│   ├── service/
│   │   └── AuthService.java             # Authentication logic
│   ├── api/
│   │   ├── AuthController.java          # Login/signup endpoints
│   │   ├── ProfileController.java       # Profile endpoints
│   │   ├── TypingController.java        # Game endpoints
│   │   └── GameWebSocketController.java # Multiplayer WebSocket
│   └── TypingGameApplication.java       # Main application
├── src/main/resources/
│   └── application.properties           # MongoDB configuration
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   │   ├── game/
│   │   │   │   ├── TypingArea.jsx       # Main typing component
│   │   │   │   ├── MultiplayerArea.jsx  # Multiplayer component
│   │   │   │   ├── ResultScreen.jsx     # Results display
│   │   │   │   ├── GhostCaret.jsx       # Ghost caret component
│   │   │   │   └── ProgressBar.jsx      # Race progress bar
│   │   │   └── layout/
│   │   │       └── Navbar.jsx           # Navigation bar
│   │   ├── pages/
│   │   │   ├── HomePage.jsx             # Main page
│   │   │   ├── Login.jsx                # Login/signup page
│   │   │   ├── ProfilePage.jsx          # User profile page
│   │   │   └── GameRoom.jsx             # Multiplayer room
│   │   ├── hooks/
│   │   │   ├── useTypingEngine.js       # Typing logic
│   │   │   ├── useGameSocket.js         # WebSocket logic
│   │   │   └── useGameConfig.js         # Game settings
│   │   ├── services/
│   │   │   ├── authService.js           # Auth utilities
│   │   │   └── api.js                   # API client
│   │   ├── App.jsx                      # Main app component
│   │   └── main.jsx                     # Entry point
│   └── package.json                     # Dependencies
├── pom.xml                              # Maven dependencies
└── PROJECT_GUIDE.md                     # This file!
```

---

## Summary

FastFingers is a complete typing game with:
- ✅ **Multiple game modes**: Practice (time/words), Code, Multiplayer
- ✅ **User accounts**: Sign up, login, guest mode
- ✅ **Real-time stats**: WPM updates every 200ms, live accuracy
- ✅ **Data persistence**: MongoDB stores users and game history
- ✅ **Multiplayer racing**: Real-time races with ghost carets
- ✅ **Profile system**: View stats and game history
- ✅ **Smart features**: Auto-indent in code mode, smooth animations
- ✅ **Modern UI**: Dark theme, responsive design, visual feedback

The project demonstrates:
- Full-stack development (React + Spring Boot)
- NoSQL database integration (MongoDB)
- Real-time communication (WebSocket)
- State management and hooks (React)
- RESTful API design
- User authentication
- Data persistence and statistics tracking

**Built with ❤️ for typing enthusiasts and developers!**
