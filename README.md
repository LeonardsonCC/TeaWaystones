# TeaWaystones
![Build Status](https://drone.tardis.systems/api/badges/Kurt/TeaWaystones/status.svg)

TeaWaystones is my attempt at recreating the [Waystones mod](https://modrinth.com/mod/waystones) as a spigot/paper plugin. It allows you to craft waystones players can teleport to once they visited them. 

You can find the builds to download [here](https://releases.tardis.systems/TeaWaystones). Just drop it into your plugins folder and you should be ready to go.

The plugin is compiled for and tested with Paper 1.20.4.

## Usage

You can craft Waystones with the following crafting recipe:
![First row: Obsidian, Eye of Ender, Obsidian; Second row: Eye of Ender, Light Blue Candle, Eye of Ender; Third row: Obsidian, Compass, Obsidian](https://share.k00.eu/image_waystones_crafting.png)

To rename a Waystone, you need to be standing directly next to one and then use the command `/tw setname <your-name>`. The name may not contain spaces.

## commands 
|command|permission|description|
|-|-|-|
|`/tw setname <name>`||sets the name of the waystone next to you|
|`/tw list`|`waystones.command.list`|List all waystones with their location and the uuids of the players who visited the waystone.|
|`/tw setpublic <true/false>`||Changes if the waystone next to you can be teleported to without having visited it.|
|`/tw openui [page]`|`waystones.command.openui`|Opens the waystone menu as if you interacted with a waystone.|
