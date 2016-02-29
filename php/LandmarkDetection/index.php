<!--
	Copyright 2015, Google, Inc.
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
-->
<?php
include_once 'creds.php'; // Get $bucket
use google\appengine\api\cloud_storage\CloudStorageTools;

$options = ['gs_bucket_name' => $bucket];
$upload_url = CloudStorageTools::createUploadUrl('/process.php', $options);

?>

<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<title>Cloud Vision API PHP Example</title>
</head>
<body>
	<form action="<?php echo $upload_url ?>" method="post" enctype="multipart/form-data">
	Your Photo: <input type="file" name="photo" size="25" />
	<input type="submit" name="submit" value="Submit" />
</form>
</body>
</html>