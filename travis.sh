#!/usr/bin/env bash
# Copyright 2016 Google Inc. All Rights Reserved.
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

# Note: travis currently does not support listing more than one language so
# this cheats and claims to only be cpp.  If they add multiple language
# support, this should probably get updated to install steps and/or
# rvm/gemfile/jdk/etc. entries rather than manually doing the work.

# Modeled after:
# https://github.com/google/protobuf/blob/master/travis.sh

use_java() {
  version=$1
  case "$version" in
    jdk8)
      sudo apt-get update
      sudo apt-get -y install openjdk-8-jdk
      export PATH=/usr/lib/jvm/java-8-openjdk-amd64/bin:$PATH
      ;;
    oracle8)
      sudo apt-get install python-software-properties # for apt-add-repository
      echo "oracle-java8-installer shared/accepted-oracle-license-v1-1 select true" | \
          sudo debconf-set-selections
      yes | sudo apt-add-repository ppa:webupd8team/java
      sudo apt-get update
      sudo apt-get -y install oracle-java8-installer
      export PATH=/usr/lib/jvm/java-8-oracle/bin:$PATH
      ;;
  esac

  which java
  java -version
}

build_java() {
  for jdir in $(ls -d java/*/); do
    cd "${jdir}" && mvn clean compile assembly:single
    if [ -z "${GOOGLE_APPLICATION_CREDENTIALS}" ]; then
      echo "Secrets not available, skipping tests."
      mvn clean verify -DskipTests
    else
      mvn clean verify
    fi
    cd ../..
  done
}

build_java_jdk8() {
  use_java jdk8
  build_java
}

build_java_oracle8() {
  use_java oracle8
  build_java
}

internal_ios_common () {
  # Make sure xctool is up to date. Adapted from
  #  http://docs.travis-ci.com/user/osx-ci-environment/
  # We don't use a before_install because we test multiple OSes.
  brew update
  brew outdated xctool || brew upgrade xctool
  xctool --version
}

internal_xctool_debug_and_release() {
  # Always use -reporter plain to avoid escape codes in output (makes travis
  # logs easier to read).
  xctool -reporter plain -configuration Debug "$@"
  xctool -reporter plain -configuration Release "$@"
}

build_objectivec_ios() {
  internal_ios_common
  # https://github.com/facebook/xctool/issues/509 - unlike xcodebuild, xctool
  # doesn't support >1 destination, so we have to build first and then run the
  # tests one destination at a time.
  internal_xctool_debug_and_release \
    -project ios/Objective-C/imagepicker-objc.xcodeproj \
    -scheme imagepicker-objc \
    -sdk iphonesimulator \
    build
  # TODO: build-tests, when we have tests
  IOS_DESTINATIONS=(
    "platform=iOS Simulator,name=iPhone 4s,OS=8.1" # 32bit
    "platform=iOS Simulator,name=iPhone 6,OS=9.2" # 64bit
    "platform=iOS Simulator,name=iPad 2,OS=8.1" # 32bit
    "platform=iOS Simulator,name=iPad Air,OS=9.2" # 64bit
  )
  # TODO: run-tests, when we have tests
  # for i in "${IOS_DESTINATIONS[@]}" ; do
  #   internal_xctool_debug_and_release \
  #     -project objectivec/ProtocolBuffers_iOS.xcodeproj \
  #     -scheme ProtocolBuffers \
  #     -sdk iphonesimulator \
  #     -destination "${i}" \
  #     run-tests
  # done
}

build_swift_ios() {
  internal_ios_common
  # CocoaPods is needed to manage dependencies for the Swift project.
  gem install cocoapods
  pod install --project-directory=ios/Swift
  # https://github.com/facebook/xctool/issues/509 - unlike xcodebuild, xctool
  # doesn't support >1 destination, so we have to build first and then run the
  # tests one destination at a time.
  internal_xctool_debug_and_release \
    -workspace ios/Swift/imagepicker.xcworkspace \
    -scheme imagepicker \
    -sdk iphonesimulator \
    build
  # TODO: build-tests, when we have tests
  IOS_DESTINATIONS=(
    "platform=iOS Simulator,name=iPhone 4s,OS=8.1" # 32bit
    "platform=iOS Simulator,name=iPhone 6,OS=9.2" # 64bit
    "platform=iOS Simulator,name=iPad 2,OS=8.1" # 32bit
    "platform=iOS Simulator,name=iPad Air,OS=9.2" # 64bit
  )
  # TODO: run-tests, when we have tests
  # for i in "${IOS_DESTINATIONS[@]}" ; do
  #   internal_xctool_debug_and_release \
  #     -project objectivec/ProtocolBuffers_iOS.xcodeproj \
  #     -scheme ProtocolBuffers \
  #     -sdk iphonesimulator \
  #     -destination "${i}" \
  #     run-tests
  # done
}

# -------- main --------

if [ "$#" -ne 1 ]; then
  echo "
Usage: $0 { java_jdk7 |
            java_oracle7 |
            objectivec_ios |
            swift_ios }
"
  exit 1
fi

set -e  # exit immediately on error
set -x  # display all commands
eval "build_$1"
