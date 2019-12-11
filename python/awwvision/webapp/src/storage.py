# Copyright 2015 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import os

from redis import StrictRedis


class Storage(object):
    def __init__(self, host=None, port=None, *args, **kwargs):
        if host is None:
            host = os.environ.get('REDIS_HOST', 'localhost')
        if port is None:
            port = os.environ.get('REDIS_PORT', '6379')

        self.redis = StrictRedis(host, port, *args, **kwargs)

    def get_labels(self):
        return [x.decode('utf-8') for x in self.redis.smembers('labels')]

    def get_images(self, label):
        return self.redis.smembers(label)

    def get_repr_image_for_labels(self, labels):
        if not labels:
            return {}
        keys = [u'repr_img:{}'.format(label) for label in labels]
        labels_and_images = dict(zip(labels, self.redis.mget(*keys)))
        return {label: labels_and_images.get(label).decode('utf-8') for label in
                labels_and_images.keys()}
