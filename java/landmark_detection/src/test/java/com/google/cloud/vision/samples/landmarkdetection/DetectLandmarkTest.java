/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.vision.samples.landmarkdetection;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.vision.v1.model.EntityAnnotation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

/**
 * Unit tests for {@link DetectLandmark}.
 **/
@RunWith(JUnit4.class)
public class DetectLandmarkTest {
  private static final int MAX_RESULTS = 3;
  private static final String LANDMARK_URI = "gs://cloud-samples-tests/vision/water.jpg";
  private static final String PRIVATE_LANDMARK_URI =
      "gs://cloud-samples-tests/vision/water-private.jpg";

  @Test public void identifyLandmark_withLandmark_returnsKnownLandmark() throws Exception {
    // Arrange
    DetectLandmark appUnderTest = new DetectLandmark(DetectLandmark.getVisionService());

    // Act
    List<EntityAnnotation> landmarks = appUnderTest.identifyLandmark(LANDMARK_URI, MAX_RESULTS);

    // Assert
    assertThat(landmarks).named("water.jpg landmarks").isNotEmpty();
    assertThat(landmarks.get(0).getDescription())
        .named("water.jpg landmark #0 description")
        .isEqualTo("Taitung, Famous Places \"up the water flow\" marker");
  }

  @Test public void identifyLandmark_noImage_throwsNotFound() throws Exception {
    DetectLandmark appUnderTest = new DetectLandmark(DetectLandmark.getVisionService());

    try {
      appUnderTest.identifyLandmark(LANDMARK_URI + "/nonexistent.jpg", MAX_RESULTS);
      fail("Expected GoogleJsonResponseException");
    } catch (GoogleJsonResponseException expected) {
      assertThat(expected.getDetails().getCode())
          .named("GoogleJsonResponseException Error Code")
          .isEqualTo(HttpServletResponse.SC_NOT_FOUND);
    }
  }

  @Test public void identifyLandmark_noImage_throwsForbidden() throws Exception {
    DetectLandmark appUnderTest = new DetectLandmark(DetectLandmark.getVisionService());

    try {
      appUnderTest.identifyLandmark(PRIVATE_LANDMARK_URI, MAX_RESULTS);
      fail("Expected GoogleJsonResponseException");
    } catch (GoogleJsonResponseException expected) {
      assertThat(expected.getDetails().getCode())
          .named("GoogleJsonResponseException Error Code")
          .isEqualTo(HttpServletResponse.SC_FORBIDDEN);
    }
  }
}

