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

from gcloud import pubsub
import requests
import psq

import reddit
from storage import Storage
from vision import VisionApi


def download_image(image_url):
    r = requests.get(image_url)
    r.raise_for_status()
    return r.content


def label_images(vision, storage, image_urls):
    image_contents = [
        download_image(image_url)
        for image_url
        in image_urls]

    # TODO content check

    response = vision.detect_labels(image_contents)

    for image_url, labels in zip(image_urls, response):
        storage.add_labels(labels)
        storage.add_image(image_url, labels)


def label_images_task(image_urls):
    vision = VisionApi()
    storage = Storage()

    label_images(vision, storage, image_urls)


def scrape_reddit(subreddit, pages=10):
    after = None

    for _ in range(pages):
        posts, after = reddit.get_hot('aww', after=after)
        yield reddit.get_previews(posts)


def scrape_reddit_task(subreddit, pages=20):
    for image_urls in scrape_reddit(subreddit, pages):
        q = psq.Queue(pubsub.Client(), 'images')
        q.enqueue('main.label_images_task', image_urls)
        print("Enqueued {} images".format(len(image_urls)))


q = psq.Queue(pubsub.Client(), 'images')
