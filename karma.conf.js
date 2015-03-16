// Karma configuration
// Generated on Wed Oct 22 2014 14:27:38 GMT-0600 (MDT)

module.exports = function(config) {
  config.set({

    // base path that will be used to resolve all patterns (eg. files, exclude)
    basePath: './',

    // frameworks to use
    // available frameworks: https://npmjs.org/browse/keyword/karma-adapter
    frameworks: [
      'jasmine',
      'jasmine-matchers',
      'requirejs',
      'faker'
    ],

    // list of files / patterns to load in the browser
    files: [
      'target/web/web-modules/main/webjars/lib/underscorejs/underscore.js',
      {pattern: 'target/web/web-modules/main/webjars/lib/angularjs/angular.js', included: false},
      {pattern: 'target/web/web-modules/main/webjars/lib/angularjs/angular-*.js', included: false},
      {pattern: 'target/web/web-modules/main/webjars/lib/angular-ui-router/angular-ui-router.js', included: false},
      {pattern: 'target/web/web-modules/main/webjars/lib/angular-ui-bootstrap/ui-bootstrap-tpls.js', included: false},
      {pattern: 'target/web/web-modules/main/webjars/lib/angular-sanitize/angular-sanitize.js', included: false},
      {pattern: 'target/web/web-modules/main/webjars/lib/ng-table/ng-table.js', included: false},
      {pattern: 'target/web/web-modules/main/webjars/lib/jquery/jquery.js', included: false},
      {pattern: 'target/web/web-modules/main/webjars/lib/toastr/toastr.js', included: false},
      {pattern: 'target/web/web-modules/main/webjars/lib/momentjs/moment.js', included: false},
      {pattern: 'app/assets/javascripts/**/*.js', included: false},
      {pattern: 'test/assets/javascripts/**/*Spec.js', included: false},
      {pattern: 'test/assets/javascripts/test/**/*.js', included: false},
      'test/assets/javascripts/test-main.js',
      'test/assets/javascripts/fixtures/**/*.js'
    ],

    // list of files to exclude
    exclude: [
      'app/assets/javascripts/main.js',
      'target/web/web-modules/main/webjars/lib/angularjs/angular-*.min.js',
      'target/web/web-modules/main/webjars/lib/angularjs/angular-scenario.js'
    ],

    // preprocess matching files before serving them to the browser
    // available preprocessors: https://npmjs.org/browse/keyword/karma-preprocessor
    preprocessors: {
      //'app/assets/javascripts/**/*.js': 'coverage'
    },

    // test results reporter to use
    // possible values: 'dots', 'progress'
    // available reporters: https://npmjs.org/browse/keyword/karma-reporter
    reporters: [
      'spec',
      'coverage'
    ],

    specReporter: {
      maxLogLines: 10,
      prefixes: {
        // these are override here becasue the default values do not show up correctly when the tests are run
        // inside Emacs
        success: 'PASS ',
        failure: 'FAIL ',
        skipped: 'SKIPPED '
      }
    },

    coverageReporter: {
      type: 'html',
      dir: 'coverage'
    },

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

    // Continuous Integration mode
    // if true, Karma captures browsers, runs the tests and exits
    singleRun: true

  });
};
