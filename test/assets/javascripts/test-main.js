/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
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
    'admin',
    'centres',
    'collection',
    'common',
    'dashboard',
    'domain',
    'home',
    'studies',
    'users'
  ],

  paths: {
    'jquery':                                '../../../target/web/web-modules/main/webjars/lib/jquery/jquery',
    'angular':                               '../../../target/web/web-modules/main/webjars/lib/angularjs/angular',
    'angularMocks':                          '../../../target/web/web-modules/main/webjars/lib/angularjs/angular-mocks',
    'angular-cookies':                       '../../../target/web/web-modules/main/webjars/lib/angularjs/angular-cookies',
    'underscore':                            '../../../target/web/web-modules/main/webjars/lib/underscorejs/underscore',
    'toastr':                                '../../../target/web/web-modules/main/webjars/lib/toastr/toastr',
    'smart-table':                           '../../../target/web/web-modules/main/webjars/lib/smart-table/smart-table',
    'angular-ui-router':                     '../../../target/web/web-modules/main/webjars/lib/angular-ui-router/angular-ui-router',
    'ui-bootstrap':                          '../../../target/web/web-modules/main/webjars/lib/angular-ui-bootstrap/ui-bootstrap-tpls',
    'angular-sanitize':                      '../../../target/web/web-modules/main/webjars/lib/angular-sanitize/angular-sanitize',
    'angularUtils.directives.uiBreadcrumbs': '../../../target/web/web-modules/main/webjars/lib/angular-utils-ui-breadcrumbs/uiBreadcrumbs',
    'moment':                                '../../../target/web/web-modules/main/webjars/lib/momentjs/moment',
    'faker':                                 '../../../node_modules/karma-faker/node_modules/faker/build/build/faker',
    'biobank.testUtils':                     '../../../test/assets/javascripts/fixtures/testUtils',
    'biobankTest':                           '../../../test/assets/javascripts/test/module',
    'biobankApp':                            'app'
  },

  shim: {
    'angular' : {
      'exports' : 'angular'
    },
    'angularMocks': {
      deps: ['angular'],
      exports: 'angular.mock'
    },
    'biobankApp': {
      deps: ['angular',
             'angular-ui-router',
             'angular-sanitize',
             'ui-bootstrap',
             'smart-table',
             'angularUtils.directives.uiBreadcrumbs',
             'angular-cookies'
            ],
      exports: 'biobankApp'
    }
  },

  // dynamically load all test files
  deps: allTestFiles,

  // we have to kickoff jasmine, as it is asynchronous
  callback: window.__karma__.start
});

requirejs.onError = function (err) {
  console.log('requireJS error', err.requireType);
  if (err.requireType === 'timeout') {
    console.log('modules: ' + err.requireModules);
  }

  throw err;
};
