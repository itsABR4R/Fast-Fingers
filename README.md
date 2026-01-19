# FastFingers - MonkeyType-Inspired Typing Game

A feature-rich, MonkeyType-inspired typing game built with **Spring Boot** (backend) and **React** (frontend). This project demonstrates advanced object-oriented programming concepts, real-time multiplayer functionality, and modern web development practices.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green)
![React](https://img.shields.io/badge/React-18-blue)
![Maven](https://img.shields.io/badge/Maven-3.9-red)

---

## 📋 Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Prerequisites](#-prerequisites)
- [Installation](#-installation)
- [Running the Application](#-running-the-application)
- [Project Structure](#-project-structure)
- [AOOP Requirements](#-aoop-requirements)
- [API Endpoints](#-api-endpoints)
- [Usage Guide](#-usage-guide)
- [Troubleshooting](#-troubleshooting)

---

## ✨ Features

### Core Typing Modes
- **Practice Mode**: Type random words with customizable word count (10/25/50/100)
- **Time Mode**: Race against the clock (15s/30s/60s/∞)
- **Code Mode**: Practice typing Java code snippets
- **Multiplayer Mode**: Real-time competitive typing with WebSocket
- **Battle Royale**: Elimination-based multiplayer (coming soon)

### Advanced Features
- **Real-time WPM & Accuracy Tracking**: Live statistics with visual graphs
- **Character-level Feedback**: Instant visual feedback for correct/incorrect characters
- **Smooth Animations**: Fluid cursor movement and character state transitions
- **Auto-indent Jump**: Smart indentation handling for code mode
- **Persistent Stats**: Save and track your progress using ObjectOutputStream
- **Responsive Design**: Works seamlessly on desktop and mobile

---

## 🛠 Tech Stack

### Backend
- **Java 17**
- **Spring Boot 3.2**
- **Maven** - Dependency management
- **WebSocket** - Real-time multiplayer
- **ObjectOutputStream/ObjectInputStream** - Data persistence
- **FileReader/FileWriter** - File I/O operations

### Frontend
- **React 18**
- **React Router** - Navigation
- **Axios** - HTTP client
- **Tailwind CSS** - Styling
- **Recharts** - WPM graphs
- **Vite** - Build tool

---

## 📦 Prerequisites

Before you begin, ensure you have the following installed:

### Required
- **Java Development Kit (JDK) 17 or higher**
  - Download: https://www.oracle.com/java/technologies/downloads/
  - Verify: `java -version`

- **Maven 3.9 or higher**
  - Download: https://maven.apache.org/download.cgi
  - Verify: `mvn -version`

- **Node.js 18 or higher**
  - Download: https://nodejs.org/
  - Verify: `node -v`

- **npm 9 or higher** (comes with Node.js)
  - Verify: `npm -v`

### Optional
- **Git** - For cloning the repository
- **VS Code** or **IntelliJ IDEA** - Recommended IDEs

---

## 📥 Installation

### 1. Clone the Repository
```bash
git clone <repository-url>
cd "AOOP PROJECT"
```

### 2. Backend Setup

#### Navigate to Backend Directory
```bash
cd backend
```

#### Install Dependencies
```bash
mvn clean install
```

#### Verify Installation
```bash
mvn dependency:tree
```

### 3. Frontend Setup

#### Navigate to Frontend Directory
```bash
cd ../frontend
```

#### Install Dependencies
```bash
npm install
```

#### Verify Installation
```bash
npm list
```

---

## 🚀 Running the Application

### Option 1: Run Both Servers Separately

#### Terminal 1 - Backend Server
```bash
cd backend
mvn spring-boot:run
```
**Backend will start on:** `http://localhost:8080`

#### Terminal 2 - Frontend Server
```bash
cd frontend
npm run dev
```
**Frontend will start on:** `http://localhost:5173`

### Option 2: Production Build

#### Build Frontend
```bash
cd frontend
npm run build
```

#### Run Backend (serves frontend)
```bash
cd ../backend
mvn spring-boot:run
```

---

## 📁 Project Structure

```
AOOP PROJECT/
├── src/
│   ├── main/
│   │   ├── java/com/typinggame/
│   │   │   ├── api/              # REST Controllers
│   │   │   ├── config/           # Configuration classes
│   │   │   ├── domain/           # Domain models (Word, Keystroke)
│   │   │   ├── engine/           # Core typing engine
│   │   │   ├── io/               # File I/O operations
│   │   │   ├── mode/             # Game modes (Practice, etc.)
│   │   │   ├── network/          # Multiplayer networking
│   │   │   └── websocket/        # WebSocket handlers
│   │   └── resources/
│   │       ├── words.txt         # Word bank
│   │       └── snippets/         # Code snippets
│   └── test/                     # Unit tests
│
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   │   ├── game/             # Game components
│   │   │   └── common/           # Reusable components
│   │   ├── hooks/                # Custom React hooks
│   │   ├── pages/                # Page components
│   │   ├── services/             # API services
│   │   └── App.jsx
│   └── package.json
│
└── README.md
```

---

## 🎓 AOOP Requirements

This project demonstrates the following Advanced Object-Oriented Programming concepts:

### Requirement 1: Inheritance & Polymorphism
- **`Word` class** implements `Comparable<Word>` interface
- **`BotPlayer`** extends base player functionality
- **`GameMode`** enum with polymorphic behavior

### Requirement 2: Collections Framework
- **ArrayList**: Word bank storage (`TypingEngine.java`)
- **HashSet**: Unique words tracking (`PracticeMode.java`)
- **Stack**: Backspace character tracking (`useTypingEngine.js`)
- **Queue**: Upcoming words queue (`PerformanceTracker.java`)
- **Collections.shuffle()**: Randomizing word selection

### Requirement 3: File I/O
- **FileReader**: Loading word lists and code snippets
- **ObjectOutputStream**: Saving user statistics (`ScoreManager.java`)
- **ObjectInputStream**: Loading user statistics
- **FileWriter**: Exporting match history

### Requirement 4: Networking (Bonus)
- **Java Sockets**: Multiplayer game server (`GameServer.java`)
- **WebSocket**: Real-time communication
- **ClientHandler**: Thread-per-client model
- **GameSession**: Managing multiplayer matches

---

## 🔌 API Endpoints

### Game Text
```http
GET /api/game/text?lang=english&count=50
GET /api/game/text?lang=java
```

### Scores
```http
POST /api/scores
Body: {
  "username": "string",
  "wpm": number,
  "accuracy": number,
  "mode": "string",
  "wordsTyped": number,
  "duration": number
}
```

### Words
```http
GET /api/words                    # Get all words
GET /api/words/random?count=10    # Get random words
GET /api/words/difficulty/EASY    # Get words by difficulty
```

### Session
```http
POST /api/session/start?wordCount=10
GET /api/session/metrics
POST /api/session/end
```

---

## 📖 Usage Guide

### 1. Starting a Practice Session
1. Open `http://localhost:5173`
2. Select mode: **Time** (15/30/60/∞) or **Words** (10/25/50/100)
3. Click on the typing area to focus
4. Start typing!

### 2. Code Mode
1. Click **CODE MODE** button
2. Select time duration
3. Type the Java code snippet accurately
4. Press **Enter** for auto-indent jump

### 3. Keyboard Shortcuts
- **Tab**: Restart test
- **Enter**: Auto-jump indent (code mode only)
- **Backspace**: Delete previous character

### 4. Viewing Results
- **WPM**: Words per minute (5 characters = 1 word)
- **Accuracy**: Percentage of correct keystrokes
- **Graph**: Real-time WPM fluctuation
- **Character Stats**: Correct/Incorrect/Missed breakdown

---

## 🐛 Troubleshooting

### Backend Issues

**Port 8080 already in use:**
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:8080 | xargs kill -9
```

**Maven build fails:**
```bash
mvn clean install -U
```

### Frontend Issues

**Port 5173 already in use:**
```bash
# Change port in vite.config.js
server: {
  port: 3000
}
```

**Dependencies not installing:**
```bash
rm -rf node_modules package-lock.json
npm install
```

**API connection refused:**
- Ensure backend is running on `http://localhost:8080`
- Check `frontend/src/services/api.js` for correct base URL

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

This project is created for educational purposes as part of an Advanced Object-Oriented Programming course.

---

## 👥 Authors

- Rasak Ahmed  - Frontend
- Abrar Jahin - Backend

---

## 🙏 Acknowledgments

- Inspired by [MonkeyType](https://monkeytype.com/)
- Spring Boot Documentation
- React Documentation
- Tailwind CSS

---

**Happy Typing! ⌨️**
