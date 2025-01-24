# About

If you have any problems or questions, feel free to reach out to me on Discord by clicking [here](https://discord.offsetmonkey538.top) or the Discord icon somewhere on this page :D

## Why?
GitHub Resourcepack Manager is a mod meant **only** for **servers**, this isn't in any way useful for **singleplayer** or opening to LAN!

I saw in one of XisumaVoid's videos ([this one, to be specific](https://youtu.be/cUfTlbO2Tgg?si=yD_3v4F9irS6VCGA&t=206)) that they have something set up on HermitCraft which allows them to update their resource pack through GitHub.  
I tried searching for it on the internet, but couldn't find it, which is why I created this.

## How?

The mod hooks into the Minecraft server (which uses [netty](https://netty.io/)) and hosts an HTTP server on the same port and ip (included library for it [here](https://modrinth.com/plugin/mesh-lib)).  
Said server will listen to POST (webhook) requests sent by GitHub to update and GET requests sent by a client to download the pack.

After the mod is notified of a push to the repository, it uses the [jGit](https://www.eclipse.org/jgit/) library to download a copy of the repository.

Once a copy of the repo is acquired, it will try to find a `pack.mcmeta` file or `packs` folder inside the (configurable) resource pack directory, pack it all up as a `.zip` file and host it.

After that, it modifies the `server.properties` file with the correct sha1 hash and download url, while randomizing the zip file name, otherwise the client thinks it has already downloaded it.

Finally, a notification is sent in chat that there is a newer version of the pack available, which they can get by rejoining the server, *or* by clicking the button in chat, which doesn't require leaving the server. (I guess manually running the `/gh-rp-manager request-pack` command should also work.)
