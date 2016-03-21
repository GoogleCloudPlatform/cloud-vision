/**
 * Copyright 2016 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

// http makes an HTTP request and calls callback with parsed JSON.
var http = function(method, url, body, cb) {
  var xhr = new XMLHttpRequest();
  xhr.open(method, url, true);
  xhr.setRequestHeader('Content-Type', 'application/json');
  xhr.onreadystatechange = function() {
    if (xhr.readyState != 4) { return; }
    if (xhr.status >= 400) {
      notify('API request failed');
      console.log('XHR failed', xhr.responseText);
      return;
    }
    cb(JSON.parse(xhr.responseText));
  };
  xhr.send(body);
};

// Fetch the API key from config.json on extension startup.
http('GET', chrome.runtime.getURL('config.json'), '', function(obj) {
  API_KEY = obj.key;
});

API_KEY = "";
MAX_LABELS = 4; // Only show the top few labels for an image.
LINE_COLOR = '#f3f315';

// detect makes a Cloud Vision API request with the API key.
var detect = function(type, b64data, cb) {
  var url = 'https://vision.googleapis.com/v1/images:annotate?key='+API_KEY;
  var data = {
    requests: [{
      image: {content: b64data},
      features: [{'type':type}]
    }]
  }
  http('POST', url, JSON.stringify(data), cb);
};

var b64 = function(url, cb) {
  var image = new Image();
  image.setAttribute('crossOrigin', 'anonymous');
  image.onload = function() {
    var canvas = document.createElement('canvas');
    canvas.height = this.naturalHeight;
    canvas.width = this.naturalWidth;
    canvas.getContext('2d').drawImage(this, 0, 0);
    var b64data = canvas.toDataURL('image/png').replace(/^data:image\/(png|jpg);base64,/, '');
    cb(b64data);
  };
  image.src = url;
};

var notify = function(title, message) {
  chrome.notifications.create('', {
    'type':    'basic',
    'iconUrl': 'images/icon128.png',
    'title':   title,
    'message': message || '',
  }, function(nid) {
    // Automatically close the notification in 4 seconds.
    window.setTimeout(function() {
      chrome.notifications.clear(nid);
    }, 4000);
  });
};

// Returns true if successful.
var copyToClipboard = function(text) {
  var buffer = document.createElement('textarea');
  document.body.appendChild(buffer);
  buffer.value = text;
  buffer.focus();
  buffer.selectionStart = 0;
  buffer.selectionEnd = buffer.value.length;
  if (!document.execCommand('copy')) {
    console.log('Couldn\'t copy from buffer');
    return false;
  }
  buffer.remove(); // clean up the buffer node.
  return true;
};

chrome.contextMenus.create({
  title: 'Text detection',
  contexts: ['image'],
  onclick: function(obj) {
    b64(obj.srcUrl, function(b64data) {
      detect('TEXT_DETECTION', b64data, function(data) {
        // Get 'description' from first 'textAnnotation' of first 'response', if present.
        var text = (((data.responses || [{}])[0]).textAnnotations || [{}])[0].description || '';
        if (text == '') {
          notify('No text found');
          return;
        }

        if (copyToClipboard(text)) {
          notify('Text copied to clipboard', text);
        } else {
          notify('Failed to copy to clipboard');
        }
      });
    });
  },
}, function() {
  if (chrome.extension.lastError) {
    console.log('contextMenus.create: ', chrome.extension.lastError.message);
  }
});

chrome.contextMenus.create({
  title: 'Label detection',
  contexts: ['image'],
  onclick: function(obj) {
    b64(obj.srcUrl, function(b64data) {
      detect('LABEL_DETECTION', b64data, function(data) {
        var labels = (((data.responses || [{}])[0]).labelAnnotations || [{}])
        if (labels.length == 0) {
          notify('No labels detected');
          return;
        }
        var t = ''
        for (var i = 0; i < labels.length && i < MAX_LABELS; i++) {
          t += labels[i].description + ' (' + labels[i].score + ')\n'
        }
        notify('Labels detected', t);
      });
    });
  },
}, function() {
  if (chrome.extension.lastError) {
    console.log('contextMenus.create: ', chrome.extension.lastError.message);
  }
});

chrome.contextMenus.create({
  title: 'Face detection',
  contexts: ['image'],
  onclick: function(obj) {
    b64(obj.srcUrl, function(b64data) {
      detect('FACE_DETECTION', b64data, function(data) {
        var faces = (((data.responses || [{}])[0]).faceAnnotations || [{}])
        if (faces.length == 0) {
          notify('No faces detected');
          return;
        }
        drawFaces(b64data, faces);
      });
    });
  },
}, function() {
  if (chrome.extension.lastError) {
    console.log('contextMenus.create: ', chrome.extension.lastError.message);
  }
});

var drawFaces = function(b64data, faces) {
  // Draw on a canvas.
  var image = new Image();
  image.onload = function() {
    var canvas = document.createElement('canvas');
    canvas.height = this.naturalHeight;
    canvas.width = this.naturalWidth;
    var ctx = canvas.getContext('2d');
    ctx.strokeStyle = LINE_COLOR;
    ctx.lineWidth = 3;
    ctx.drawImage(image, 0, 0); // Draw the original image.
    for (var i = 0; i < faces.length; i++) {
      if (faces[i].fdBoundingPoly == undefined) { continue; }
      ctx.beginPath();
      var v = faces[i].fdBoundingPoly.vertices
      ctx.moveTo(v[0].x, v[0].y);
      for (var j = 1; j < v.length; j++) {
        if (v[j].x > this.naturalWidth) { v[j].x = this.naturalWidth; }
	if (v[j].y > this.naturalHeight) { v[j].y = this.naturalHeight; }
        ctx.lineTo(v[j].x, v[j].y);
      }
      ctx.lineTo(v[0].x, v[0].y);
      ctx.stroke();
    }
    chrome.tabs.create({
      url:canvas.toDataURL(),
    }, function() {
      if (chrome.extension.lastError) {
        console.log('tabs.create: ', chrome.extension.lastError.message);
      }
    });
  };
  image.src = 'data:image/png;base64,'+b64data;
};
