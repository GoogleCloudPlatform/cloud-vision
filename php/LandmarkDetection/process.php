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

include_once 'creds.php'; // Get $api_key
$cvurl = 'https://vision.googleapis.com/v1/images:annotate?key=' . $api_key;
$type = 'LANDMARK_DETECTION';

//Did they upload a file...
if ($_FILES['photo']['name']) {
    //if no errors...
    if (!$_FILES['photo']['error']) {
        $valid_file = true;
        if ($_FILES['photo']['size'] > (4024000)) {
            //can't be larger than ~4 MB

            $valid_file = false;
            die('Your file\'s size is too large.');
        }

        //if the file has passed the test
        if ($valid_file) {
            //convert it to base64
            $data = file_get_contents($_FILES['photo']['tmp_name']);
            $base64 = base64_encode($data);
            //Create this JSON
            $request_json = '{
			  	"requests": [
					{
					  "image": {
					    "content":"' . $base64 . '"
					  },
					  "features": [
					      {
					      	"type": "' . $type . '",
							"maxResults": 200
					      }
					  ]
					}
				]
			}';

            $curl = curl_init();
            curl_setopt($curl, CURLOPT_URL, $cvurl);
            curl_setopt($curl, CURLOPT_RETURNTRANSFER, true);
            curl_setopt($curl, CURLOPT_HTTPHEADER, array('Content-type: application/json'));
            curl_setopt($curl, CURLOPT_POST, true);
            curl_setopt($curl, CURLOPT_POSTFIELDS, $request_json);
            $json_response = curl_exec($curl);
            $status = curl_getinfo($curl, CURLINFO_HTTP_CODE);
            curl_close($curl);

            if ($status != 200) {
                die("Error: call to URL $cvurl failed with status $status, response $json_response, curl_error " . curl_error($curl) . ', curl_errno ' . curl_errno($curl));
            }

            echo '<pre>';
            echo $json_response;
            echo '</pre>';
        }
    }
    //if there is an error...
    else {
        //set that to be the returned message
        echo 'Error';
        die('Ooops!  Your upload triggered the following error:  ' . $_FILES['photo']['error']);
    }
}
?>
