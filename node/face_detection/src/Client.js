'use strict'
const __ = require('underscore'),
  createAPIRequest = require('googleapis/lib/apirequest')

const API_ANNOTATE = 'https://vision.googleapis.com/v1/images:annotate'

class Client {

  constructor(options) {
    this._options = options || {}
  }

  /**
   * annotate
   *
   * @desc Call cloud vision API
   *
   * @param  {array} requests - Parameters for request
   */
  annotate(requests) {
    return new Promise((resolve, reject) => {
      this._buildRequests(requests).then((params) => {
        var parameters = {
          options: {
            url: API_ANNOTATE,
            method: 'POST'
          },
          params: {
            resource: {
              requests: params
            }
          },
          requiredParams: [],
          pathParams: [],
          context: this
        }
        createAPIRequest(parameters, (err, response) => {
          if (err) {
            debugger;
            reject(err)
          } else {
            debugger;
            resolve(response)
          }
        })
      })
    })
  }

  _buildRequests(requests) {
    return Promise.all(__.map(requests, (req) => req.build()))
  }
}

module.exports = Client
