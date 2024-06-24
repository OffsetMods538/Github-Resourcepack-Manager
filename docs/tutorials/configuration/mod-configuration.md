Once you have the mod and its dependencies installed, you can launch the server once. It should crash telling you to `Fill in the config file`.  
Let's do that!

It should have generated the config file at `serverLocation/config/github-resourcepack-manager.json` and it should look something like this:
```json
{
	"packUpdateMessage": "Server resourcepack has been updated!\nPlease rejoin the server to get the most up to date pack.",
	// The port that the *webserver* binds to. *NOT* the same as your minecraft servers port
	"webServerBindPort": 8080,
	// Usually shouldn't need changing
	"webServerBindIp": "0.0.0.0",
	// Usually shouldn't need changing
	"webhookPath": "/webhook",
	// The public ip of your server (123.45.67.89 or play.coolserver.net)
	"serverPublicIp": null,
	// Should be "refs/heads/[YOUR BRANCH NAME HERE]"
	"githubRef": "refs/heads/master",
	"githubUrl": null,
	"isPrivate": false,
	"githubUsername": null,
	// PLEASE DON'T SHARE THIS WITH ANYONE EVER
	"githubToken": null
}
```

## Value types
If you have a basic understanding of json values, you can skip this section and go straight to [options](#options).

In the next section, I will be specifying which type an option is, but first I'll explain the different types:

### Integer
An integer is a number without a decimal point.  

For example `1234` **is** an integer, but `12.34` **is not**.

### Boolean
A boolean can be either `true` or `false`.

### String
A string represents text.  
The text has to be surrounded by double quotes (`"`).

Example of valid string:
```json
"githubUrl": "https://github.com/OffsetMonkey538/CoolPack"
```

Example of invalid string:
```json
"githubUrl": https://github.com/OffsetMonkey538/CoolPack
```

A string can also be `null`.  
Null basically means that the value doesn't exist.  
In most cases using `null` for a string will result in an unexpected error, but I will mark values that can be null without a crash.

## Options
### packUpdateMessage
!!! info ""
    Type: `String`  
    Default Value: `"Server resourcepack has been updated!\nPlease rejoin the server to get the most up to date pack."`  
    May be null: `no`
This message will be displayed after an update to the resource pack.  
It can contain color codes (including hex), new lines and information about the commit, which you can read more about [here](../../reference/update-message.md).

### webServerBindPort
!!! info ""
    Type: `Integer`  
    Default Value: `8080`  
    May be null: `no`
This is the port that the webserver binds to.  
When self-hosting, port-forward this port. When using a Minecraft host, ask them to open up another port and edit this value.

### webServerBindIp
!!! info ""
    Type: `String`  
    Default Value: `"0.0.0.0"`  
    May be null: `no`
This is the ip that the webserver binds to.  
The default value just tells it to listen on all addresses, so it should be fine.

### webhookPath
!!! info ""
    Type: `String`  
    Default Value: `"/webhook"`  
    May be null: `no`
This is the path where the webhook is hosted.  
With the default value it would be at `coolServer.net:[webServerBindPort]/webhook`.  
This should not need to be changed.

### serverPublicIp
!!! info ""
    Type: `String`  
    Default Value: `null`  
    May be null: `no`
This is your servers public ip. This is sent to the clients, so they know where to download the pack from.  
For example: `"1283.45.67.89""` or `"play.offsetmonkey538.top"`

### githubRef
!!! info ""
    Type: `String`  
    Default Value: `"refs/heads/master"`  
    May be null: `no`
This is the git branch/ref that the mod will download the resource pack from.  
Here's an image showing (in red) where the name of your branch is located:  
![Image showing where to find the name of your branch](../../images/repository-branch-location.png)

### githubUrl
!!! info ""
    Type: `String`  
    Default Value: `null`  
    May be null: `no`
This is the url to your GitHub repository.  
For example: `"https://github.com/OffsetMonkey538/CoolPackYay"`

### isPrivate
!!! info ""
    Type: `Boolean`  
    Default Value: `false`  
    May be null: `no`
This tells the mod that your GitHub repository is private and that it needs to provide credentials to download it.
If your GitHub repository is private, you need to set this to `true`.

### githubUsername
!!! info ""
    Type: `String`  
    Default Value: `null`  
    May be null: `yes`
The username the mod provides for credentials when downloading a private pack.  
If your GitHub repository is private, you need to set this to your GitHub username.

### githubToken
!!! info ""
    Type: `String`  
    Default Value: `null`  
    May be null: `yes`
The token the mod provides for credentials when downloading a private pack.  
If your GitHub repository is private, you'll need to generate a token and paste it here.

#### Generating the token
To generate a token, go [here](https://github.com/settings/tokens?type=beta) and click the `Generate new token` button.  
This should bring you to the token generation page. Here you'll need to give it a name and, if you want, modify the expiration date and description.

After that, scroll down to `Repository access`.  
Here you'll want to select `Only select repositories`, then click on `Select repositories` and finally search for your private resource pack repository.
![Image showing example Repository access](../../images/token-generation-repository-access.png)

Now scroll down to `Permissions`.  
Here you need to click on `Repository permissions`, then find `Contents`, click on the drop-down next to it that says `Access: No access` and select `Read-only`.  

This will give the token just enough permissions to download the repo contents, but (hopefully) not enough to do anything bad with.  
That being said, you should still *never* share any access tokens with *anyone*.

After that, you can scroll down and press the green `Generate token` button.  
On the next page, copy the token and paste it into the config file.

## Done ;D

And now you can start your server and configure your GitHub repository!
