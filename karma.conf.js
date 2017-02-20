/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 *
 * Karma configuration
 */
module.exports = function(config) {
  'use strict';

  var path = require("path");

  config.set({

    // base path that will be used to resolve all patterns (eg. files, exclude)
    basePath: './',

    // frameworks to use
    // available frameworks: https://npmjs.org/browse/keyword/karma-adapter
    frameworks: [
      'jasmine-jquery',
      'jasmine',
      'jasmine-matchers',
      'requirejs'
    ],

    // list of files / patterns to load in the browser
    files: [
      { pattern: 'target/web/web-modules/main/webjars/lib/angular/angular.js', included: false },
      { pattern: 'target/web/web-modules/main/webjars/lib/angular-animate/angular-animate.js', included: false },
      { pattern: 'target/web/web-modules/main/webjars/lib/angular-cookies/angular-cookies.js', included: false },
      { pattern: 'target/web/web-modules/main/webjars/lib/angular-gettext/dist/angular-gettext.js', included: false },
      { pattern: 'target/web/web-modules/main/webjars/lib/angular-messages/angular-messages.js', included: false },
      { pattern: 'target/web/web-modules/main/webjars/lib/angular-sanitize/angular-sanitize.js', included: false },
      { pattern: 'target/web/web-modules/main/webjars/lib/angular-smart-table/dist/smart-table.js', included: false },
      { pattern: 'target/web/web-modules/main/webjars/lib/angular-toastr/dist/angular-toastr.tpls.js', included: false },
      { pattern: 'target/web/web-modules/main/webjars/lib/angular-ui-bootstrap/dist/ui-bootstrap-tpls.js', included: false },
      { pattern: 'target/web/web-modules/main/webjars/lib/angular-ui-router/release/angular-ui-router.js', included: false },
      { pattern: 'target/web/web-modules/main/webjars/lib/angular-utils-ui-breadcrumbs/uiBreadcrumbs.js', included: false },
      { pattern: 'target/web/web-modules/main/webjars/lib/bootstrap/dist/js/bootstrap.js', included: false },
      { pattern: 'target/web/web-modules/main/webjars/lib/bootstrap-ui-datetime-picker/dist/datetime-picker.js', included: false },
      { pattern: 'target/web/web-modules/main/webjars/lib/jquery/dist/jquery.js', included: false },
      { pattern: 'target/web/web-modules/main/webjars/lib/lodash/lodash.js', included: false },
      { pattern: 'target/web/web-modules/main/webjars/lib/moment/moment.js', included: false },
      { pattern: 'target/web/web-modules/main/webjars/lib/sprintf-js/src/sprintf.js', included: false },
      { pattern: 'target/web/web-modules/main/webjars/lib/tv4/tv4.js', included: false },
      { pattern: 'node_modules/angular-mocks/angular-mocks.js', included: false },
      { pattern: 'node_modules/faker/build/build/faker.js', included: false },
      { pattern: 'app/assets/javascripts/**/*.js', included: false },
      { pattern: 'test/assets/javascripts/**/*Spec.js', included: false },
      { pattern: 'test/assets/javascripts/test/**/*.js', included: false },

      { pattern: 'app/assets/javascripts/**/*.html', watched: true, included: false, served: true },
      'test/assets/javascripts/test-main.js',
    ],

    // list of files to exclude
    exclude: [
      'app/assets/javascripts/main.js'
    ],

    // preprocess matching files before serving them to the browser
    // available preprocessors: https://npmjs.org/browse/keyword/karma-preprocessor
    preprocessors: {
      //'app/assets/javascripts/**/*.js': 'coverage' -> moved to Gruntfile.js
    },

    // test results reporter to use
    // possible values: 'dots', 'progress'
    // available reporters: https://npmjs.org/browse/keyword/karma-reporter
    reporters: [
      'dots'
      //'spec'
      //'failed'
    ],

    specReporter: {
      maxLogLines: 10,
      prefixes: {
        // these are override here becasue the default values do not show up correctly when the tests are run
        // inside Emacs
        success: 'PASSED  ',
        failure: 'FAILED  ',
        skipped: 'SKIPPED '
      }
    },

    // -> moved to Gruntfile.js
    // coverageReporter: {
    //   type: 'html',
    //   dir: 'coverage'
    // },

    // web server port
    port: 9876,

    // enable / disable colors in the output (reporters and logs)
    colors: true,

    // level of logging
    // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
    logLevel: config.LOG_INFO,

    // enable / disable watching file and executing tests whenever any file changes
    autoWatch: false,

    // start these browsers
    // available browser launchers: https://npmjs.org/browse/keyword/karma-launcher
    browsers: [
      'PhantomJS'
      // 'ChromeExtra'
    ],

    customLaunchers: {
      Chrome_with_debugging: {
        base: 'Chrome',
        chromeDataDir: path.resolve(__dirname, '.chrome')
      }
    },

    // Continuous Integration mode
    // if true, Karma captures browsers, runs the tests and exits
    singleRun: true

  });
};
