# Delirious

## Install

1. Download and install the latest **stable** (not dev) version of [Meteor](https://meteorclient.com/)
2. Download the latest .jar from the [releases page](https://github.com/underscore-zi/delirious/releases)
3. Place jar into your mods folder like any other meteor module

## Modules

### MinecartToucher (2025-01-06)

Pretty simple idea, when carts are generated/placed they are on position 0.5 of the block. When on rails like 

### MissingCaveAir (2025-01-04)

A module to detect when cave_air has been replaced with air such as if a block like a torch was placed and then broken.
This difference is invisible to the eye but can reveal player activity. There are two checks that can be run:

**Dungeon Scan** - This specifically looks for dungeon spawner rooms that generated with cave air (default since 1.13).
Once found, it scans where cave air should be in the room and alert when it finds air instead.

**Minecart Scan** - Detect chest minecarts in what is likely a mineshaft and scans for any unexpected air blocks. There 
one is a bit more prone to false positives and false negatives because of the greater variation in mineshaft generation.

#### False Positives

No one likes to waste time on false alerts so I've made an effort to prefer false negatives to false positives. 

Some of the most common false positives I've run into include:

**Water/Lava Flows** - These can for example water might flow over a rail and break it, or lava might burn a cob web. 
The result being an unexpected air block that wasn't player interaction. 

**Double spawners** - Specifically around the 2-wide doorway gap, when the doorway ends up in the middle of the chamber.
These get air in the middle of the room leading to a false positive along the middle wall.

**Weird Generation** - Main thing to look for is does it look like the air block is exactly on the boundary between a
special cave structure like dungeon or mineshft and the noraml cave. If it is its probably a false positive.

### Alternative Elytra (2025-01-04)

My first module to help me find some Elytra. It's kinda silly but it'll alert on finding a mob wearing an Elytra or on 
finding a dropped Elytra like from a dead player.

I've been using it and have had some successful finds with it:
 - A piglin in a fortress just off but a fair bit under the nether highway about 20k blocks down an axis highway
 - A dropped Elytra floating on the surface of the ocean
