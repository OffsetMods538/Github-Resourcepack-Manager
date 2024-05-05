If you've finished setting everything up, then it's time to test and see if it works!

First join the server.  
Now when you go into the resource packs menu (++escape++ -> Options -> Resource Packs), you should see the server resource pack.

## Changing name and description
Let's try changing its name and description!  
Open up your GitHub repository and navigate to either `packs/0-base/pack.mcmeta` if using a multi pack setup or just `pack.mcmeta` if using a single pack one.  
There should be an edit button, click on it and change the name and description of the pack.  
After you've done that, you can press the `Commit changes..` button and commit your changes.

Now when you go back into the game, you should see the message you set in the config file appear in chat.

Once you disconnect and reconnect, navigate to the resource packs menu again, and you should hopefully see that the name and description have changed!

## Useful
I'd recommend using the [GitHub Desktop](https://desktop.github.com/) app to make your life easier when editing the pack.

## Adding a pack to a multi pack setup
Let's try adding the [Fresh Animations](https://modrinth.com/resourcepack/fresh-animations) pack to our server.  
First download the pack itself, then you need to prefix it with a priority (which you can read more about [here](../reference/priority.md)).  
For example I've renamed it to `10-FreshAnimations_v1.9.zip`.  
Now all you need to do is just upload this pack to your repository, and you should hopefully receive a message in chat notifying of the update.  
Now when you disconnect and rejoin, you should hopefully have fresh animations applied!
