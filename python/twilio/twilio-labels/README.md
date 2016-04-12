# Twilio Labels, aka *What's That?!*

*What's That?!* is a simple Python application that uses Twilio and the Google Cloud Vision API to let people text an image to a number and receive a response that tells them what the Vision API sees in the first image they send.

*What's That?!* uses a [Flask](http://flask.pocoo.org/) server to receive text messages, query the API, and send a response to the user.

## Prerequisites

1. Create a project in the [Google Cloud Platform Console](https://console.cloud.google.com).

2. [Enable billing](https://console.cloud.google.com/project/_/settings) for your project.

3. Enable the Vision APIs.

4. Install the [Google Cloud SDK](https://cloud.google.com/sdk)

       	$ curl https://sdk.cloud.google.com | bash
        $ gcloud init

5. Create and set up a [Twilio account](https://www.twilio.com/try-twilio) and number capable of sending and receiving MMS (make sure to whitelist your number with Twilio).

6. Install the python requirements found in `requirements.txt`

		$ pip install -r requirements.txt

## Test *What's That?!*

1. Run the sample
		
		$ python whats_that.py

2. Text an image to your Twilio number

3. See the response!

## Clean-up

1. Stop the server (`ctrl-c` or `kill` the process)

2. Delete images from Twilio [using the API](https://www.twilio.com/help/faq/sms/how-do-i-delete-messages-message-media-or-message-bodies); you can write a script to do this

