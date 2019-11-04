# For more information about this base image and dockerfile, see
# https://github.com/GoogleCloudPlatform/python-docker
FROM gcr.io/google_appengine/python

RUN virtualenv -p python3 /env

ENV VIRTUAL_ENV /env
ENV PATH /env/bin:$PATH

ADD src/requirements.txt /app/requirements.txt
RUN pip install -r /app/requirements.txt

ADD src /app

CMD gunicorn -w 4 -b :$PORT main:app
