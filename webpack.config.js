/* global module, __dirname */
/* eslint no-process-env: "off" */

const path = require('path'),
      webpack = require('webpack'),
      CleanWebpackPlugin = require('clean-webpack-plugin');

const config = {
  context: __dirname,
  entry: {
    index: './app/assets/javascripts/app.js'
  },
  output: {
    filename: 'js/[name].bundle.js',
    chunkFilename: '[name].bundle.js',
    path: path.resolve(__dirname, 'public'),
    publicPath: '/assets/'
  },
  module: {
    rules: [
      {
        test: /\.js$/,
        exclude: path.resolve(__dirname, 'node_modules'),
        loader: 'babel-loader',
        options: {
          presets: [ 'env' ]
        }
      },
      {
        test: /\.css$/,
        loader: 'style-loader!css-loader'
      },
      {
        test: /\.less$/,
        use: [
          // creates style nodes from JS strings
          { loader: 'style-loader' },
          // translates CSS into CommonJS
          { loader: 'css-loader' },
          // compiles Less to CSS
          { loader: 'less-loader' }
        ]
      },
      {
        test: /\.html$/,
        loader: 'html-loader'
      },
      {
        test: /\.(png|jpg|jpeg|gif|svg|eot|woff|woff2|ttf)$/,
        use: [
          {
            loader: 'url-loader',
            options: {
              limit: 8192
            }
          }
        ]
      },
    ]
  },
  resolve: {
    modules: ['node_modules']
  },
  plugins : [
    new CleanWebpackPlugin([
      'public/*.hot-update.js',
      'public/*.hot-update.json',
      'public/*.woff2',
      'public/*.eot',
      'public/*.svg',
      'public/*.ttf',
      'public/stats.html',
      'public/css',
      'public/js'
    ]),
    new webpack.optimize.CommonsChunkPlugin({
      name: 'vendor',
      minChunks: function (module) {
        // this assumes your vendor imports exist in the node_modules directory
        return module.context && module.context.indexOf('node_modules') !== -1;
      }
    }),
    new webpack.optimize.CommonsChunkPlugin({
      name: 'manifest',
      minChunks: Infinity
    })
  ],

  performance: {
    hints: 'warning', // enum
    maxAssetSize: 200000, // int (in bytes),
    maxEntrypointSize: 400000, // int (in bytes)
    assetFilter: function(assetFilename) {
      // Function predicate that provides asset filenames
      return assetFilename.endsWith('.css') || assetFilename.endsWith('.js');
    }
  },
  externals: [],
  stats: 'normal'
};

module.exports = config;
