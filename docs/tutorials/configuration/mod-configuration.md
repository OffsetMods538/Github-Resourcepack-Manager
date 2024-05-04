Once you have the mod and its dependencies installed, you can launch the server once. It should crash telling you to `Fill in the config file`.  
Let's do that!

It should have generated the config file at `serverLocation/config/github-resourcepack-manager.json` and it should look something like this:
```json
{
	"updateMessage": "Server resourcepack has been updated!\nPlease rejoin the server to get the most up to date pack.",
	"serverPort": 8080,
	// Usually shouldn't need changing
	"serverIp": "0.0.0.0",
	// Usually shouldn't need changing
	"webhookPath": "/webhook",
	"githubRef": "refs/heads/master",
	"githubUrl": null,
	// This is *not* the same as githubUrl!
	"resourcepackUrl": null,
	"isPrivate": false,
	"githubUsername": null,
	// PLEASE DON'T SHARE THIS WITH ANYONE EVER !
	"githubToken": null
}
```

## Options
### Update message
This message will be displayed after an update to the resource pack.  
It can contain color codes (including hex), new lines and information about the commit, which you can read more about [here](../../reference/update-message.md).

### Server Port
This is the port that the webserver binds to.
When self-hosting, port-forward this port. When using a Minecraft host, ask them to open up another port and edit this value.

### Server IP
This is the ip that the webserver listens on.  
The default value just tells it to listen on all addresses, so it should be fine.

### Webhook Path
This is the path where the webhook is hosted.  
With the default value it would be at `coolServer.net:[serverPort]/webhook`.  
This should not need to be changed.

### GitHub Ref
This is the git branch/ref that the mod will download the resource pack from.  
Here's an image showing (in red) where the name of your branch is located:  
![Image showing where to find the name of your branch](../../images/repository-branch-location.png)

### GitHub URL
This is the url to your GitHub repository.  
For example: `"https://github.com/OffsetMonkey538/CoolPackYay"`

### Resourcepack URL
This is the url that the mod sends to clients, from which they download the pack.  
You need to set this to `[YOUR_SERVER_IP]:[YOUR_PORT]/pack.zip`  
It needs to end with `/pack.zip` as the mod replaces that with the actual name of the generated pack that it's hosting.

If your server ip is `123.45.67.89` and the port you set in the config is `8080`, then this should be set to `"123.45.67.89:8080/pack.zip"`

### Is Private
This tells the mod that your GitHub repository is private and that it needs to provide credentials to download it.
If your GitHub repository is private, you need to set this to `true`.

### GitHub Username
The username the mod provides for credentials when downloading a private pack.  
If your GitHub repository is private, you need to set this to your GitHub username.

### GitHub Token
The token the mod provides for credentials when downloading a private pack.  
If your GitHub repository is private, you'll need to generate a token and paste it here.

#### Generating the token
To generate a token, go to https://github.com/settings/tokens?type=beta and click the `Generate new token` button.  
That should bring you to the token generation page. Here you'll need to give it a name and, if you want, modify the expiration date and description.

After that, scroll down to `Repository access`.  
Here you'll want to select `Only select repositories`, then click on `Select repositories` and finally search for your private resource pack repository.
![Image showing example Repository access](../../images/token-generation-repository-access.png)

Now scroll down to `Permissions`.  
Here you need to click on `Repository permissions`, then find `Contents`, click on the drop-down next to it that says `Access: No access` and select `Read-only`.  

This will give the token just enough permissions to download the repo contents, but (hopefully) not enough to do anything bad with.  
That being said, you should still *never* share any access tokens with *anyone*.

After that, you can scroll down and press the green `Generate token` button.  
On the next page, copy the token and paste it into the config file.

<br />

And now you can start your server and configure your GitHub repository!
