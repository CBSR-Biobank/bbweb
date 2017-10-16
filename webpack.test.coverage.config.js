/* global module */

const path         = require('path'),
      webpack      = require('webpack'),
      commonConfig = require('./webpack.config'),
      _            = require('lodash');

const config = _.cloneDeep(commonConfig);

config.devtool = 'cheap-module-eval-source-map';

config.module.rules = config.module.rules
  .concat([{
    test: /\.js$/,
    enforce: 'post',
    use: {
      loader: 'istanbul-instrumenter-loader',
      options: { esModules: true }
    },
    include: path.resolve('app/assets/javascripts'),
    exclude: /(test|Spec.js)$/
  }]);

config.plugins = [
  new webpack.DefinePlugin({
      PRODUCTION: JSON.stringify(false)
  })
];

module.exports = config;
