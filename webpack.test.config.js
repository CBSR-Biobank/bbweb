/* global module */

const webpack              = require('webpack');
const commonConfig         = require('./webpack.config');
const _                    = require('lodash');

const config = _.cloneDeep(commonConfig);

config.mode = 'development';
//config.devtool = 'inline-source-map';
config.devtool = 'eval-source-map';

config.plugins = config.plugins.concat([
  new webpack.DefinePlugin({
    PRODUCTION:  JSON.stringify(false),
    DEVELOPMENT: JSON.stringify(false),
    'process.env': {
      'NODE_ENV': JSON.stringify('development')
    }
  })
]);

config.optimization = {};

config.watch = true;
config.watchOptions = {
  ignored: /node_modules/,
  aggregateTimeout: 300,
  poll: true
};

module.exports = config;
