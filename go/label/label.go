// Copyright 2016 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// Command label uses the Vision API's label detection capabilities to find a label
// based on an image's content.
//
//     go run label.go <path-to-image>
package main

import (
	"encoding/base64"
	"flag"
	"fmt"
	"io/ioutil"
	"os"
	"path/filepath"

	"golang.org/x/net/context"
	"golang.org/x/oauth2/google"
	"google.golang.org/api/vision/v1"
)

// run submits a label request on a single image by given file.
func run(file string) error {
	ctx := context.Background()

	// Authenticate to generate a vision service.
	client, err := google.DefaultClient(ctx, vision.CloudPlatformScope)
	if err != nil {
		return err
	}
	service, err := vision.New(client)
	if err != nil {
		return err
	}

	// Read the image.
	b, err := ioutil.ReadFile(file)
	if err != nil {
		return err
	}

	// Construct a label request, encoding the image in base64.
	req := &vision.AnnotateImageRequest{
		Image: &vision.Image{
			Content: base64.StdEncoding.EncodeToString(b),
		},
		Features: []*vision.Feature{{Type: "LABEL_DETECTION"}},
	}
	batch := &vision.BatchAnnotateImagesRequest{
		Requests: []*vision.AnnotateImageRequest{req},
	}
	res, err := service.Images.Annotate(batch).Do()
	if err != nil {
		return err
	}

	// Parse annotations from responses
	if annotations := res.Responses[0].LabelAnnotations; len(annotations) > 0 {
		label := annotations[0].Description
		fmt.Printf("Found label: %s for %s\n", label, file)
		return nil
	}
	fmt.Printf("Not found label: %s\n", file)
	return nil
}

func main() {
	flag.Usage = func() {
		fmt.Fprintf(os.Stderr, "Usage: %s <path-to-image>\n", filepath.Base(os.Args[0]))
	}
	flag.Parse()

	args := flag.Args()
	if len(args) == 0 {
		flag.Usage()
		os.Exit(1)
	}

	if err := run(args[0]); err != nil {
		fmt.Fprintf(os.Stderr, "%s\n", err.Error())
		os.Exit(1)
	}
}
