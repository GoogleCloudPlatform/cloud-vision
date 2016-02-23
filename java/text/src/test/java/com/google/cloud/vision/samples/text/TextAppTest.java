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

package com.google.cloud.vision.samples.text;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.vision.v1.model.FaceAnnotation;
import com.google.api.services.vision.v1.model.BoundingPoly;
import com.google.api.services.vision.v1.model.Vertex;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

/** Unit tests for {@link TextApp}. */
@RunWith(JUnit4.class)
public class TextAppTest {
  @Test public void detectText_withImage_returnsPath() throws Exception {
    // Arrange
    TextApp appUnderTest = new TextApp(TextApp.getVisionService(), null /* index */);

    // Act
    List<ImageText> image =
        appUnderTest.detectText(ImmutableList.<Path>of(Paths.get("../../data/text/wakeupcat.jpg")));

    // Assert
    assertThat(image.get(0).path().toString())
        .named("wakeupcat.jpg path")
        .isEqualTo("../../data/text/wakeupcat.jpg");
  }

  @Test public void extractDescriptions_withImage_returnsText() throws Exception {
    // Arrange
    TextApp appUnderTest = new TextApp(TextApp.getVisionService(), null /* index */);
    List<ImageText> image =
        appUnderTest.detectText(ImmutableList.<Path>of(Paths.get("../../data/text/wakeupcat.jpg")));

    // Act
    Word word = appUnderTest.extractDescriptions(image.get(0));

    // Assert
    assertThat(word.path().toString())
        .named("wakeupcat.jpg path")
        .isEqualTo("../../data/text/wakeupcat.jpg");
    assertThat(word.word().toLowerCase()).named("wakeupcat.jpg word").contains("wake");
    assertThat(word.word().toLowerCase()).named("wakeupcat.jpg word").contains("up");
    assertThat(word.word().toLowerCase()).named("wakeupcat.jpg word").contains("human");
  }
}
