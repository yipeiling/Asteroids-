# Asteroids-

Changed the game Asteroids (folk.ntnu.no/alfredse/Fun/Asteroids.java) to a multiplayer game that synchronizes its state in each game client.

1.Adds a second player and allows game state changes to be communicated over the network to other game clients.

2.Artificial network latency is introduced to simulate some of the challenges that occur in online games.

3.Implement bucket sync so that all clients have the same frame rate so games can be considered "fair"

4.Implement interest management so that the large map exists on the primary copy of the game, and object information sent to secondary copies should only contain information relevant to that client

5.Implemented dead reckoning so that players continue to update in the event that network issues delay receiving updates about the player's location
Players send actions, states to the server, and the server broadcasts them to other players.

6.Implemented a smoothing correction so the player moves to the correct position after a network communication outage to prevent choppy animations
