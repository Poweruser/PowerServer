PowerServer
===========

A substitute for GameSpy's master server. 


The goal of this project is to create a master server, which picks up the pieces that GameSpy leaves behind on May 31.2014. It won't be a fully qualified replacement of GameSpy's master server, with all the different services and features, though. Actually it will be a very limited version, with the main focus on serving game server IPs.


This project currently evolves around and is driven by a single game: `Operation Flashpoint:Resistance`, but I'll try to design it as game-independent as possible, with the intention to have it expanded for other games as well. 

For the beginning, only the [GameSpy protocol 1](http://int64.org/docs/gamestat-protocols/gamespy.html) will be supported and therefore only games which use that one. I tried to allow a game to define what protocol it is using, but this makes the whole code completly complicated, which made me fail so far.
Once everything is up and running as planned, I might get back to adding support for the other protocols.
