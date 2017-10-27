/* global module */

const path         = require('path'),
      webpack      = require('webpack'),
      commonConfig = require('./webpack.config'),
      _            = require('lodash');

const config = _.cloneDeep(commonConfig);

// see http://cheng.logdown.com/posts/2016/03/25/679045
config.devtool = 'eval-source-map';

config.module.rules = config.module.rules
  .concat([{
    test: /\.js$/,
    enforce: 'post',
    use: {
      loader: 'istanbul-instrumenter-loader',
      options: { esModules: true }
    },
    include: path.resolve('app/assets/javascripts'),
    exclude: /(?:[\\\/]test[\\\/]|Spec\.js)/
  }]);

config.plugins = [
  new webpack.DefinePlugin({
    PRODUCTION: JSON.stringify(false),
    DEVELOPMENT: JSON.stringify(false)
  })
];

module.exports = config;
