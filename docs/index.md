# About

If you have any problems or questions, feel free to reach out to me on Discord by clicking [here](https://discord.offsetmonkey538.top) or the Discord icon at the bottom of your screen :D

## Why?
GitHub Resourcepack Manager is a mod meant **only** for **dedicated servers**!

I saw in one of XisumaVoid's videos ([here](https://youtu.be/cUfTlbO2Tgg?si=yD_3v4F9irS6VCGA&t=206)) that they have a mod on HermitCraft which allows them to update their resource pack through GitHub.  
I tried searching for it on the internet, but couldn't find it, which is why I created this.

## How?

This mod hosts a webserver, on the same port as the Minecraft server, using [netty](https://netty.io/), which listens to POST requests (webhook) sent by GitHub.  
After the mod is notified of an update to the GitHub repo, it uses [jGit](https://www.eclipse.org/jgit/) to download the contents of the GitHub repository.  
Once it has the repo downloaded it will either:

1. If it finds a `pack.mcmeta` file in the resource pack directory: Pack the content of the repo into a `.zip` file and host it on the webserver.
2. If it finds a `packs` folder in the resource pack directory: Unpack all the source packs and then pack those into a `.zip` file and host it on the webserver.

After everything is done, it sends a message in chat notifying the players that there's a new pack available.
