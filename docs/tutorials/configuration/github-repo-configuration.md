The mod listens for a webhook (POST request) sent by GitHub to know when the repository is updated.  
The last thing you need to do now, is adding the webhook.

You need to go to your repository page on GitHub and open the `Settings` tab.  
On the sidebar you need to find and click on `Webhooks`.  
Here click on the `Add webhook` button.

Now that you're on the webhook creation page, you'll need to fill in some values.  
For `Payload URL` put `http://[YOUR_SERVER_IP]:[THE_MOD_PORT]/[THE_WEBHOOK_PATH]`.  
For example if your server ip is `123.45.67.89`, the port `8080` and webhook path `/webhook`, the url would be `http://123.45.67.89:8080/webhook`.

Then set `Content type` to `application/json`.

This is what it should look like:
![Image showing webhook creation page filled in](../../images/repository-webhook-creation.png)

Now you can press `Add webhook` button and you should be done!