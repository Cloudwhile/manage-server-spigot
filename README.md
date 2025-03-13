# ManageServer

A feature-rich Spigot server management plugin for Minecraft 1.20.1.

## Features

- Player login/logout notifications
- Welcome message for new players joining the server for the first time
- Admin player ban functionality
- Admin player kick functionality
- Admin player unban functionality
- QQ integration via OneBot protocol (supports both HTTP and WebSocket)

## Commands

- `/ban <player> [reason]` - Ban a player
- `/kick <player> [reason]` - Kick a player
- `/unban <player>` - Unban a player

## QQ Commands (when OneBot is enabled)

- `!bind <player>` - Bind your QQ account to a Minecraft player
- `!unbind` - Unbind your QQ account
- `!list` - List online players
- `!help` - Show help message
- Admin group can execute Minecraft commands directly

## Permissions

- `manageserver.ban` - Allows using the ban command
- `manageserver.kick` - Allows using the kick command
- `manageserver.unban` - Allows using the unban command

## Configuration

The plugin configuration file is located at `plugins/ManageServer/config.yml`, where you can customize the following:

- Message prefix
- Player join/quit messages
- New player welcome message
- Ban/kick messages
- Whether to broadcast ban/kick messages
- OneBot settings (QQ integration)

### OneBot Configuration

```yaml
onebot:
  # Whether to enable OneBot functionality
  enabled: false
  # Connection type: http or websocket
  connection-type: websocket
  # HTTP connection settings (used when connection-type is http)
  http:
    url: "http://127.0.0.1:5700"
    access-token: ""
  # WebSocket connection settings (used when connection-type is websocket)
  websocket:
    url: "ws://127.0.0.1:6700"
    access-token: ""
  # Message forwarding settings
  forward:
    # Forward server messages to QQ
    server-to-qq: true
    # Forward QQ messages to server
    qq-to-server: true
    # Forward player join/quit messages to QQ
    player-join-quit: true
    # Forward ban/kick messages to QQ
    ban-kick: true
  # QQ group settings
  groups:
    # Main group ID
    main-group: 123456789
    # Admin group ID (optional)
    admin-group: 987654321
  # QQ user to Minecraft player bindings
  bindings:
    # Format: QQ number: player name
    123456789: "ExamplePlayer"
  # Command prefix (for executing Minecraft commands in QQ)
  command-prefix: "!"
```

## Installation

1. Download the plugin JAR file
2. Place the JAR file in your server's `plugins` folder
3. Restart the server or load the plugin using a plugin manager
4. Configure the `config.yml` file (optional)
5. If you want to use QQ integration, set up a OneBot-compatible QQ bot (like go-cqhttp)
