# Delirious

## MissingCaveAir (2025-01-04)

A module to detect when cave_air has been replaced with air such as if a block like a torch was placed and then broken.
This difference is invisible to the eye but can reveal player activity. There are two checks that can be run:

**Dungeon Scan** - This specifically looks for dungeon spawner rooms that generated with cave air (default since 1.13).
Once found, it scans where cave air should be in the room and alert when it finds air. 

**Minecart Scan** - Detect chest minecarts in what is likely a mineshaft and scans for any unexpected air blocks. There 
one is a bit more prone to false positives and false negatives because of the greater variation in mineshaft generation. 
If it can't match the layout around the minecart to an expected pattern it'll just skip the check, and because of the 
natural variations in shaft generation occasionally air might enroach into the minecart area.

### False Positives

No one likes to waste time on false alerts so I've made an effort to prefer false negatives to false positives. 

Some of the most common false positives I've run into include:

**Double spawners** - Specifically around the 2-wide doorway gap, when the doorway ends up in the middle of the chamber.
These get air in the middle of the room leading to a false positive along the middle wall.

**Updated Chunks** - This happens for various reasons but the clear sign of it will be a bunch of detections and what  
looks like no blocks that are not cave_air in the area. It seems like in some updates cave_air was placed into the chunk
but not everywhere that it was expected. One case I saw had a dungeon a few blocks above a mineshaft. The dungeon had
cave_air but the mineshaft in the same chunk did not, it happens with dungeons too though.

**Weird Generation** - Especially with the mineshafts there are so many possibilities that its hard to account for 
everything. Caves enroach into the mineshaft in so many different ways, so just be aware of it and just looked at the 
missing cave_air, does it actually make sense for there to have been a block there.

## Alternative Elytra (2025-01-04)

My first module to help me find some Elytra. It's kinda silly but it'll alert on finding a mob wearing an Elytra or on 
finding a dropped Elytra like from a dead player.

I've been using it and have had some successful finds with it:
 - A piglin in a fortress just off but a fair bit under the nether highway about 20k blocks down an axis highway
 - A dropped Elytra floating on the surface of the ocean

### Minecart Toucher

To be honest I don't know if this'll be useful. It was just something I noticed while working on cave air. It'll detect 
if a user has bumped into a minecart as it can be visually imperceptible and happen accidentally. 

