/* global module */

const webpack = require('webpack'),
      commonConfig  = require('./webpack.config'),
      _       = require('lodash');

const config = _.cloneDeep(commonConfig);

// see http://cheng.logdown.com/posts/2016/03/25/679045
config.devtool = 'eval-source-map';

config.plugins = [
  new webpack.DefinePlugin({
    PRODUCTION: JSON.stringify(false),
    DEVELOPMENT: JSON.stringify(false)
  }),
  // Adds webpack HMR support. It act's like livereload,
  // reloading page after webpack rebuilt modules.
  // It also updates stylesheets and inline assets without page reloading.
  new webpack.HotModuleReplacementPlugin()
];

config.watchOptions = {
  ignored: /node_modules/,
  aggregateTimeout: 300,
  poll: 1000
};

module.exports = config;
