
# Text Detection using the Vision API

## Introduction

This example uses the [Cloud Vision API](https://cloud.google.com/vision/) to
detect text within images, stores this text in an index, and then lets you
query this index.

This example uses `TEXT_DETECTION` Vision API requests to build an [inverted
index](https://en.wikipedia.org/wiki/Inverted_index) from the
[stemmed](https://en.wikipedia.org/wiki/Stemming) words found in the images,
and stores that index in a [Redis](http://redis.io/) database.  The example
uses the [nltk](http://www.nltk.org/index.html) library for finding
[stopwords](https://en.wikipedia.org/wiki/Stop_words) and doing stemming. The
resulting index can be queried to find images that match a given set of words,
and to list text that was found in each matching image.

For the full code for this example, see the [Text Detection
Example](textindex.py).

## Audience

This documentation is designed for people familiar with basic Python
programming, though even without any Python knowledge, you should be able to
follow along. There are many Python tutorials available on the Web.

This conceptual documentation is designed to let you quickly start exploring
and developing applications with the Google Cloud Vision API. You may find it
useful to first look through the [Label Detection](https://cloud.google.com/vision/docs/label-tutorial) tutorial,
which explores use of the Vision API in more detail.

## Initial Setup

### Enable the Cloud Vision API for Your Project ###

Before you start this tutorial, see the [Getting
Started](https://cloud.google.com/vision/docs/getting-started) documentation
for information about enabling the Cloud Vision API for your project.

### Download the Example Code

To download the example code, visit
[https://dev-partners.googlesource.com/cloud/vision/+archive/master/python/text.tar.gz](https://dev-partners.googlesource.com/cloud/vision/+archive/master/python/text.tar.gz).
You must be whitelisted for access.

This example requires Python 2.7.

### Install the Libraries Used by the Example

Install the python package management system `pip` if necessary:

```
$ sudo easy_install pip
```

Run the
[requirements.txt](https://dev-partners.googlesource.com/cloud/vision/+/master/python/text/requirements.txt)
file from the zip file that you downloaded:

```
$ pip install -r requirements.txt
```

to install necessary libraries.

Then, install some data needed for `nltk`:

```shell
$ python -m nltk.downloader stopwords
$ python -m nltk.downloader punkt
```

(This data will be installed into a `nltk_data` directory under your home directory.)

### Set Up to Authenticate With Your Project's Credentials

Next, set up to authenticate with the Cloud Vision API using your project's
service account credentials. See the
[Cloud Platform Auth Guide](https://cloud.google.com/docs/authentication#developer_workflow) for more information. E.g., to authenticate locally, set
the `GOOGLE_APPLICATION_CREDENTIALS` environment variable to point to your
downloaded service account credentials before running this example:

    export GOOGLE_APPLICATION_CREDENTIALS=/path/to/your/credentials-key.json

If you do not do this, you will see an error that looks something like this when
you run the script:
`HttpError 403 when requesting
https://vision.googleapis.com/v1/images:annotate?alt=json returned
"Project has not activated the vision.googleapis.com API. Please enable the API
for project ..."`

### Install and Start Up a Redis server

This example uses a [redis](http://redis.io/) server, which must be up and
running before you start the indexing.  To install Redis, follow the
instructions on the [download page](http://redis.io/download), or install via a
package manager like [homebrew](http://brew.sh/) or `apt-get` as appropriate
for your OS.

The example assumes that the server is running on `localhost`, on the default
port, and it uses [redis
dbs](http://www.rediscookbook.org/multiple_databases.html) 0 and 1 for its data.
Edit the example code before you start if your redis settings are different.

## Quick Start: Running the Example ##

If you'd like to get the example up and running before we dive into the
details, here are the steps.

1. Ensure that your redis server is running.
2. Index a directory of images by passing the image name as an argument to
   `textindex.py`:

```
$ ./textindex.py <path-to-image-directory>
```

3. Wait for the indexing to finish.  The process is resumable, in case the
   indexing is interrupted.
4. Query the index to find images that match a given set of keywords.  An easy
   way to do this is from the Python interpreter, as shown below. Pick a word
   that you know is in your images.  If you list multiple words, the conjunction
   of hits will be returned— that is, images that contain all the given
   keywords.

```shell
$ python
...
>>> import textindex
>>> index = textindex.Index()
>>> index.print_lookup('cats')
...
>>> index.print_lookup('cats', 'dogs')
```


## Text Detection Example Walkthrough

We'll walk through this example and its use of the Vision API's
`TEXT_DETECTION` API request, which uses uses OCR to find any text
in an image.

### Building the Vision API Client, and Processing a Request

We import some standard libraries: `base64`to encode image files for
transmission as JSON text, and `httplib2` for creating an `Http()` object to
handle requests and responses.

To use the Cloud Vision API, we'll also import the `discovery` module within
the `googleapiclient` library. As well, we'll import the `GoogleCredentials`
module within `oauth2client.client` to handle authentication to the service.

The constructor for the `VisionApi` class builds the client object.

The `VisionApi.detect_text` method constructs the API request.  The request
specifies type `TEXT_DETECTION`, which tells the Vision API that we're making a
request to do OCR on the given image.  The image contents are base64-encoded and
passed as part of the request. See
[the Label tutorial](https://cloud.google.com/vision/docs/label-tutorial) for
more detail on using the Vision API.

```python
import base64
import os
import re
import sys

from googleapiclient import discovery
from googleapiclient import errors
import nltk
from nltk.stem.snowball import EnglishStemmer
from oauth2client.client import GoogleCredentials
import redis

DISCOVERY_URL='https://{api}.googleapis.com/$discovery/rest?version={apiVersion}'

class VisionApi:
    """Construct and use the Google Vision API service."""

    def __init__(self, api_discovery_file='vision_api.json'):
        self.credentials = GoogleCredentials.get_application_default()
        self.service = discovery.build(
            'vision', 'v1', credentials=credentials,
            discoveryServiceUrl=DISCOVERY_URL)


    def detect_text(self, image_file, num_retries=3, max_results=6):
        """Uses the Vision API to detect text in the given file.
        """
        image_content = image_file.read()

        batch_request = [{
            'image': {
                'content': base64.b64encode(image_content)
            },
            'features': [{
                'type': 'TEXT_DETECTION',
                'maxResults': max_results,
            }]
        }]
        request = self.service.images().annotate(
            body={'requests': batch_request})

        try:
            response = request.execute(num_retries=num_retries)
            if ('responses' in response
               and 'textAnnotations' in response['responses'][0]):
                text_response = response['responses'][0]['textAnnotations']
                return text_response
            else:
                return []
        except errors.HttpError, e:
            print("Http Error for %s: %s" % (image_file, e))
        except KeyError, e2:
            print("Key error: %s" % e2)
```

The Vision API returns its response in JSON. If text was successfully
discovered, information about the text will be in the `textAnnotations` field
of the first element of the `responses` list. If text was found, the
`textAnnotations` field itself will hold a list of one or more discovered text
blocks. For each text block, it will include the `description` (the extracted
text), as well as other information, like the detected text's bounding box.  It
will look something like the following:

```
{u'textAnnotations': [{u'locale': u'eo', u'description': u"...discovered
text....", u'boundingPoly': {u'vertices': [{u'y': 32, u'x': 21}, {u'y': 32,
u'x': 954}, {u'y': 685, u'x': 954}, {u'y': 685, u'x': 21}]}}]}]}
```

We'll only use the `description` field in this example.

### Building an Inverted Index from the Image OCR results

The `Index` class builds an inverted index using Redis&mdash; indexing the
files in which each keyword stem was found&mdash; and supports queries on the
index. In the `extract_descriptions` function, we concatenate all the
`description` fields from the `textAnnotations` response list to create a
"document" string containing all the text found in the image.  Then, the
`Index.add()` method is called with the filename and the "document".

```python
def extract_descriptions(input_filename, index, texts):
    """Gets and indexes the text that was detected in the image."""
    if texts:
        document = ''
        for text in texts:
            try:
                document += text['description']
            except KeyError, e:
                print('KeyError: %s' % text)
        index.add(input_filename, document)
        sys.stdout.write('.')  # Output a progress indicator.
        sys.stdout.flush()
    else:
        if texts == []:
            print('%s had no discernible text.' % input_filename)
            index.set_contains_no_text(input_filename)
```


In `Index.add()`, [stems](https://en.wikipedia.org/wiki/Stemming) are generated
for each non-stopword in the document, and for each stem, the filename is
added&mdash; using Redis&mdash; to the set of filenames associated with that
stem (keyword).  This builds the inverted index.  You can uncomment the "print"
statement in `Index.add()` to see the words found for each image.

The Index.lookup() method then takes a set of search keywords, stems them,
queries Redis to find the set of files in which each stem was found, and
returns the intersection of the results. Because this tutorial focuses on the
Vision API, we won't go through these methods in detail; find them in the
[example code](textindex.py).

### Putting the Pieces Together

Run the script like this to index a directory of images, where at least some of
the images have text in them:

```
$ ./textindex.py <path-to-image-directory>
```

(A "memes" site is a good place to find sample images).

The Vision API client object is created, and the `Index()` object is created.
Then, for each file in the directory that has not already been processed, the
`get_text_from_file()` function is passed these two objects and the unprocessed
file. (We're also using Redis to track the files already processed). Any text
is extracted and added to the index.

```python
def get_text_from_file(vision, index, input_filename):
    """Call the Vision API on a file and index the results."""
    with open(input_filename, 'rb') as image:
        texts = vision.detect_text(image)
        extract_descriptions(input_filename, index, texts)


def main(input_dir):
    """Walk through all the not-yet-processed image files in the given
    directory, extracting any text from them and adding that text to an
    inverted index.
    """
    # Create a client object for the Vision API
    vision = VisionApi()
    # Create an Index object to build query the inverted index.
    index = Index()

    fileslist = []
    # Recursively construct a list of all the files in the given input
    # directory.
    for folder, subs, files in os.walk(input_dir):
        for filename in files:
            fileslist.append(os.path.join(folder, filename))

    for filename in fileslist:
        # Look for text in any files that have not yet been processed.
        if index.document_is_processed(filename):
            continue
        get_text_from_file(vision, index, filename)
```

Because we're using Redis to track the files already processed, this script can
be paused and resumed; it can also be run multiple times on the same directory,
if the Redis database is retained.

Once you've started to populate the inverted index, use the `Index.lookup()` or
`Index.print_lookup()` methods, as described above in the "Quick Start"
section, to list all the indexed files that match a given stem keyword,
including the text that was identified in each matched file.

The query results will look something like the following, listing the text
detected in each of the images in which the query text was found. E.g., the
example below shows a query on the word 'nature'. The query returned all the
text found in both the images that contained 'nature'.

```shell
$ python
...
>>> import textindex
>>> index = textindex.Index()
>>> index.print_lookup('nature')
***Image /path/to/image1.jpg has text:
The Nature
Conservancy
PHOTO: JONATHAN HEY

***Image /path/to/image2.jpg has text:
T-Mobile
19:00
usinterior
9 Grand Teton National Park
nature
228489 likes
nature Hill Inlet, Whitehaven Beach, Queensland, AU
l Photography by OPaul Pichugin (@paulmp)
view all 2669 comments
```


You can pass a list of query terms, and images that match the conjunction are
returned. E.g., the following query would match `image2.jpg` above:
`index.print_lookup('nature', 'beaches')`

