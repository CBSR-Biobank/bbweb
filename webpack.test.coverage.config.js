/* global module */

const webpack              = require('webpack');
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const commonConfig         = require('./webpack.config');
const _                    = require('lodash');

const config = _.cloneDeep(commonConfig);

config.mode = 'development';
config.devtool = 'inline-source-map';

config.module.rules = config.module.rules.concat([{
  test: /\.js$/,
  use: {
    loader: 'istanbul-instrumenter-loader',
    options: { esModules: true }
  },
  enforce: 'post',
  exclude: /(?:[\\\/]node_modules[\\\/]|[\\\/]test[\\\/]|Spec\.js)/
}]);

config.plugins = [
  new webpack.DefinePlugin({
    PRODUCTION: JSON.stringify(false),
    DEVELOPMENT: JSON.stringify(false),
    'process.env': {
      'NODE_ENV': JSON.stringify('development')
    }
  }),
  new MiniCssExtractPlugin({
    filename: 'css/[name].css'
  }),
];

config.optimization = {};
config.performance.hints = false;

module.exports = config;
