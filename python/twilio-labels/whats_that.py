#!/usr/bin/env python
# Copyright 2015 Google Inc. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Flask / Twilio reqs
from flask import Flask, request, redirect
import twilio.twiml

# GCP Vision reqs
import base64
import httplib2
from googleapiclient import discovery
from oauth2client.client import GoogleCredentials

# App reqs
import requests
from random import randint

DISCOVERY_URL = 'https://{api}.googleapis.com/$discovery/rest?version={apiVersion}&labels=TRUSTED_TESTER'
ACCEPTABLE_FILE_TYPES = ["image/jpeg", "image/png", "image/jpg"]
DEFAULT_PRETEXT = ("Your message has been passed to the Google Cloud "
                   "Vision API for processing.\n Images are not stored in "
                   "Google Cloud Platform, and will periodically need to be "
                   "deleted from Twilio.")

app = Flask(__name__)


@app.route("/", methods=['GET', 'POST'])
def receive_message():
    labels = None

    # Go through and process attachments
    for i in range(int(request.values.get('NumMedia', None))):

        media_content_type = request.values.get('MediaContentType%i' % i, None)

        # First, check to see if we can use the attachment
        if media_content_type in ACCEPTABLE_FILE_TYPES:

            # Go get the image
            media_url = request.values.get('MediaUrl%i' % i, None)

            image = parse_attachment(media_url)

            # Query the API
            labels = get_labels(image)

            # We're only looking at the first image
            break

    # Construct the response
    resp = construct_message(labels)

    # And it's off!
    return str(resp)


def construct_message(labels):

    # We'll use this to construct our response
    response_text = DEFAULT_PRETEXT
    label_desc = ""
    remarks = ['Swanky!', 'Meh.', 'Alright.', 'So what?', 'NBD.', 'Cool!']

    # Go through labels and turn them into text of the response
    for i in range(len(labels)):

        # We've got an answer! Let's tell them about it
        label_desc += '\n%s Score is %s for %s' % (remarks[randint(0, 5)],
                                                   labels[i]['score'],
                                                   labels[i]['description'])

    # Add the prefix
    response_text += label_desc
    resp = twilio.twiml.Response()
    resp.message(response_text)
    return resp


def parse_attachment(media_url):

    # Get the image from teh interwebz
    r = requests.get(media_url)
    return r.content


def get_labels(image, num_retries=3, max_results=3):

    labels = ""

    # Set up the service that can access the API
    http = httplib2.Http()
    credentials = GoogleCredentials.get_application_default().create_scoped(
        ['https://www.googleapis.com/auth/cloud-platform'])
    credentials.authorize(http)
    service = discovery.build('vision', 'v1alpha1', http=http,
                              discoveryServiceUrl=DISCOVERY_URL)

    # Prepare the image for the API
    image_content = base64.b64encode(image)

    # Construct the request
    service_request = service.images().annotate(
        body={
            'requests': [{
                'image': {
                    'content': image_content
                },
                'features': [{
                    'type': 'LABEL_DETECTION',
                    'maxResults': max_results,
                }]
            }]
        })

    # Send it off to the API
    try:
        response = service_request.execute(num_retries=num_retries)
        if('responses' in response and
                'labelAnnotations' in response['responses'][0]):
            labels = response['responses'][0]['labelAnnotations']

            return labels
        else:
            return []
    except errors.HttpError, e:
        return []
    except KeyError, e2:
        print "Key error: %s" % e2


if __name__ == "__main__":

    # Change to app.run(host='0.0.0.0') for an externally visible server
    app.run()
