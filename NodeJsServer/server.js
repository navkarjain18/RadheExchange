/**
 * SIMPLE & FAST WEBSOCKET RELAY SERVER
 * Supports:
 *  - Connect (role + username)
 *  - Command forwarding phone → desktop
 *  - No ACK required
 */

const WebSocket = require("ws");

const PORT = 8080;

// Store connected clients
// clients.desktop["username"] = socket
// clients.phone["username"] = socket
const clients = {
  desktop: {},
  phone: {}
};

const wss = new WebSocket.Server({ port: PORT }, () => {
  console.log("🚀 WebSocket Server running on port", PORT);
});

wss.on("connection", (ws) => {
  console.log("\n🔗 Client connected. Awaiting CONNECT message...");

  let assignedRole = null;
  let assignedUser = null;

  ws.on("message", (raw) => {
    const message = raw.toString();
    console.log("📩 Received:", message);

    // Parse JSON safely
    let data;
    try {
      data = JSON.parse(message);
    } catch (e) {
      console.log("❌ Invalid JSON:", message);
      return;
    }

    // ------------------------------------
    // 1️⃣ CONNECT MESSAGE
    // ------------------------------------
    if (data.type === "connect") {
      const { role, username } = data;

      if (!role || !username) {
        console.log("❌ Missing role or username.");
        return;
      }

      clients[role][username] = ws; // store socket
      assignedRole = role;
      assignedUser = username;

      console.log(`🟢 Registered: ${role.toUpperCase()} (${username})`);

      ws.send(JSON.stringify({
        type: "connected",
        role,
        username,
        message: "Connection established"
      }));

      return;
    }

    // ------------------------------------
    // 2️⃣ COMMAND MESSAGE (phone → desktop)
    // ------------------------------------
    if (data.type === "command") {
      const { username, payload } = data;

      if (!username || !payload) {
        console.log("❌ Invalid COMMAND message.");
        return;
      }

      const desktopSocket = clients.desktop[username];
      if (!desktopSocket) {
        console.log("⚠️ Desktop not connected for user:", username);
        return;
      }

      console.log(`➡️ Forwarding command to DESKTOP (${username})`);
      desktopSocket.send(JSON.stringify({
        type: "command",
        username,
        payload
      }));

      return;
    }
  });

  // ------------------------------------
  // Disconnection handling
  // ------------------------------------
  ws.on("close", () => {
    console.log(`❌ Disconnected: role=${assignedRole}, user=${assignedUser}`);

    if (assignedRole && assignedUser) {
      delete clients[assignedRole][assignedUser];
    }
  });
});
