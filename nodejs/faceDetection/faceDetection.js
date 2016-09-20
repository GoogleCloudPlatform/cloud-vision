// Copyright 2016, Google, Inc.
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

'use strict';

// You must set the GOOGLE_APPLICATION_CREDENTIALS and GCLOUD_PROJECT
// environment variables to run this sample. See:
// https://github.com/GoogleCloudPlatform/gcloud-node/blob/master/docs/authentication.md

var gcloud = require('gcloud')(),
    vision = gcloud.vision();
var fs = require('fs'),
    Canvas = require('canvas'),
    Image = Canvas.Image;

/**
 * Uses the Vision API to detect faces in the given file.
 */
function detectFaces(inputFile, callback) {
  // Make a call to the Vision API to detect the faces
  vision.detectFaces(inputFile, function(error, faces) {
    if (error) throw error;
    callback(faces);
  });
}

/**
 * Draws a polygon around the faces, then saves to outputFile.
 */
function highlightFaces(inputFile, faces, outputFile) {
  fs.readFile(inputFile, function(error, image) {
    if (error) throw error;

    // Open the original image into a canvas
    var img = new Image;
    img.src = image;
    var canvas = new Canvas(img.width, img.height),
        context = canvas.getContext('2d');
    context.drawImage(img, 0, 0, img.width, img.height);

    // Now draw boxes around all the faces
    context.strokeStyle = 'rgba(0,255,0,0.8)';
    context.lineWidth = '5';

    faces.forEach(function(face) {
      context.beginPath();
      face.bounds.face.forEach(function(bounds) {
        context.lineTo(bounds.x, bounds.y);
      });
      context.lineTo(face.bounds.face[0].x, face.bounds.face[0].y);
      context.stroke();
    });

    // Write the result to a file
    var out = fs.createWriteStream(outputFile);
    canvas.jpegStream().on('data', out.write.bind(out));
  });
}

function main(inputFile, outputFile) {
  outputFile = outputFile || 'out.jpg';
  detectFaces(inputFile, function(faces) {
    console.log('Found ' + faces.length + ' face' +
        (faces.length == 1 ? '' : 's'));

    console.log('Writing to file ' + outputFile);
    highlightFaces(inputFile, faces, outputFile);
  });
}


exports.detectFaces = detectFaces;

if (module === require.main) {
  if (process.argv.length < 3) {
    console.log('Usage: ' + process.argv[0] + ' ' + process.argv[1] +
        ' <image.jpg> [outputFile]');
    process.exit(1);
  }

  main(process.argv[2], process.argv[3]);
}
