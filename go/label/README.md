# Google Cloud Vision API Go example

## Get Dependencies

This sample depends on the following packages. Before getting started, be sure to get them.

```bash
go get -u golang.org/x/net/context golang.org/x/oauth2/google google.golang.org/api/vision/...
```

To run tests, getting the following package.

```bash
go get -u github.com/stretchr/testify/assert
```

## Setup

* Create a project with the [Google Cloud Console][cloud-console], and enable
  the [Vision API][vision-api].
* From the Cloud Console, create a service account,
  download its json credentials file, then set the 
  `GOOGLE_APPLICATION_CREDENTIALS` environment variable:

  ```bash
  export GOOGLE_APPLICATION_CREDENTIALS=/path/to/your-project-credentials.json
  ```

[cloud-console]: https://console.cloud.google.com
[vision-api]: https://console.cloud.google.com/apis/api/vision.googleapis.com/overview?project=_
[adc]: https://cloud.google.com/docs/authentication#developer_workflow

## Run the sample

To build and run the sample:

```bash
go run label.go ../../data/label/cat.jpg
```
