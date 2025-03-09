# ManageServer

A feature-rich Spigot server management plugin for Minecraft 1.20.1.

## Features

- Player login/logout notifications
- Welcome message for new players joining the server for the first time
- Admin player ban functionality
- Admin player kick functionality
- Admin player unban functionality

## Commands

- `/ban <player> [reason]` - Ban a player
- `/kick <player> [reason]` - Kick a player
- `/unban <player>` - Unban a player

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

## Installation

1. Download the plugin JAR file
2. Place the JAR file in your server's `plugins` folder
3. Restart the server or load the plugin using a plugin manager
4. Configure the `config.yml` file (optional)