/* global module */

const webpack = require('webpack');
const config  = require('./webpack.config');
const BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin;
const UglifyJsPlugin = require('uglifyjs-webpack-plugin');
const OptimizeCSSAssetsPlugin = require("optimize-css-assets-webpack-plugin");

config.mode = 'production';
config.cache = false;
config.devtool = 'cheap-module-source-map';

config.plugins = config.plugins.concat([
  new BundleAnalyzerPlugin({
    analyzerMode: 'static'
  }),

  new webpack.DefinePlugin({
    DEVELOPMENT: JSON.stringify(false),
    PRODUCTION:  JSON.stringify(true),
    'process.env': {
      'NODE_ENV': JSON.stringify('production')
    }
  }),

  new webpack.IgnorePlugin(/^\.\/locale$/, /moment$/)
]);

config.optimization.minimizer = [
  new UglifyJsPlugin({
    cache: true,
    parallel: true,
    sourceMap: true,
    uglifyOptions: {
      ecma: 5,
      warnings: true
    }
  }),
  new OptimizeCSSAssetsPlugin({})
];


module.exports = config;
