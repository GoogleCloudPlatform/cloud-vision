This sample takes in the uri for an object in Google Cloud Storage, and
identifies the landmark pictured in it.

## Setup
* Create a project with the [Google Cloud Console][cloud-console].
* Set up your environment with [Application Default Credentials][adc]. For
    example, from the Cloud Console, you might create a service account,
    download its json credentials file, then set the appropriate environment
    variable:

    ```bash
    export GOOGLE_APPLICATION_CREDENTIALS=/path/to/your-project-credentials.json
    ```

* Upload an image to [Google Cloud Storage][gcs] (for example, using
    [gsutil][gsutil] or the [GUI][gcs-browser]), and make sure the image is
    accessible to the default credentials you set up. Note that by default,
    Cloud Storage buckets and their associated objects are readable by service
    accounts within the same project. For example, if you're using gsutil, you
    might do:

    ```bash
    gsutil mb gs://<your-project-bucket>
    gsutil cp landmark.jpg gs://<your-project-bucket>/landmark.jpg
    # This step is unnecessary with default permissions, but for completeness,
    # explicitly give the service account access to the image. This email can
    # be found by running:
    # `grep client_email /path/to/your-project-credentials.json`
    gsutil acl ch -u <service-account@your-project.iam.gserviceaccount.com>:R \
        gs://<your-project-bucket>/landmark.jpg
    ```

[cloud-console]: https://console.cloud.google.com
[adc]: https://cloud.google.com/docs/authentication#developer_workflow
[gcs]: https://cloud.google.com/storage/docs/overview
[gsutil]: https://cloud.google.com/storage/docs/gsutil
[gcs-browser]: https://console.cloud.google.com/storage/browser?project=_

## Run the sample

If you've successfully uploaded your object to your Cloud Storage bucket, and
set the appropriate access controls, you should now be able to run the sample as
follows:

    ./detect_landmark.py gs://<your-project-bucket>/landmark.jpg

For your convenience, you're also welcome to test the landmark detection script
on the [image][landmark.jpg] we use for testing, which we've made
publicly-accessible:

    ./detect_landmark.py gs://cloud-samples-tests/vision/water.jpg

[landmark.jpg]: https://cloud-samples-tests.storage.googleapis.com/vision/water.jpg
