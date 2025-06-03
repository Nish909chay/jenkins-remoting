# Jenkins Remoting Web Frontend

This is a React-based web frontend for your Jenkins Remoting demo.

## Features
- Send messages to the agent
- Display replies from the agent
- Show the communication log

## Getting Started

1. Install dependencies:
   ```powershell
   cd frontend
   npm install
   ```
2. Start the development server:
   ```powershell
   npm start
   ```
3. The app will open at http://localhost:3000

## Integration
- Connect this frontend to your Java backend using REST or WebSocket APIs.
- Update `src/App.js` to call your backend endpoints.
