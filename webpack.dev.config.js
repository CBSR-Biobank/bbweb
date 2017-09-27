/* global module */

const webpack = require('webpack'),
      config  = require('./webpack.config');

config.devtool = 'eval-source-map';

config.plugins = config.plugins.concat([
  // Adds webpack HMR support. It act's like livereload,
  // reloading page after webpack rebuilt modules.
  // It also updates stylesheets and inline assets without page reloading.
  new webpack.HotModuleReplacementPlugin()
]);

module.exports = config;
