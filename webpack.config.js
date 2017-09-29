/* global module, __dirname */
/* eslint no-process-env: "off" */

const path = require('path'),
      webpack = require('webpack'),
      ExtractTextPlugin = require('extract-text-webpack-plugin'),
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
          presets: [ 'es2015' ]
        }
      },
      {
        test: /\.css$/,
        use: ExtractTextPlugin.extract({
          fallback: 'style-loader',
          use: 'css-loader'
        })
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
      'public/*.woff2',
      'public/*.eot',
      'public/*.svg',
      'public/*.ttf',
      'public/css',
      'public/js'
    ]),
    new webpack.optimize.CommonsChunkPlugin({
      name: 'common' // Specify the common bundle's name.
    }),
    new ExtractTextPlugin('css/styles.css')
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
