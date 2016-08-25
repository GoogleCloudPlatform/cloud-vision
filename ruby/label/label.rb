#!/usr/bin/env ruby
# Copyright 2015 Google Inc. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# This script uses the Vision API's label detection capabilities to find a label
# based on an image's content.
#
# To run the example, install the necessary libraries by running:
#
#     bundle install
#
# Run the script on an image to get a label, E.g.:
#
#     ./label.rb -i <path-to-image>

require 'optparse'
require "gcloud"

# Run a label request on a single image
def main(image_path)
    gcloud = Gcloud.new
    vision = gcloud.vision

    image = vision.image image_path
    labels = image.labels
    label = labels.first
    puts "Found label #{label.description} for #{image_path}"
end

options = {}

opt_parser = OptionParser.new do |opt|
    opt.banner = "Usage: ./label.rb [OPTIONS]"
    opt.separator  ""

    opt.on("-i","--image IMAGE","The image you'd like to label") do |image|
        options[:image] = image
    end

    opt.on("-h","--help","help") do
        puts opt_parser
    end
end

opt_parser.parse!
if !options[:image].nil?
    main(options[:image])
end
