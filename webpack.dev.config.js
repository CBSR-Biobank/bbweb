/* global module, __dirname */

const path = require('path');
const webpack = require('webpack');
const config  = require('./webpack.config');

config.mode = 'development';
config.devtool = 'inline-source-map';
config.performance.hints = false;

config.plugins = config.plugins.concat([
  new webpack.DefinePlugin({
    DEVELOPMENT: JSON.stringify(true),
    PRODUCTION:  JSON.stringify(false),
    'process.env': {
      'NODE_ENV': JSON.stringify('development')
    }
  }),

  // Adds webpack HMR support. It act's like livereload,
  // reloading page after webpack rebuilt modules.
  // It also updates stylesheets and inline assets without page reloading.
  new webpack.HotModuleReplacementPlugin()
]);

config.devServer = {
  hot:         true, // this enables hot reload
  inline:      true, // use inline method for hmr
  host:        'localhost',
  port:        8080,
  contentBase: path.join(__dirname, 'public'),
  compress:    true,
  stats:       { colors: true },
  proxy: {
    '/api/**': {
      target: 'http://localhost:9000',
      logLevel: 'debug'
    }
  }
};

module.exports = config;
