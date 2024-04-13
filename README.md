# GitHub Resourcepack Manager
[![discord-singular](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/social/discord-singular_vector.svg)](https://discord.offsetmonkey538.top/)
[![modrinth](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/modrinth_vector.svg)](https://modrinth.com/mod/github-resourcepack-manager)  
[![requires-monkeylib538](https://raw.githubusercontent.com/OffsetMods538/MonkeyLib538/master/images/requires_monkeylib538.png)](https://modrinth.com/mod/monkeylib538)

###### Sorry for any spelling mistakes or incorrect stuff in the readme, I've been sitting at the computer for the whole day working on this mod, i'm tired af and I'm *not* gonna reread this thing again while paying attention to grammar, I still have to make an icon and set up the modrinth page oh god

GitHub Resourcepack Manager is a mod meant for **dedicated servers**.

I saw in one of XisumaVoid's videos that they have a mod on Hermitcraft which allows them to edit their resource pack on GitHub and have it automatically updated without restarting the server, but I couldn't find a download link anywhere, so I made my own.

TODO: that uhh table of contents or whatever thing

## How it works
If you don't care, you can skip to [installation]() TODO: figure out how to link to a different header thingy

### HTTP server
The mod hosts an HTTP server using the [Undertow](https://undertow.io/) library, which *serves* 2 purposes:
1. Act as a webhook to listen for push events to the GitHub repository.
2. Host the resource pack for the clients to download.

### Downloading the pack
The mod uses the [jGit](https://www.eclipse.org/jgit/) library to download the GitHub repository.

### Zip it up
After the pack content has been downloaded from GitHub, we use Java's built-in zip file handling to pack it up into a nice zip file.

### Host it
After that the zip is hosted on the HTTP server mentioned previously.

## Installation
Now for the juicy bit...


### Installing the mod
This is the easy bit, all you need to do is download this mod from Modrinth and also the [MonkeyLib538](https://modrinth.com/mod/monkeylib538) library mod. Then just put them into your server's mods folder.

### Creating the GitHub repository
Now you'll need to create a [GitHub](https://github.com) repository to host the pack files.  
You can follow [this](https://docs.github.com/en/repositories/creating-and-managing-repositories/quickstart-for-repositories#create-a-repository) tutorial provided by GitHub to create a repository. Just make sure to stop and come back once the repository is created and don't commit anything yet.  
You may make the repository private if you want to.

### Configuring the mod
Once you launch the server with the mod installed once, it will generate the config file. It should be at `serverLocation/config/github-resourcepack-manager.json` and look something like this:
```json
{
	"serverPort": 8080,
	"webhookPath": "/webhook",
	"githubRef": "refs/heads/master",
	"githubUrl": null,
	"isPrivate": false,
	"githubUsername": null,
	// PLEASE DON'T SHARE THIS WITH ANYONE EVER
	"githubToken": null
}
```
Let me explain the different options a bit more deeply.
#### Server Port
###### Default value: `8080
This is the port that the webserver will be hosted on. If you are self-hosting, then you'll want to port-forward this port. If you're using a Minecraft host, then you'll want to ask them for a second port and edit this value.

#### Webhook Path
###### Default value: "/webhook"
The default value is usually fine for this.  
This will be used later on for configuring the webhook on your GitHub repository.

#### GitHub Ref
###### Default value: "refs/heads/master"
The default value is usually fine for this.  
This is the branch that the mod will listen for updates on.

#### GitHub Url
###### Default value: null
This needs to be set to the url of your GitHub repository.  
Example values:
- "https://github.com/OffsetMonkey538/CoolServerPack"
- "https://github.com/OffsetMonkey538/CoolServerPack.git"

#### Is Private
###### Default value: false
This value needs to be set to `true` **only** if the GitHub repository is private.

#### GitHub Username
###### Default value: null
This only needs to be set if the GitHub repository is private.  
In that case it will have to be set to your GitHub username.  
For example: "OffsetMonkey538"

#### Github Token
###### Default value: null
This only needs to be set if the GitHub repository is private.  
In that case it will have to be set to a personal access token with access to the GitHub repository.

##### Generating a token
If your GitHub repository is private, you will need to generate a personal access token.  
To do that go to https://github.com/settings/tokens?type=beta and press the `Generate new token` button.
On the next page, fill in a name for your token, you may also want to change the expiration date and mark the data on your calendar as you'll need to make a new token when this one expires.  
Under the `Repository access` section select `Only select repositories` and select your repository.  
![image](https://github.com/OffsetMods538/Github-Resourcepack-Manager/assets/71213040/41df9787-ca56-4cf5-be84-5a5e866b6605)

Then under the `Permissions` section, set `Contents` to `Read-only` access.  
Now you can generate the token and copy the token into the config file.  
**IMPORTANT: You should NEVER share ANY access tokens with ANYONE**

### Creating the webhook
The mod needs a webhook to be added to your GitHub repository to know when there's an update to it.

You need to go to your repository and click on the `Settings` tab.  
On the sidebar you'll need to find `Webhooks`.

On the `Webhooks` page, click on the `Add webhook` button.  
On the next page, you'll want to change the `Payload URL` to your server ip + the port you set in the config + the webhook path set in the config.  
For example: "http://123.45.67.89:8080/webhook"

You'll also want to change the `Content type` to `application/json`.  
Now it should look something like this:  
![image](https://github.com/OffsetMods538/Github-Resourcepack-Manager/assets/71213040/6d402ab0-297f-4621-9eb0-7496d2b287da)  
**Before** clicking on `Add webhook`, make sure your server is running.

### Configuring the server
You will need to edit the `server.properties` file as well.  
Before this, make sure to stop your server.

Open the `server.properties` file and find where it says `resource-pack=`. You need to set this to the same url as the webhook, just with `/pack.zip` at the end instead of the webhook path.  
For example: http://123.45.67.89:8080/pack.zip"  
You may also want to change `require-resource-pack` to `true`

### Creating a pack
You can now create a pack and upload it to your GitHub repository.  
Once you launch the server, you should see that when a client joins, it is prompted to install the resource pack.

### Testing it out
While the server is running and players are online, you can try editing something in the pack.  
Once you commit your change, you should see that after some time the server will broadcast in chat that:
```
Server resourcepack has been updated!
Please rejoin the server to get the most up to date pack.
```

Once you disconnect and reconnect, you should hopefully see the changes you made to the resource pack be applied to the client.  
If not then join my discord and scream at me for making a mdo that doesn't work :D
