/* global module */

const webpack = require('webpack'),
      path    = require('path'),
      config  = require('./webpack.config');

config.plugins = config.plugins.concat([

  // Reduces bundles total size
  new webpack.optimize.UglifyJsPlugin({
    compress: {
      screw_ie8: true
    },
    mangle: {

      // You can specify all variables that should not be mangled.
      // For example if your vendor dependency doesn't use modules
      // and relies on global variables. Most of angular modules relies on
      // angular global variable, so we should keep it unchanged
      except: ['$super', '$', 'exports', 'angular']
    }
  })
]);

module.exports = config;
