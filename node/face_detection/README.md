# Google Cloud Vision API Node  example

## Get Dependencies

This sample depends on the following packages. Before getting started, be sure to get them.

```bash
npm install
```

## Setup

* Create a project with the [Google Cloud Console][cloud-console], and enable
  the [Vision API][vision-api].

* Use Server Key
  ```JavaScript
  vision.init({auth: 'YOUR_API_KEY'})
  ```

* Use OAuth
  ```JavaScript
  const google = require('googleapis')
  const oauth2Client = new google.auth.OAuth2('YOUR_GOOGLE_OAUTH_CLIENT_ID', 'YOUR_GOOGLE_OAUTH_SECRET', 'YOUR_GOOGLE_OAUTH_CALLBACK_URL')
  oauth2Client.setCredentials({refresh_token: 'YOUR_GOOGLE_OAUTH_REFRESH_TOKEN'})
  vision.init({auth: oauth2Client})
  ```

[cloud-console]: https://console.cloud.google.com
[vision-api]: https://console.cloud.google.com/apis/api/vision.googleapis.com/overview?project=_
[adc]: https://cloud.google.com/docs/authentication#developer_workflow

## Run the sample

To build and run the sample:

```bash
node test
```
