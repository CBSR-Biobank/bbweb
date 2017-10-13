/* global module */

const path         = require('path'),
      commonConfig = require('./webpack.config'),
      _            = require('lodash');

const config = _.cloneDeep(commonConfig);

config.devtool = 'cheap-module-eval-source-map';

config.module.rules = config.module.rules
  .filter((rule) => !'.js'.match(rule.test))
  .concat([{
    test: /\.js$/,
    use: {
      loader: 'istanbul-instrumenter-loader',
      options: { esModules: true }
    },
    include: path.resolve('app/assets/javascripts'),
    enforce: 'post',
    exclude: /(test|Spec.js$)/
  }]);

config.plugins = [];

module.exports = config;
