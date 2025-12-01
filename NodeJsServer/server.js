/**
 * ONE PHONE → MULTIPLE DESKTOPS
 * Command format remains SAME:
 *
 * {
 *   "type": "command",
 *   "username": "rahul",   // OR "all"
 *   "payload": { "action": "wicket" }
 * }
 *
 * Phone connects with:  { type:"connect", role:"phone" }
 * Desktop connects with: { type:"connect", role:"desktop", username:"rahul" }
 */

const WebSocket = require("ws");
const PORT = 8080;

const clients = {
  phone: null,        // only one phone
  desktop: {}         // multiple desktops by username
};

const wss = new WebSocket.Server({ port: PORT }, () => {
  console.log("🚀 WebSocket Server running on port", PORT);
});

wss.on("connection", (ws) => {
  console.log("\n🔗 Client connected. Awaiting CONNECT message...");

  let assignedRole = null;
  let assignedUser = null;

  ws.on("message", (raw) => {
    const msg = raw.toString();
    console.log("📩 Received:", msg);

    let data;
    try {
      data = JSON.parse(msg);
    } catch {
      console.log("❌ Invalid JSON received");
      return;
    }

    // ------------------------------------
    // 1️⃣ CONNECT HANDLER
    // ------------------------------------
    if (data.type === "connect") {
      const { role, username } = data;

      // Phone connect
      if (role === "phone") {
        clients.phone = ws;
        assignedRole = "phone";
        console.log("📱 PHONE connected");
        return;
      }

      // Desktop connect
      if (role === "desktop") {
        if (!username) {
          console.log("❌ Desktop missing username");
          return;
        }

        clients.desktop[username] = ws;
        assignedRole = "desktop";
        assignedUser = username;

        console.log(`🖥️ Desktop connected (${username})`);
        return;
      }

      return;
    }

    // ------------------------------------
    // 2️⃣ COMMAND HANDLER (phone → desktops)
    // ------------------------------------
    if (data.type === "command") {
      if (assignedRole !== "phone") {
        console.log("❌ Only PHONE can send commands");
        return;
      }

      const { username, payload } = data;
      if (!username || !payload) {
        console.log("❌ Invalid COMMAND message.");
        return;
      }

      // Broadcast case: username === "all"
      if (username === "all") {
        console.log("📢 Broadcasting command to ALL desktops");
        for (const user in clients.desktop) {
          clients.desktop[user].send(JSON.stringify({
            type: "command",
            username: user,  // each desktop receives its own username
            payload
          }));
        }
        return;
      }

      // Targeted case: send to one desktop
      const desktopSocket = clients.desktop[username];
      if (!desktopSocket) {
        console.log(`⚠️ Desktop '${username}' is not connected`);
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
  // Disconnection logic
  // ------------------------------------
  ws.on("close", () => {
    console.log(`❌ Disconnected (role=${assignedRole}, user=${assignedUser})`);

    if (assignedRole === "phone") {
      clients.phone = null;
      console.log("📱 Phone disconnected");
    }

    if (assignedRole === "desktop" && assignedUser) {
      delete clients.desktop[assignedUser];
      console.log(`🖥️ Desktop removed (${assignedUser})`);
    }
  });
});
