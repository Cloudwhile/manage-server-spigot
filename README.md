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

## Building

If you want to build the plugin yourself:

1. Clone this repository
2. Use the provided build scripts:
   - Windows: Run `build.bat` or `build-with-settings.bat`
3. The built JAR file will be in the `target` folder

## Troubleshooting

### Cannot find plugin 'org.apache.maven.plugins:maven-shade-plugin'

If you encounter this issue, try the following solutions:

1. **Use custom Maven settings**:
   - Run the `build-with-settings.bat` script, which uses a custom `settings.xml` file with configured mirrors for China

2. **Manually set Maven mirrors**:
   - Edit your Maven settings file (usually located at `~/.m2/settings.xml`)
   - Add Aliyun or Huawei Cloud mirrors

3. **Check network connection**:
   - Verify that your network connection is working properly
   - If using a proxy, ensure Maven can access the internet through it

4. **Clear Maven cache**:
   - Delete the cache in the `~/.m2/repository` directory
   - Run the build command again with the `-U` parameter to force dependency updates
