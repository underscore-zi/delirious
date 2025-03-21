# Delirious

## Install

1. Download and install the latest **stable** (not dev) version of [Meteor](https://meteorclient.com/)
2. Download the latest .jar from the [releases page](https://github.com/underscore-zi/delirious/releases)
3. Place jar into your mods folder like any other meteor module

## Modules

### AutoWheatBread (2025-03-20)

Another stupid implementation just adding becauase it works for me. When you're holding wheat in your main hand it will
try to bread any animals in reach. Not very well tested, really just use it on cows. Only supports wheat but not hard to
expand to work with more.

### Bonemeal Farmer (2025-03-20)

Put seeds and bonemeal in your hotbar and face tilled dirt right in front of you. It will plant, bonemeal, harvest, and 
repeat. Is it efficient? Absolutely not, but if you've got bonemeal but no crop, it works in a pinch. 

### AutoAnvil (2025-03-18)

AutoEnchat basically. have a bunch of unenchanted items and necessary books in your inventory (or optionally in chests,
and shulkers nearby (not barrels or other storage options)) and it will attempt to enchant everything. If you park it at 
an XP farm while you do this it'll also it can turn on Kill Aura and wait for the player to reach the right XP level. 
Lastly it can drop the freshly enchanted item at its feet for a hopper to pick up to keep your inventory clear making it
possible to AFK with this.

### AreaLoader (2025-02-10)

Spirals around your current position loading in chunks with an ever widening radius. Useful for checking out potential
portal skip locations to see if you can find the overworld path. This requires a Baritone patch that isn't yet public 
for non-nether elytra flying.

### DisplacedStack (2025-01-28)

Highlight displaced blocks around minecarts and dungeon spawners. A displaced block is one that is entirely surrounded by
blocks of another type. Like if someone stacked up on cobblestone after digging down to a chest. There is no alert on 
finding these as there can be many false positives but the human eye should be able to tell what looks suspicious or not.

### StackedStorage (2025-01-10)

Just give you an alert when coming across a storage minecart that is stacked in the same position as another minecart.

### MinecartToucher (2025-01-06)

Pretty simple idea, when carts are generated/placed they are on position 0.5 of the block. They can easily be pushed out
of that .5 alignment to a position without visually looking out of place.

### MissingCaveAir (2025-01-04)

A module to detect when cave_air has been replaced with air such as if a block like a torch was placed and then broken.
This difference is invisible to the eye but can reveal player activity. There are two checks that can be run:

**Dungeon Scan** - This specifically looks for dungeon spawner rooms that generated with cave air (default since 1.13).
Once found, it scans where cave air should be in the room and alert when it finds air instead.

**Minecart Scan** - Detect chest minecarts in what is likely a mineshaft and scans for any unexpected air blocks. There 
one is a bit more prone to false positives and false negatives because of the greater variation in mineshaft generation.

**Touch Scan** - This looks for any air block that is adjacent to a cave_air block. As cave air generates in clumps an 
air block in the middle of a clump is a sign of a player interaction. Though it can also just happen on the edges where
the different types of airs meet. It might be useful if you're investigating an area though.

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
