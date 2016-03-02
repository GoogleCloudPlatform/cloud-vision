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

package main

import (
	"os"
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestLabel(t *testing.T) {
	// Skip tests that actually hit the API in short mode. We run with the
	// -test.short flag in Travis from external pull requests because the
	// credentials are not available.
	if testing.Short() {
		return
	}
	assert := assert.New(t)
	creds := os.Getenv("GOOGLE_APPLICATION_CREDENTIALS")
	const filename = "../../data/label/cat.jpg"

	os.Clearenv()
	assert.Error(run(filename))

	os.Setenv("GOOGLE_APPLICATION_CREDENTIALS", creds)
	assert.Error(run("no_exists.jpg"))
	assert.NoError(run(filename))
}
