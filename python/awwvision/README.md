# Awwvision

*Awwvision* is a [Kubernetes](https://github.com/kubernetes/kubernetes/) and [Cloud Vision API](https://cloud.google.com/vision/) sample that uses the Vision API to classify (label) images from Reddit's [/r/aww](https://reddit.com/r/aww) subreddit, and display the labelled results in a web app.

Awwvision has three components:

1. A simple [Redis](http://redis.io/) instance.
2. A webapp that displays the labels and associated images.
3. A worker that handles scraping Reddit for images and classifying them using the Vision API. [Cloud Pub/Sub](https://cloud.google.com/pubsub/) is used to coordinate tasks between multiple worker instances.

## Prerequisites

1. Create a project in the [Google Cloud Platform Console](https://console.cloud.google.com).

2. [Enable billing](https://console.cloud.google.com/project/_/settings) for your project.

3. Enable the Vision and Pub/Sub APIs. See the ["Getting Started"](https://cloud.google.com/vision/docs/getting-started) page in the Vision API documentation for more information on using the Vision API.

4. Install the [Google Cloud SDK](https://cloud.google.com/sdk):

        $ curl https://sdk.cloud.google.com | bash
        $ gcloud init

5. Install and start up [Docker](https://www.docker.com/).

If you like, you can alternately run this tutorial from your project's
[Cloud Shell](https://cloud.google.com/shell/docs/).  In that case, you don't need to do steps 4 and 5.

## Create a Container Engine cluster

This example uses [Container Engine](https://cloud.google.com/container-engine/) to set up the Kubernetes cluster.

1. Create a cluster using `gcloud`. You can specify as many nodes as you want,
   but you need at least one. The `cloud-platform` scope is used to allow
   access to the Pub/Sub and Vision APIs.
   First set your zone, e.g.:

        gcloud config set compute/zone us-central1-f

   Then start up the cluster:

        gcloud container clusters create awwvision \
            --num-nodes 2 \
            --scopes cloud-platform

2. Set up the `kubectl` command-line tool to use the container's credentials.

        gcloud container clusters get-credentials awwvision

3. Verify that everything is working:

        kubectl cluster-info

## Deploy the sample

From the `awwvision` directory, use `make all` to build and deploy everything.
Make sure Docker is running first.

        make all

As part of the process, a Docker image will be built and uploaded to the
[GCR](https://cloud.google.com/container-registry/docs/) private container
registry. In addition, `.yaml` files will be generated from templates— filled in
with information specific to your project— and used to deploy the 'redis',
'webapp', and 'worker' Kubernetes resources for the example.

### Check the Kubernetes resources on the cluster

After you've deployed, check that the Kubernetes resources are up and running.
First, list the [pods](https://kubernetes.io/docs/concepts/workloads/pods/pod/).
You should see something like the following, though your pod names will be different.

```
$ kubectl get pods
NAME                     READY     STATUS    RESTARTS   AGE
awwvision-webapp-vwmr1   1/1       Running   0          1m
awwvision-worker-oz6xn   1/1       Running   0          1m
awwvision-worker-qc0b0   1/1       Running   0          1m
awwvision-worker-xpe53   1/1       Running   0          1m
redis-master-rpap8       1/1       Running   0          2m
```

List the
[deployments](https://kubernetes.io/docs/concepts/workloads/controllers/deployment/).
You can see the number of replicas specified for each, and the images used.

```
$ kubectl get deployments -o wide
NAME               DESIRED   CURRENT   UP-TO-DATE   AVAILABLE   AGE       CONTAINERS         IMAGES                                SELECTOR
awwvision-webapp   1         1         1            1           1m        awwvision-webapp   gcr.io/your-project/awwvision-webapp   app=awwvision,role=frontend
awwvision-worker   3         3         3            3           1m        awwvision-worker   gcr.io/your-project/awwvision-worker   app=awwvision,role=worker
redis-master       1         1         1            1           1m        redis-master       redis                                 app=redis,role=master
```

Once deployed, get the external IP address of the webapp
[service](https://kubernetes.io/docs/concepts/services-networking/service/).
It may take a few minutes for the assigned external IP to be
listed in the output.  After a short wait, you should see something like the
following, though your IPs will be different.

```
$ kubectl get svc awwvision-webapp
NAME               CLUSTER_IP      EXTERNAL_IP    PORT(S)   SELECTOR                      AGE
awwvision-webapp   10.163.250.49   23.236.61.91   80/TCP    app=awwvision,role=frontend   13m
```

### Visit your new webapp and start its crawler

Visit the external IP of the `awwvision-webapp` service to open the webapp in
your browser, and click the `Start the Crawler` button.

Next, click `go back`, and you should start to see images from the
[/r/aww](https://reddit.com/r/aww) subreddit classified by the labels provided
by the Vision API. You will see some of the images classified multiple times, where multiple
labels are detected for them.
(You can reload in a bit, in case you brought up the page before the crawler was
finished).

<a href="https://storage.googleapis.com/amy-jo/images/ubiquity/awwvision.png" target="_blank"><img src="https://storage.googleapis.com/amy-jo/images/ubiquity/awwvision.png" width=500/></a>

## Cleanup

To delete your Kubernetes pods, replication controllers, and services, and to
remove your auto-generated `.yaml` files, do:

        make delete

Note: this won't delete your Container Engine cluster itself.
If you are no longer using the cluster, you may want to take it down.
You can do this through the
[Google Cloud Platform Console](https://console.cloud.google.com).
