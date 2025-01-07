# Delirious

## MinecartToucher (2025-01-06)

Pretty simple idea, when carts are generated/placed they are on position 0.5 of the block. When on rails like 

## MissingCaveAir (2025-01-04)

A module to detect when cave_air has been replaced with air such as if a block like a torch was placed and then broken.
This difference is invisible to the eye but can reveal player activity. There are two checks that can be run:

**Dungeon Scan** - This specifically looks for dungeon spawner rooms that generated with cave air (default since 1.13).
Once found, it scans where cave air should be in the room and alert when it finds air instead.

**Minecart Scan** - Detect chest minecarts in what is likely a mineshaft and scans for any unexpected air blocks. There 
one is a bit more prone to false positives and false negatives because of the greater variation in mineshaft generation.

### False Positives

No one likes to waste time on false alerts so I've made an effort to prefer false negatives to false positives. 

Some of the most common false positives I've run into include:

**Water/Lava Flows** - These can for example water might flow over a rail and break it, or lava might burn a cob web. 
The result being an unexpected airblock that wasn't player interaction. 

**Double spawners** - Specifically around the 2-wide doorway gap, when the doorway ends up in the middle of the chamber.
These get air in the middle of the room leading to a false positive along the middle wall.

**Old Chunks** - Originally Mineshafts and Dungeons did not have cave air, most of the time these will be skipped as the
whole chunk won't have cave air. However sometimes we get a mix of generation with and without cave air leading to the 
scanner looking even thouhg there is no cave air in the dungeon specifically leading to many blocks being highlighted.

**Weird Generation** - Especially with the mineshafts there are so many possibilities that its hard to account for 
everything. Caves encroach into the mineshaft in so many different ways, so just be aware of it and just looked at the 
missing cave_air, does it actually make sense for there to have been a block there.

## Alternative Elytra (2025-01-04)

My first module to help me find some Elytra. It's kinda silly but it'll alert on finding a mob wearing an Elytra or on 
finding a dropped Elytra like from a dead player.

I've been using it and have had some successful finds with it:
 - A piglin in a fortress just off but a fair bit under the nether highway about 20k blocks down an axis highway
 - A dropped Elytra floating on the surface of the ocean
