# GitHub Resourcepack Manager
[![discord-singular](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/social/discord-singular_vector.svg)](https://discord.offsetmonkey538.top/)
[![modrinth](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/modrinth_vector.svg)](https://modrinth.com/mod/github-resourcepack-manager)  
[![requires-monkeylib538](https://raw.githubusercontent.com/OffsetMods538/MonkeyLib538/master/images/requires_monkeylib538.png)](https://modrinth.com/mod/monkeylib538)

GitHub Resourcepack Manager is a mod meant for **dedicated servers**.

I saw in one of XisumaVoid's videos that they have a mod on HermitCraft which allows them to edit their resource pack on GitHub and have it automatically updated without restarting the server, but I couldn't find a download link anywhere, so I made my own.

If you are using a hosting service, then you should ask their support if they allow using this mod. Some hosts may not like you hosting an HTTP server.

#### Table of contents
- [How it works](#how-it-works)
    * [HTTP server](#http-server)
    * [Downloading the pack](#downloading-the-pack)
    * [Zip it up](#zip-it-up)
    * [Host it](#host-it)
- [Installation](#installation)
    * [Installing the mod](#installing-the-mod)
    * [Creating the GitHub repository](#creating-the-github-repository)
    * [Configuring the mod](#configuring-the-mod)
        - [Server Port](#server-port)
        - [Server IP](#server-ip)
        - [Webhook Path](#webhook-path)
        - [GitHub Ref](#github-ref)
        - [GitHub Url](#github-url)
        - [Is Private](#is-private)
        - [GitHub Username](#github-username)
        - [Github Token](#github-token)
    * [Configuring the server](#configuring-the-server)
    * [Creating the webhook](#creating-the-webhook)
    * [Testing it out](#testing-it-out)
    * [Adding packs](#adding-packs)

## How it works
If you don't care, you can skip to [installation](#installation) TODO: figure out how to link to a different header thingy

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

I expect that you already have a GitHub account set up.


Now, I've made a nice template repository, so you can easily get started with your resource packs!

You need to visit this link: https://github.com/OffsetMods538/Github-Resourcepack-Manager-Pack-Template  
There should be a green button that says `Use this template`. Click on it and select `Create a new repository`.  
This will bring you to the repository creation page. You will need to fill in a name and, if you want, you may make the repository private.  
Now just press the `Create repository` button.

![image](https://github.com/OffsetMods538/Github-Resourcepack-Manager/assets/71213040/19d0c175-ff63-4e3c-9491-1584643f2bba)

### Configuring the mod
Once you launch the server with the mod installed once, it will generate the config file. It should be at `serverLocation/config/github-resourcepack-manager.json` and look something like this:
```json
{
	"serverPort": 8080,
	"serverIp": "0.0.0.0",
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
###### Default value: 8080
This is the port that the webserver will be hosted on. If you are self-hosting, then you'll want to port-forward this port. If you're using a Minecraft host, then you'll want to ask them for a second port and edit this value.

#### Server IP
###### Default value: "0.0.0.0"
The default value should be fine.

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

### Configuring the server
You will need to edit the `server.properties` file as well.  
Before this, make sure to stop your server.

Open the `server.properties` file and find where it says `resource-pack=`. You need to set this to the IP of your server + the port set in the config + `/pack.zip` at the end instead of the webhook path.  
For example: "http://123.45.67.89:8080/pack.zip"  
You may also want to change `require-resource-pack` to `true`

### Creating the webhook
The mod needs a webhook to be added to your GitHub repository to know when there's an update to it.

You need to go to your repository and click on the `Settings` tab.  
On the sidebar you'll need to find `Webhooks`.

On the `Webhooks` page, click on the `Add webhook` button.  
On the next page, you'll want to change the `Payload URL` to your server IP + the port you set in the config + the webhook path set in the config.  
For example: "http://123.45.67.89:8080/webhook"

You'll also want to change the `Content type` to `application/json`.  
Now it should look something like this:  
![image](https://github.com/OffsetMods538/Github-Resourcepack-Manager/assets/71213040/6d402ab0-297f-4621-9eb0-7496d2b287da)  
**Before** clicking on `Add webhook`, make sure your server is running.

### Testing it out
Once you join the server, you should be prompted to download a resource pack.  
When you go to `Escape -> Options -> Resource Packs`, you should see the server resource pack.

Let's try changing its name and description!  
Open your GitHub repository and navigate to `packs/0-base/pack.mcmeta`. You should see an edit button.  
Once you've finished editing your `pack.mcmeta` file you can press the `Commit changes...` button and commit your changes.

Now when you look back at the game, you should see (it might take some time) this message in chat:
```text
Server resourcepack has been updated!
Please rejoin the server to get the most up to date pack.
```

Once you disconnect and reconnect, you should hopefully see the changed description and name be applied to the client.  
If not then join my discord and scream at me for making a mod that doesn't work :D

### Adding packs
While the server is running and players are online, you can try adding another pack. In this example I'll use the [Fresh Animations](https://modrinth.com/resourcepack/fresh-animations) resource pack.  
Alright so what you need to do, is download/create a resource pack, then you need to rename the `.zip` file to contain a priority.

"What's a priority?" you may ask, well a priority tells the mod which packs are more or less important.

**Important**: A higher priority value means that the pack will be applied *earlier*, which means it can be *overwritten* by ones with higher priority values.  
This means that when a pack is more important, it's actually less important and if two packs contain the same file, the one with a higher priority will be applied first, and then the "lower" priority pack will overwrite it later.  
I know this might not make sense at first, but it makes sense.

So in our example with the Fresh Animations pack, you would rename it to `10-FreshAnimations_v1.9.zip` to make it have a priority of 10.
Once you've renamed it, you need to upload the pack into the `packs` folder in your repository.

Once you commit the change, you should see that after some time the server will broadcast the same message as before and once you rejoin, you should have the pack applied!  
If not then, again, join my discord and scream at me for making a mod that doesn't work :D
