
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

Download the code from this repository.

If you have [`git`](https://git-scm.com/) installed, you can do this
by executing the following command:

```shell
$ git clone https://github.com/GoogleCloudPlatform/cloud-vision.git
```

This will download the repository of samples into the directory
`cloud-vision`.

Otherwise, github offers an
[auto-generated zip file](https://github.com/GoogleCloudPlatform/cloud-vision/archive/master.zip) of the
`master` branch, which you can download and extract.

Either method will include the desired subdirectory
`python/text`.  Change to the `python/text` directory under `cloud-vision`.
The rest of the tutorial assumes this as your working directory.


This example has been tested with Python 2.7 and 3.4.

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

DISCOVERY_URL = 'https://{api}.googleapis.com/$discovery/rest?version={apiVersion}'  # noqa
BATCH_SIZE = 10


class VisionApi:
    """Construct and use the Google Vision API service."""

    def __init__(self, api_discovery_file='vision_api.json'):
        self.credentials = GoogleCredentials.get_application_default()
        self.service = discovery.build(
            'vision', 'v1', credentials=self.credentials,
            discoveryServiceUrl=DISCOVERY_URL)

    def detect_text(self, input_filenames, num_retries=3, max_results=6):
        """Uses the Vision API to detect text in the given file.
        """
        images = {}
        for filename in input_filenames:
            with open(filename, 'rb') as image_file:
                images[filename] = image_file.read()

        batch_request = []
        for filename in images:
            batch_request.append({
                'image': {
                    'content': base64.b64encode(
                            images[filename]).decode('UTF-8')
                },
                'features': [{
                    'type': 'TEXT_DETECTION',
                    'maxResults': max_results,
                }]
            })
        request = self.service.images().annotate(
            body={'requests': batch_request})

        try:
            responses = request.execute(num_retries=num_retries)
            if 'responses' not in responses:
                return {}
            text_response = {}
            for filename, response in zip(images, responses['responses']):
                if 'error' in response:
                    print("API Error for %s: %s" % (
                            filename,
                            response['error']['message']
                            if 'message' in response['error']
                            else ''))
                    continue
                if 'textAnnotations' in response:
                    text_response[filename] = response['textAnnotations']
                else:
                    text_response[filename] = []
            return text_response
        except errors.HttpError as e:
            print("Http Error for %s: %s" % (filename, e))
        except KeyError as e2:
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
def extract_description(texts):
    """Returns all the text in text annotations as a single string"""
    document = ''
    for text in texts:
        try:
            document += text['description']
        except KeyError as e:
            print('KeyError: %s\n%s' % (e, text))
    return document


def extract_descriptions(input_filename, index, texts):
    """Gets and indexes the text that was detected in the image."""
    if texts:
        document = extract_description(texts)
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
Then, for each batch of files in the directory that have not already been
processed, the `get_text_from_files()` function is passed these two objects and
the unprocessed files. (We're also using Redis to track the files already
processed). Any text is extracted and added to the index.

```python
def get_text_from_files(vision, index, input_filenames):
    """Call the Vision API on a file and index the results."""
    texts = vision.detect_text(input_filenames)
    for filename, text in texts.items():
        extract_descriptions(filename, index, text)


def batch(iterable, batch_size=BATCH_SIZE):
    """Group an iterable into batches of size batch_size.

    >>> tuple(batch([1, 2, 3, 4, 5], batch_size=2))
    ((1, 2), (3, 4), (5))
    """
    b = []
    for i in iterable:
        b.append(i)
        if len(b) == batch_size:
            yield tuple(b)
            b = []
    if b:
        yield tuple(b)


def main(input_dir):
    """Walk through all the not-yet-processed image files in the given
    directory, extracting any text from them and adding that text to an
    inverted index.
    """
    # Create a client object for the Vision API
    vision = VisionApi()
    # Create an Index object to build query the inverted index.
    index = Index()

    allfileslist = []
    # Recursively construct a list of all the files in the given input
    # directory.
    for folder, subs, files in os.walk(input_dir):
        for filename in files:
            allfileslist.append(os.path.join(folder, filename))

    fileslist = []
    for filename in allfileslist:
        # Look for text in any files that have not yet been processed.
        if index.document_is_processed(filename):
            continue
        fileslist.append(filename)

    for filenames in batch(fileslist):
        get_text_from_files(vision, index, filenames)
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

