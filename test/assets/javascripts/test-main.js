/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
(function(window, require) {
  'use strict';

  var allTestFiles = [];
  var TEST_REGEXP = /(spec|test)\.js$/i;

  var pathToModule = function(path) {
    var module = '../../../' + path.replace(/^\/base\//, '').replace(/\.js$/, '');
    //console.log(path, module);
    return module;
  };

  Object.keys(window.__karma__.files).forEach(function(file) {
    var path;

    if (TEST_REGEXP.test(file)) {
      // Normalize paths to RequireJS module names.
      path = pathToModule(file);
      allTestFiles.push(path);
    }
  });

  require.config({
    // Karma serves files under /base, which is the basePath from your config file
    baseUrl: '/base/app/assets/javascripts',

    packages: [
      'common',
      'admin',
      'centres',
      'collection',
      'dashboard',
      'domain',
      'home',
      'studies',
      'users'
    ],

    paths: {
      'jquery':                       '../../../target/web/web-modules/main/webjars/lib/jquery/jquery',
      'bootstrap':                    '../../../target/web/web-modules/main/webjars/lib/bootstrap/js/bootstrap',
      'faker':                        '../../../node_modules/karma-faker/node_modules/faker/build/build/faker',
      'moment':                       '../../../target/web/web-modules/main/webjars/lib/momentjs/moment',
      'toastr':                       '../../../target/web/web-modules/main/webjars/lib/toastr/toastr',
      'underscore':                   '../../../target/web/web-modules/main/webjars/lib/underscorejs/underscore',
      'angular':                      '../../../target/web/web-modules/main/webjars/lib/angularjs/angular',
      'angularMocks':                 '../../../target/web/web-modules/main/webjars/lib/angularjs/angular-mocks',
      'angular-cookies':              '../../../target/web/web-modules/main/webjars/lib/angularjs/angular-cookies',
      'angular-sanitize':             '../../../target/web/web-modules/main/webjars/lib/angular-sanitize/angular-sanitize',
      'angular-ui-router':            '../../../target/web/web-modules/main/webjars/lib/angular-ui-router/angular-ui-router',
      'angular-utils-ui-breadcrumbs': '../../../target/web/web-modules/main/webjars/lib/angular-utils-ui-breadcrumbs/uiBreadcrumbs',
      'smart-table':                  '../../../target/web/web-modules/main/webjars/lib/smart-table/smart-table',
      'ui-bootstrap':                 '../../../target/web/web-modules/main/webjars/lib/angular-ui-bootstrap/ui-bootstrap-tpls',
      'sprintf':                      '../../../target/web/web-modules/main/webjars/lib/sprintf.js/sprintf.min',
      'tv4':                          '../../../target/web/web-modules/main/webjars/lib/tv4/tv4',
      'biobankTest':                  '../../../test/assets/javascripts/test/module',
      'biobankApp':                   'app'
    },

    shim: {
      'angular' : { exports : 'angular' },
      'angularMocks': {
        deps: ['angular'],
        exports: 'angular.mock'
      },
      'angular-cookies':              ['angular'],
      'angular-sanitize':             ['angular'],
      'angular-ui-router':            ['angular'],
      'bootstrap':                    ['jquery'],
      'smart-table':                  ['angular'],
      'ui-bootstrap':                 ['angular'],
      'angular-utils-ui-breadcrumbs': ['angular'],
      'biobankApp':                   { exports: 'biobankApp' }
    },

    // dynamically load all test files
    deps: allTestFiles,

    // we have to kickoff jasmine, as it is asynchronous
    callback: window.__karma__.start
  });

  require.onError = function (err) {
    console.log('requireJS error', err.requireType);
    if (err.requireType === 'timeout') {
      console.log('modules: ' + err.requireModules);
    }

    throw err;
  };

}(window, require));
