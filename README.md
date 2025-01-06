# Delirious

### MissingCaveAir (2025-01-04)

A module to detect when cave_air has been replaced with air. While not visually noticable it can reveal that a player 
has been present in the area and had placed and block then removed it to cover their tracks.

#### Limitations

There is a bug I've witnessed on 2b2t specifically with dungeons in older chunks. Most of the time the old chunks were
generated without any cave_air at all so the module doesn't try to find any missing air. However, in some cases there is 
cave_air in the chunk, but the dungeon was generated without it causing the whole thing to be detected and shown via ESP.

Cave_air is also used in Mineshafts, which is another interesting location to detect player activity. Unfortunately, my attempts
so far have been rife with rather annoying and believable false positives. 

### Alternative Elytra (2025-01-04)

My first module to help me find some Elytra. It's kinda silly but it'll alert on finding a mob wearing an Elytra or on 
finding a dropped Elytra like from a dead player.


