/* global module, __dirname */

const path = require('path'),
      webpack = require('webpack'),
      config  = require('./webpack.config');

config.devtool = 'eval-source-map';

config.module.rules = config.module.rules
  .filter((rule) => !'.css'.match(rule.test))
  .concat([{
    test: /\.css$/,
    loader: 'style-loader!css-loader'
  }]);

config.plugins = [
  // Adds webpack HMR support. It act's like livereload,
  // reloading page after webpack rebuilt modules.
  // It also updates stylesheets and inline assets without page reloading.
  new webpack.HotModuleReplacementPlugin()
];

module.exports = config;
