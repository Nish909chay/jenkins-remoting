import React, { useState, useEffect, useRef } from "react";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";

const arkColors = {
  bg: "#181A20",
  panel: "#23262F",
  accent: "#246BFD",
  text: "#F4F4F7",
  subtext: "#A3A7B3",
  input: "#23262F",
  inputText: "#F4F4F7",
  border: "#23262F",
  you: "#246BFD",
  agent: "#F4F4F7",
};

const fadeIn = {
  animation: "fadeIn 0.6s cubic-bezier(.39,.575,.565,1)",
};

function App() {
  const [log, setLog] = useState([]);
  const [input, setInput] = useState("");
  const ws = useRef(null);

  // Fetch initial log
  useEffect(() => {
    fetch("/api/remoting/log")
      .then((res) => res.json())
      .then((data) =>
        setLog(
          data.map((msg) => ({
            from: msg.startsWith("You:") ? "You" : "Agent",
            text: msg.replace(/^You: |^Agent: /, ""),
          }))
        )
      );
  }, []);

  // WebSocket connection
  useEffect(() => {
    const stompClient = new Client({
      webSocketFactory: () => new SockJS("/ws"),
      onConnect: () => {
        stompClient.subscribe("/topic/messages", (message) => {
          setLog((prev) => [...prev, { from: "Agent", text: message.body }]);
        });
      },
    });
    stompClient.activate();
    ws.current = stompClient;
    return () => {
      stompClient.deactivate();
    };
  }, []);

  const sendMessage = async () => {
    if (!input.trim()) return;
    setLog((prev) => [...prev, { from: "You", text: input }]);
    await fetch("/api/remoting/send", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ message: input }),
    });
    setInput("");
  };

  // Add Ark theme scrollbar and fade-in keyframes
  React.useEffect(() => {
    const style = document.createElement("style");
    style.innerHTML = `
      ::-webkit-scrollbar {
        width: 8px;
        background: #23262F;
        border-radius: 8px;
      }
      ::-webkit-scrollbar-thumb {
        background: #20222A;
        border-radius: 8px;
        transition: background 0.2s;
      }
      ::-webkit-scrollbar-thumb:hover {
        background: #2A2D36;
      }
      @keyframes fadeIn {
        0% { opacity: 0; transform: translateY(16px); }
        100% { opacity: 1; transform: translateY(0); }
      }
      .ark-fade-in { animation: fadeIn 0.6s cubic-bezier(.39,.575,.565,1); }
      .ark-btn:hover { filter: brightness(1.08); box-shadow: 0 4px 16px #246bfd33; }
      .ark-btn:active { filter: brightness(0.95); }
      @media (max-width: 600px) {
        .ark-panel { width: 98vw !important; padding: 12vw 2vw !important; }
      }
    `;
    document.head.appendChild(style);
    return () => document.head.removeChild(style);
  }, []);

  return (
    <div
      style={{
        minHeight: "100vh",
        background: arkColors.bg,
        color: arkColors.text,
        fontFamily: "Inter, Segoe UI, Arial, sans-serif",
        display: "flex",
        flexDirection: "column",
        justifyContent: "center",
        alignItems: "center",
        transition: "background 0.3s",
      }}
    >
      <div
        className="ark-panel"
        style={{
          width: 420,
          background: arkColors.panel,
          borderRadius: 18,
          boxShadow: "0 4px 32px #00000040",
          padding: 32,
          marginTop: 48,
          marginBottom: 48,
          maxWidth: "98vw",
          transition: "box-shadow 0.2s, background 0.3s",
        }}
      >
        <h2
          style={{
            color: arkColors.text,
            fontWeight: 700,
            fontSize: 28,
            marginBottom: 8,
            letterSpacing: 1,
            transition: "color 0.2s",
          }}
        >
          Jenkins Remoting{" "}
          <span style={{ color: arkColors.accent }}>Ark</span> Chat
        </h2>
        <div
          style={{
            color: arkColors.subtext,
            fontSize: 15,
            marginBottom: 24,
            transition: "color 0.2s",
          }}
        >
          Minimal, modern, and dark. Master ↔ Agent messaging in style.
        </div>
        <div
          style={{
            background: arkColors.bg,
            borderRadius: 12,
            minHeight: 260,
            maxHeight: 320,
            overflowY: "auto",
            border: `1px solid ${arkColors.border}`,
            padding: 18,
            marginBottom: 18,
            boxShadow: "0 1px 8px #00000020",
            transition: "background 0.3s, border 0.2s",
          }}
        >
          {log.length === 0 && (
            <div
              style={{
                color: arkColors.subtext,
                textAlign: "center",
                marginTop: 60,
                transition: "color 0.2s",
              }}
              className="ark-fade-in"
            >
              No messages yet.
            </div>
          )}
          {log.map((entry, i) => (
            <div
              key={i}
              style={{
                display: "flex",
                flexDirection: "row",
                justifyContent:
                  entry.from === "You" ? "flex-end" : "flex-start",
                margin: "10px 0",
              }}
              className="ark-fade-in"
            >
              <div
                style={{
                  background:
                    entry.from === "You"
                      ? arkColors.accent
                      : arkColors.panel,
                  color:
                    entry.from === "You"
                      ? arkColors.inputText
                      : arkColors.text,
                  borderRadius: 14,
                  padding: "10px 18px",
                  maxWidth: "75%",
                  fontSize: 16,
                  boxShadow:
                    entry.from === "You"
                      ? "0 2px 8px #246bfd33"
                      : "0 1px 4px #00000020",
                  border:
                    entry.from === "You"
                      ? `1px solid ${arkColors.accent}`
                      : `1px solid ${arkColors.border}`,
                  fontWeight: 500,
                  wordBreak: "break-word",
                  transition: "background 0.3s, color 0.2s, border 0.2s",
                }}
              >
                <span
                  style={{
                    fontSize: 13,
                    fontWeight: 600,
                    color:
                      entry.from === "You"
                        ? arkColors.inputText
                        : arkColors.accent,
                    opacity: 0.7,
                    marginRight: 8,
                    transition: "color 0.2s",
                  }}
                >
                  {entry.from}
                </span>
                {entry.text}
              </div>
            </div>
          ))}
        </div>
        <div
          style={{
            display: "flex",
            flexDirection: "row",
            alignItems: "center",
            gap: 10,
            marginTop: 2,
            transition: "gap 0.2s",
          }}
        >
          <input
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && sendMessage()}
            placeholder="Type a message..."
            style={{
              flex: 1,
              background: arkColors.input,
              color: arkColors.inputText,
              border: `1.5px solid ${arkColors.border}`,
              borderRadius: 10,
              padding: "12px 16px",
              fontSize: 16,
              outline: "none",
              transition:
                "border 0.2s, background 0.3s, color 0.2s, box-shadow 0.2s",
              boxShadow: `0 1px 4px #00000010, 0 0 0 2px #246BFD55`, // subtle blue glow
            }}
            autoFocus
          />
          <button
            onClick={sendMessage}
            className="ark-btn"
            style={{
              background: arkColors.accent,
              color: "#fff",
              border: "none",
              borderRadius: 10,
              padding: "12px 22px",
              fontWeight: 700,
              fontSize: 16,
              cursor: "pointer",
              boxShadow: "0 2px 8px #246bfd33",
              transition: "background 0.2s, box-shadow 0.2s",
            }}
          >
            Send
          </button>
        </div>
      </div>
      <div
        style={{
          color: arkColors.subtext,
          fontSize: 13,
          marginTop: -24,
          marginBottom: 16,
          opacity: 0.7,
          transition: "color 0.2s",
        }}
      >
        Powered by Jenkins Remoting &mdash; Ark Theme UI
      </div>
    </div>
  );
}

export default App;
