## Cloud Vision API samples

These samples require two environment variables to be set:

- `GOOGLE_APPLICATION_CREDENTIALS` - Path to a service account file. You can
download one from your Google project's "permissions" page.
- `GCLOUD_PROJECT` - Id of your Google project.

See [gcloud-node:authentication.md][auth.md] for more details.

[auth.md]: https://github.com/GoogleCloudPlatform/gcloud-node/blob/master/docs/authentication.md

## Run a sample

Install npm dependencies:

    npm install

This sample uses [node-canvas](https://github.com/Automattic/node-canvas) to
draw an output image. node-canvas depends on Cairo, which may require separate
installation. See the node-canvas [installation section][canvas-install] for
details.

[canvas-install]: https://github.com/Automattic/node-canvas#installation

Execute the sample:

    node faceDetection input-file.jpg
