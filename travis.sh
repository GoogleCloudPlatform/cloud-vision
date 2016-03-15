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

add_ppa() {
  ppa=$1
  # Install the apt-add-repository command.
  sudo apt-get -qqy install \
      software-properties-common python-software-properties
  sudo apt-add-repository -y "${ppa}"
  sudo apt-get -qq update || true
}

use_java() {
  version=$1
  case "$version" in
    jdk8)
      add_ppa 'ppa:openjdk-r/ppa'
      sudo apt-get -qqy install openjdk-8-jdk
      export PATH=/usr/lib/jvm/java-8-openjdk-amd64/bin:$PATH
      ;;
    oracle8)
      echo "oracle-java8-installer shared/accepted-oracle-license-v1-1 select true" | \
          sudo debconf-set-selections
      add_ppa 'ppa:webupd8team/java'
      sudo apt-get -qqy install oracle-java8-installer
      export PATH=/usr/lib/jvm/java-8-oracle/bin:$PATH
      ;;
  esac

  which java
  java -version
}

build_java() {
  (
  cd java
  if [ -f "${GOOGLE_APPLICATION_CREDENTIALS}" ]; then
    mvn clean verify
  else
    echo "Application Credentials not available, skipping integration tests."
    mvn clean verify -DskipITs
  fi
  )
}

build_java_jdk8() {
  use_java jdk8
  build_java
}

build_java_oracle8() {
  use_java oracle8
  build_java
}

build_android() {
  # Set up Gradle, then update to latest version.
  # http://www.tothenew.com/blog/gradle-installation-in-ubuntu/
  add_ppa 'ppa:cwchien/gradle'
  sudo apt-get -qqy install gradle-2.11
  gradle -v
  # Set up the Android SDK.
  # https://gist.github.com/wenzhixin/43cf3ce909c24948c6e7
  # We can't use the default Android Travis runtime, because we are a
  # multi-language project.
  # Need 32-bit versions of the libraries.
  # http://stackoverflow.com/a/19524010/101923
  sudo dpkg --add-architecture i386
  sudo apt-get -qqy update || true
  # adb
  sudo apt-get -qqy install libc6:i386 libstdc++6:i386
  # aapt
  sudo apt-get -qqy install zlib1g:i386
  wget http://dl.google.com/android/android-sdk_r24.4.1-linux.tgz
  tar xzf android-sdk_r24.4.1-linux.tgz
  export ANDROID_HOME=$PWD/android-sdk-linux
  export PATH="${PATH}:${ANDROID_HOME}/tools:${ANDROID_HOME}/platform-tools"
  # Accept the license.
  # http://stackoverflow.com/a/17863931/101923
  sudo apt-get -qqy install expect
  expect -c '
  set timeout 600   ;
  spawn android update sdk --no-ui --force --all --filter tools,platform-tools,build-tools-23.0.2,android-23,extra-android-support,extra-android-m2repository,extra-google-m2repository;
  expect {
    "Do you accept the license" { exp_send "y\r" ; exp_continue }
    eof
  }
  '
  # Build and test the app!
  (
  cd android/CloudVision
  ./gradlew --console=plain assembleDebug && \
      ./gradlew --console=plain lintDebug && \
      ./gradlew --console=plain testDebugUnitTest
  )
}

build_android_jdk8() {
  use_java jdk8
  build_android
}

build_android_oracle8() {
  use_java oracle8
  build_android
}

build_go() {
  # Install
  if [ $(uname -s) == "Linux" ]; then
    sudo apt-get -qqy install golang
  else
    mkdir -p "${HOME}/bin"
    (
    cd "${HOME}/bin"
    wget https://storage.googleapis.com/golang/go1.6.darwin-amd64.tar.gz
    tar -xzf go1.6.darwin-amd64.tar.gz
    export GOROOT="${HOME}/bin/go"
    export PATH="${GOROOT}/bin:${PATH}"
    )
  fi
  which go
  go version

  # Configure
  export GOPATH="${HOME}/gocode"
  export GOBIN="${GOPATH}/bin"
  export PATH="${PATH}:${GOBIN}"
  mkdir -p "${GOBIN}"
  go get -u github.com/golang/lint/golint

  # Run build & tests
  go get -t -v ./go/...
  go vet ./go/...
  if [ -f "${GOOGLE_APPLICATION_CREDENTIALS}" ]; then
    go test -v ./go/...
  else
    echo "Application Credentials not available, skipping integration tests."
    go test -v -short ./go/...
  fi
  GOFMT=$(gofmt -d -s .) && echo $GOFMT && test -z "$GOFMT"
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
  # IOS_DESTINATIONS=(
  #   "platform=iOS Simulator,name=iPhone 4s,OS=8.1" # 32bit
  #   "platform=iOS Simulator,name=iPhone 6,OS=9.2" # 64bit
  #   "platform=iOS Simulator,name=iPad 2,OS=8.1" # 32bit
  #   "platform=iOS Simulator,name=iPad Air,OS=9.2" # 64bit
  # )
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
  # IOS_DESTINATIONS=(
  #   "platform=iOS Simulator,name=iPhone 4s,OS=8.1" # 32bit
  #   "platform=iOS Simulator,name=iPhone 6,OS=9.2" # 64bit
  #   "platform=iOS Simulator,name=iPad 2,OS=8.1" # 32bit
  #   "platform=iOS Simulator,name=iPad Air,OS=9.2" # 64bit
  # )
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

build_php() {
  if [ $(uname -s) == "Linux" ]; then
    sudo apt-get update -qq
    sudo apt-get install -yqq php5
  fi
  wget http://get.sensiolabs.org/php-cs-fixer.phar -O php-cs-fixer.phar
  php php-cs-fixer.phar fix --dry-run --diff --level=psr2 \
      --fixers=concat_with_spaces,unused_use,trailing_spaces,indentation ./php
  # TODO: run composer install & phpunit
}

# -------- main --------

if [ "$#" -ne 1 ]; then
  echo "
Usage: $0 { android_jdk8 |
            android_oracle8 |
            java_jdk8 |
            java_oracle8 |
            objectivec_ios |
            php |
            swift_ios }
"
  exit 1
fi

set -e  # exit immediately on error
set -x  # display all commands
eval "build_$1"
