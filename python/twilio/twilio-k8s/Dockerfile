# Copyright 2016 Google Inc. All Rights Reserved.
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

# For more information about this base image and dockerfile, see
# https://github.com/GoogleCloudPlatform/python-docker
FROM gcr.io/google_appengine/python

RUN virtualenv /env

ENV VIRTUAL_ENV /env
ENV PATH /env/bin:$PATH

ADD src/requirements.txt /app/requirements.txt
RUN pip install -r /app/requirements.txt

ADD src /app

CMD gunicorn -w 4 -b :$PORT whats_that:app
