#!/usr/bin/python

# Copyright 2016 Google, Inc
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""This script generates request JSON for the Vision API.
Run the script using the following command:

  python generatejson.py \
      -i <inputfile> \
      -o <outputfile.json>

where the input file contains information on how to process a set of URIs, and
the output is a JSON request file.

The Python script reads an input text file that contains one line for each
image to process. Each line in the input file contains the URI of an image--for
example, its file location--and a space-separated list of
Feature:maxResults to request for the image. The script associates each
Feature with an integer value from 1-6 (see the 'getDetectionType' function
definition).

For example, the following input file content requests face and label
detection annotations for image1, and landmark and logo detection annotations
for image2; each with a maximum of 10 results per annotation.

filepath_to_image1.jpg 1:10 4:10
filepath_to_image2.png 2:10 3:10

See https://cloud.google.com/vision/docs/concepts for more detail, and for
information on using the generated result to send a request to the Vision API.
"""
# [START generate_json]
import base64
import collections
import getopt
import json
import sys


def main(argv):
    inputfile = ''
    outputfile = ''
    try:
        opts, args = getopt.getopt(argv, "hi:o:", ["ifile=", "ofile="])
    except getopt.GetoptError:
        print 'generatejson.py -i <i>inputfile</i> -o <i>outputfile</i>'
        sys.exit(2)
    for opt, arg in opts:
        if opt == '-h':
            print 'generatejson.py -i <i>inputfile</i> -o <i>outputfile</i>'
            print 'Input File format:'
            print 'FileURI Feature:maxResults ...'
            sys.exit()
        elif opt in ("-i", "--ifile"):
            inputfilename = arg
        elif opt in ("-o", "--ofile"):
            outputfilename = arg

    inputFile = open(inputfilename, 'r')
    lineCount = 0
    requestsJsonObj = {}
    requestArray = []
    for inputLine in inputFile:
        lineCount = lineCount + 1
        print inputLine
        wordCount = 0
        contentJsonObj = {}
        imageJsonObj = {}
        featureJsonObj = []
        lineWords = inputLine.split(" ")
        if len(lineWords) < 2:
            print "Invalid input file format"
            print "Valid Format: FileURI feature:maxResults feature:maxResults ...."
            sys.exit()
        for word in lineWords:
            if wordCount == 0:
                imageFile = open(word, 'rb')
                contentJsonObj['content'] = base64.b64encode(imageFile.read())
            else:
                detectValues = word.split(":")
                valueCount = 0
                featureDict = collections.OrderedDict()
                for values in detectValues:
                    if valueCount == 0:
                        detectionType = getDetectionType(values)
                        featureDict['type'] = detectionType
                        print("detect:" + detectionType)
                    else:
                        maxResults = values.rstrip()
                        featureDict['maxResults'] = maxResults
                        print("results: " + maxResults)
                    valueCount = valueCount + 1
                featureJsonObj.append(featureDict)
            wordCount = wordCount + 1
        imageJsonObj['features'] = featureJsonObj
        imageJsonObj['image'] = contentJsonObj
        requestArray.append(imageJsonObj)
    requestsJsonObj['requests'] = requestArray
    j = json.dumps(requestsJsonObj)
    outputfile = open(outputfilename, 'w')
    print >> outputfile, j


def getDetectionType(detectNum):
    if detectNum == "1":
        return "FACE_DETECTION"
    elif detectNum == "2":
        return "LANDMARK_DETECTION"
    elif detectNum == "3":
        return "LOGO_DETECTION"
    elif detectNum == "4":
        return "LABEL_DETECTION"
    elif detectNum == "5":
        return "TEXT_DETECTION"
    elif detectNum == "6":
        return "SAFE_SEARCH_DETECTION"
    return "TYPE_UNSPECIFIED"


if __name__ == "__main__":
    main(sys.argv[1:])
# [END generate_json]
