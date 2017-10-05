/* global module */

const webpack = require('webpack'),
      config  = require('./webpack.config');
//, BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin,
//, CompressionPlugin = require('compression-webpack-plugin');

config.cache = false;
config.devtool = 'cheap-module-source-map';

config.plugins = config.plugins.concat([
  //new BundleAnalyzerPlugin(),

  new webpack.DefinePlugin({
    'process.env.NODE_ENV': '"production"'
  }),

  new webpack.optimize.DedupePlugin(),

  new webpack.optimize.UglifyJsPlugin({
    ie8: false,
    parallel: true,
    exclude: [/\.min\.js$/gi], // skip pre-minified libs
    mangle: true,
    compress: {
      warnings: false // Suppress uglification warnings
    },
    output: {
      comments: false,
      beautify: false
    }
  }),
  // new CompressionPlugin({
  //   asset: '[path].gz[query]',
  //   algorithm: 'gzip',
  //   test: /\.js$|\.css$|\.html$/,
  //   threshold: 10240,
  //   minRatio: 0
  // })
  new webpack.IgnorePlugin(/^\.\/locale$/, /moment$/),
  new webpack.NoEmitOnErrorsPlugin()
]);

module.exports = config;
