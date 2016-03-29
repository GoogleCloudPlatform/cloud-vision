# Copyright 2016 Google, Inc.
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

import json
import os
import shutil
import tempfile
import unittest
from io import StringIO

from generatejson import main


class TestGenerateJson(unittest.TestCase):
    def setUp(self):
        self.temp_dir = tempfile.mkdtemp()
        self.image_name = os.path.join(
            os.path.dirname(__file__), u'generatejson_test_fake.jpg')

    def tearDown(self):
        shutil.rmtree(self.temp_dir)

    def test_no_features(self):
        input_file = StringIO(self.image_name)
        output_filename = os.path.join(self.temp_dir, 'empty_file.json')
        with self.assertRaises(ValueError):
            main(input_file, output_filename)
        self.assertFalse(os.path.exists(output_filename))

    def test_no_image(self):
        input_file = StringIO(u'doesnotexist.jpg 1:1')
        output_filename = os.path.join(self.temp_dir, 'empty_file.json')
        with self.assertRaises(IOError):
            main(input_file, output_filename)
        self.assertFalse(os.path.exists(output_filename))

    def test_no_max(self):
        input_file = StringIO(self.image_name + ' 1')
        output_filename = os.path.join(self.temp_dir, 'empty_file.json')
        with self.assertRaises(ValueError):
            main(input_file, output_filename)
        self.assertFalse(os.path.exists(output_filename))

    def test_one_detection(self):
        input_file = StringIO(self.image_name + ' 1:1')
        output_filename = os.path.join(self.temp_dir, 'empty_file.json')
        main(input_file, output_filename)
        self.assertTrue(os.path.exists(output_filename))
        with open(output_filename, 'r') as output_file:
            obj = json.load(output_file)
        self.assertEqual(1, len(obj['requests']))
        self.assertIn('image', obj['requests'][0])
        self.assertIn('content', obj['requests'][0]['image'])
        self.assertIn('features', obj['requests'][0])
        self.assertEqual(1, len(obj['requests'][0]['features']))
        self.assertEqual(
            'FACE_DETECTION', obj['requests'][0]['features'][0]['type'])
        self.assertEqual(
            1, obj['requests'][0]['features'][0]['maxResults'])

    def test_multiple_detections(self):
        input_file = StringIO(self.image_name + ' 1:1 2:3')
        output_filename = os.path.join(self.temp_dir, 'empty_file.json')
        main(input_file, output_filename)
        self.assertTrue(os.path.exists(output_filename))
        with open(output_filename, 'r') as output_file:
            obj = json.load(output_file)
        self.assertEqual(1, len(obj['requests']))
        self.assertIn('image', obj['requests'][0])
        self.assertIn('content', obj['requests'][0]['image'])
        self.assertIn('features', obj['requests'][0])
        self.assertEqual(2, len(obj['requests'][0]['features']))
        self.assertEqual(
            'FACE_DETECTION', obj['requests'][0]['features'][0]['type'])
        self.assertEqual(
            1, obj['requests'][0]['features'][0]['maxResults'])
        self.assertEqual(
            'LANDMARK_DETECTION', obj['requests'][0]['features'][1]['type'])
        self.assertEqual(
            3, obj['requests'][0]['features'][1]['maxResults'])

    def test_multiple_lines(self):
        input_file = StringIO(
            '%s 1:2\n%s 2:1' % ((self.image_name,) * 2))
        output_filename = os.path.join(self.temp_dir, 'empty_file.json')
        main(input_file, output_filename)
        self.assertTrue(os.path.exists(output_filename))
        with open(output_filename, 'r') as output_file:
            obj = json.load(output_file)
        self.assertEqual(2, len(obj['requests']))
        for i in range(2):
            self.assertIn('image', obj['requests'][i])
            self.assertIn('features', obj['requests'][i])
            self.assertEqual(1, len(obj['requests'][i]['features']))
            self.assertIn('content', obj['requests'][i]['image'])

    def test_bad_detection_type(self):
        input_file = StringIO(self.image_name + ' 125:1')
        output_filename = os.path.join(self.temp_dir, 'empty_file.json')
        main(input_file, output_filename)
        self.assertTrue(os.path.exists(output_filename))
        with open(output_filename, 'r') as output_file:
            obj = json.load(output_file)
        self.assertEqual(1, len(obj['requests']))
        self.assertIn('image', obj['requests'][0])
        self.assertIn('content', obj['requests'][0]['image'])
        self.assertIn('features', obj['requests'][0])
        self.assertEqual(1, len(obj['requests'][0]['features']))
        self.assertEqual(
            'TYPE_UNSPECIFIED', obj['requests'][0]['features'][0]['type'])
        self.assertEqual(
            1, obj['requests'][0]['features'][0]['maxResults'])



if __name__ == '__main__':
    unittest.main()
