
# Using the Cloud Vision API with Twilio Messaging on Kubernetes

  - [Prerequisites](#prerequisites)
  - [Step 1: Start up a Google Container Engine Cluster](#step-1-start-up-a-google-container-engine-cluster)
  - [Step 2: Deploy your app to the cluster](#step-2-deploy-your-app-to-the-cluster)
  - [Step 3: Create a Twilio "TwilML app" using your webapp's service IP](#step-3-create-a-twilio-twilml-app-using-your-webapps-service-ip)
  - [Step 4: Test your app!](#step-4-test-your-app)
  - [Step 5 (optional): Roll out changes to your app via the Deployment resource](#step-5-optional-roll-out-changes-to-your-app-via-the-deployment-resource)
  - [Step 6 (optional): Build your own docker image](#step-6-optional-build-your-own-docker-image)
  - [Step 8: Cleanup](#step-8-cleanup)

This example shows how to run the ["What's That?" app](../twilio-labels), [built by Julia
Ferraioli](http://www.blog.juliaferraioli.com/2016/02/exploring-world-using-vision-twilio.html), on
[Kubernetes](http://kubernetes.io/)
(more specifically, on  [Google Container Engine](https://cloud.google.com/container-engine/)).
The app uses [Twilio](https://www.twilio.com) to allow people to text an image to a given number,
then uses the [Cloud Vision API](https://cloud.google.com/vision/) to find labels in the image and
return the detected labels as a reply text.
Because the app is running on Kubernetes, it's easy to **scale up the app** to support a large number
of requests. We'll show how to do that as part of this tutorial.

The example demos the use of
[Kubernetes Deployments](http://kubernetes.io/docs/user-guide/deployments/),
a new feature in Kubernetes 1.2, to manage app updates and rollouts, and so it
requires Kubernetes 1.2 or greater.


## Prerequisites

1. Create a project in the [Google Cloud Platform Console](https://console.cloud.google.com).

2. [Enable billing](https://console.cloud.google.com/project/_/settings) for your project.

3. Enable the Vision API. See the
   ["Getting Started"](https://cloud.google.com/vision/docs/getting-started) page
   in the Vision API documentation for more information on using the Vision API.

4. Install the [Google Cloud SDK](https://cloud.google.com/sdk):

        $ curl https://sdk.cloud.google.com | bash
        $ gcloud init

5. Create and set up a [Twilio account](https://www.twilio.com/try-twilio) and number capable of
   sending and receiving MMS. (A bit later in this tutorial, you will set up a "TwilML" app in that
   account, that points to your new Kubernetes service).

6. Optional: Install and start up [Docker](https://www.docker.com/).
   This is only necessary if you want to build your own container images.
   You can run the example without doing this.


## Step 1: Start up a Google Container Engine Cluster

For simplicity, this example uses [Google Container Engine](https://cloud.google.com/container-engine/) to set up
a Kubernetes cluster.  (If you prefer, you could instead use [Kubernetes](https://github.com/kubernetes/kubernetes/releases) directly).

1. Create a cluster using `gcloud`. You can specify as many nodes as you want,
   but you need at least one. The `cloud-platform` scope is needed to allow
   access to the Pub/Sub and Vision APIs.

        gcloud container clusters create twilio-vision \
            --num-nodes 2 \
            --scopes cloud-platform

2. Set up the `kubectl` command-line tool to use the container's credentials.

        gcloud container clusters get-credentials twilio-vision

3. Verify that everything is working:

        kubectl cluster-info


## Step 2: Deploy your app to the cluster

Run the following two commands once your cluster is up and running.
The first command creates a [Service](http://kubernetes.io/docs/user-guide/services/) for your app,
fronted by an external IP. The second command creates a
[Deployment](http://kubernetes.io/docs/user-guide/deployments/),
which spins up the specified number of
[Pods](http://kubernetes.io/docs/user-guide/pods/)— in this case, 2.

```sh
$ kubectl create -f webapp-service.yaml
$ kubectl apply -f webapp-dep.yaml
```

The `webapp-dep.yaml` file looks like the following.  Note that 2 replicas are specified. Note also
the environment variable, `MESSAGE_BLURB`, containing a (rather long) string.  We'll come back to
that in Step 5.

```yaml
apiVersion: extensions/v1beta1
kind: Deployment
metadata:
  name: twilio-vision-webapp
spec:
  replicas: 2
  template:
    metadata:
      labels:
        app: twilio-vision
        role: frontend
    spec:
      containers:
      - name: twilio-vision-webapp
        image: gcr.io/google-samples/twilio-vision:v1
        imagePullPolicy: Always
        env:
        - name: PORT
          value: "5000"
        - name: MESSAGE_BLURB
          value: "Your message has been passed to the Google Cloud Vision API for processing.\nImages are not stored in Google Cloud Platform, and will periodically need to be deleted from Twilio."
        ports:
        - name: http-server
          containerPort: 5000
```

Check that the pods and service are running before you proceed.  You can list your pods as follows.
You should see two listed, since that is the number of replicas specified in `webapp-dep.yaml`.

```sh
$ kubectl get pods -o wide
NAME                                   READY     STATUS    RESTARTS   AGE       NODE
twilio-vision-webapp-888475278-r7c2k   1/1       Running   0          9m        gke-twilio-vision-c7609f98-node-47a6
twilio-vision-webapp-888475278-rqxue   1/1       Running   0          9m        gke-twilio-vision-c7609f98-node-ygtw
```

Then, list your services like this:

```sh
$ kubectl get services
NAME                   CLUSTER-IP     EXTERNAL-IP       PORT(S)    AGE
kubernetes             10.0.0.1       <none>            443/TCP    1d
twilio-vision-webapp   10.0.213.241   104.197.142.245   5000/TCP   1d
```

You should see an external IP listed for your new `twilio-vision-webapp` service.  If you don't see
it right away, wait a minute or two.  Note this IP -- you will need it to set up your Twilio "TwiML"
app.

## Step 3: Create a Twilio "TwilML app" using your webapp's service IP

Visit [this page](https://www.twilio.com/user/account/voice/dev-tools/twiml-apps) on the Twilio
site, and create a "TwilML" app that points to your external service IP from Step 2, port 5000.
See this
[blog post](http://www.blog.juliaferraioli.com/2016/02/exploring-world-using-vision-twilio.html)
for details.


## Step 4: Test your app!

Text an image to the Twilio number you created in the Prerequisites setup. After a few seconds, you
should receive a reply that looks something along the lines of the following, with information about
what labels the Vision API found in the image.

<a href="https://amy-jo.storage.googleapis.com/images/tiger_selfie.jpg" target="_blank"><img src="https://amy-jo.storage.googleapis.com/images/cat_and_laptop.jpg" width=300/></a>


## Step 5 (optional): Roll out changes to your app via the Deployment resource

We've set up the app to use a
[Deployment](http://kubernetes.io/docs/user-guide/deployments/).

That means that now, we can make changes to the deployment config, and have them gracefully
applied using a rolling update.

Suppose we've decided that the `MESSAGE_BLURB` string in `webapp-dep.yaml` is too long.  Plus, we've now absorbed the info about the need for managing our Twilio deletions, and so we don't need to share that with our users every time.  So, change the value of `MESSAGE_BLURB` to the following:

```
    value: "Your message has been passed to the Google Cloud Vision API for processing.\nImages are not stored in GCP."
```

Next, increase the number of replicated pods to 4; we expect to be getting popular.

Your `webapp-dep.yaml` file should now look like this:

```yaml
apiVersion: extensions/v1beta1
kind: Deployment
metadata:
  name: twilio-vision-webapp
spec:
  replicas: 4
  template:
    metadata:
      labels:
        app: twilio-vision
        role: frontend
    spec:
      containers:
      - name: twilio-vision-webapp
        image: gcr.io/google-samples/twilio-vision:v1
        imagePullPolicy: Always
        env:
        - name: PORT
          value: "5000"
        - name: MESSAGE_BLURB
          value: "Your message has been passed to the Google Cloud Vision API for processing.\nImages are not stored in GCP."
        ports:
        - name: http-server
          containerPort: 5000
```

**Apply** the changes via:

```sh
$ kubectl apply -f webapp-dep.yaml
```
When you list the pods again, you should now see four.

If you like, you can see a record of the changes by *describing* the deployment:

```sh
$ kubectl describe deployment/twilio-vision-webapp
Name:			twilio-vision-webapp
Namespace:		default
CreationTimestamp:	Sat, 09 Apr 2016 13:13:34 -0700
Labels:			app=twilio-vision,role=frontend
Selector:		app=twilio-vision,role=frontend
Replicas:		4 updated | 4 total | 4 available | 0 unavailable
StrategyType:		RollingUpdate
MinReadySeconds:	0
RollingUpdateStrategy:	1 max unavailable, 1 max surge
OldReplicaSets:		twilio-vision-webapp-888475278 (0/0 replicas created)
NewReplicaSet:		twilio-vision-webapp-590672895 (4/4 replicas created)
Events:
  FirstSeen	LastSeen	Count	From				SubobjectPath	Type		Reason			Message
  ---------	--------	-----	----				-------------	--------	------			-------
  3m		3m		1	{deployment-controller }			Normal		ScalingReplicaSet	Scaled up replica set twilio-vision-webapp-888475278 to 2
  8s		8s		1	{deployment-controller }			Normal		ScalingReplicaSet	Scaled up replica set twilio-vision-webapp-590672895 to 3
  6s		6s		1	{deployment-controller }			Normal		ScalingReplicaSet	Scaled down replica set twilio-vision-webapp-888475278 to 0
  6s		6s		1	{deployment-controller }			Normal		ScalingReplicaSet	Scaled up replica set twilio-vision-webapp-590672895 to 4
```

## Step 6 (optional): Build your own docker image

The `webapp-dep.yaml ` file points to a prebuilt docker image.  If you would like to generate your
own instead, here is the process.

First, double check that Docker is running on your local machine, as in Step 6 of the Prerequisites.

Copy the the ["What's That?" app script](../twilio-labels/whats_that.py) into the `src` subdirectory
of this directory:

```sh
$ cp ../twilio-labels/whats_that.py src
```

Then, build your image and push it to your project's private [Google Container
Registry](https://cloud.google.com/container-registry/), again substituting the name of your
project for the `YOUR-PROJECT-ID` name in the following):

```sh
$ docker build -t gcr.io/YOUR-PROJECT-ID/twilio-vision:v1 .
$ gcloud docker push gcr.io/YOUR-PROJECT-ID/twilio-vision:v1
```

(You can alternately use some other registry, like Docker Hub).

Next, edit the `webapp-dep.yaml` file, and change the `image:` spec to point to your image
information instead.  Change the project name from `google-samples` to your project, and change the
image name and tag to yours.

Then, reapply the deployment again to pick up those changes:

```sh
$ kubectl apply -f webapp-dep.yaml
```

## Step 8: Cleanup

When you're ready to clean up, delete your Deployment and Service:

```sh
$ kubectl delete deployment,service -l app=twilio-vision
```

Note: this won't delete your Container Engine cluster itself.
If you are no longer using the cluster, you may want to take it down.
You can do this through the
[Google Cloud Platform Console](https://console.cloud.google.com).

You can delete images from Twilio
[using the Twilio API](https://www.twilio.com/help/faq/sms/how-do-i-delete-messages-message-media-or-message-bodies).
You can write a script to do this.
If you no longer want to keep your Twilio number, you can delete that as well.
