'use strict'
const fs = require('fs'),
  request = require('request'),
  __ = require('underscore')

class Image {
  constructor(path) {
    const options = __.isObject(path) ? path : {
      path: path
    }
    this._path = options.path
    this._url = options.url
    this._base64 = options.base64
  }
  load() {
    return new Promise((resolve, reject) => {
      if (this._path) {
        resolve(fs.readFileSync(this._path).toString('base64'))
      } else if (this._url) {
        this._loadRemote().then(resolve)
      } else if (this._base64) {
        this._base64 = this._base64.substring(this._base64.indexOf(',') + 1); // remove 'data:image/jpeg;base64,' if included
        resolve(this._base64)
      } else {
        console.log('No path or url are specified in image')
        reject()
      }
    })
  }
  _loadRemote() {
    return new Promise((resolve, reject) => {
      request({
        url: this._url,
        encoding: null
      }, (err, response, body) => {
        if (!err && response.statusCode == 200) {
          resolve(new Buffer(body).toString('base64'))
        } else {
          console.log('Error while loading image. code: ' + response.statusCode, err)
          resolve('')
        }
      })
    })
  }

  build() {
    return new Promise((resolve, reject) => {
      this.load().then((cotent) => {
        resolve({
          content: content
        })
      })
    })
  }
}

module.exports = Image
