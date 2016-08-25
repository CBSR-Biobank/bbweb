/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
(function(window, require) {
  'use strict';

  var allTestFiles = [],
      TEST_REGEXP = /(spec|test)\.js$/i;

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
      //console.log(file);
      allTestFiles.push(path);
    }
  });

  require.config({
    // Karma serves files under /base, which is the basePath from your config file
    baseUrl: '/base/app/assets/javascripts',

    packages: [
      'common',
      'admin',
      {
        name: 'biobank.admin.centres',
        location: 'admin/centres'
      },
      {
        name: 'biobank.admin.studies',
        location: 'admin/studies'
      },
      {
        name: 'biobank.admin.users',
        location: 'admin/users'
      },
      'centres',
      'collection',
      'domain',
      'home',
      'studies',
      'users'
    ],

    paths: {
      'jquery':                       '../../../target/web/web-modules/main/webjars/lib/jquery/jquery',
      'bootstrap':                    '../../../target/web/web-modules/main/webjars/lib/bootstrap/js/bootstrap',
      'faker':                        '../../../node_modules/faker/build/build/faker',
      'moment':                       '../../../target/web/web-modules/main/webjars/lib/momentjs/moment',
      'toastr':                       '../../../target/web/web-modules/main/webjars/lib/toastr/toastr',
      'lodash':                       '../../../target/web/web-modules/main/webjars/lib/lodash/lodash',
      'angular':                      '../../../target/web/web-modules/main/webjars/lib/angularjs/angular',
      'angularMocks':                 '../../../target/web/web-modules/main/webjars/lib/angularjs/angular-mocks',
      'angular-messages':             '../../../target/web/web-modules/main/webjars/lib/angularjs/angular-messages',
      'angular-cookies':              '../../../target/web/web-modules/main/webjars/lib/angularjs/angular-cookies',
      'angular-sanitize':             '../../../target/web/web-modules/main/webjars/lib/angular-sanitize/angular-sanitize',
      'angular-ui-router':            '../../../target/web/web-modules/main/webjars/lib/angular-ui-router/angular-ui-router',
      'angular-utils-ui-breadcrumbs': '../../../target/web/web-modules/main/webjars/lib/angular-utils-ui-breadcrumbs/uiBreadcrumbs',
      'smart-table':                  '../../../target/web/web-modules/main/webjars/lib/smart-table/smart-table',
      'ui-bootstrap':                 '../../../target/web/web-modules/main/webjars/lib/angular-ui-bootstrap/ui-bootstrap-tpls',
      'sprintf':                      '../../../target/web/web-modules/main/webjars/lib/sprintf.js/sprintf.min',
      'tv4':                          '../../../target/web/web-modules/main/webjars/lib/tv4/tv4',
      'bootstrap-ui-datetime-picker': '../../../target/web/web-modules/main/webjars/lib/bootstrap-ui-datetime-picker/dist/datetime-picker',
      'angular-gettext':              '../../../target/web/web-modules/main/webjars/lib/angular-gettext/dist/angular-gettext',
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
      'angular-messages':             ['angular'],
      'angular-sanitize':             ['angular'],
      'angular-ui-router':            ['angular'],
      'angular-utils-ui-breadcrumbs': ['angular'],
      'bootstrap':                    ['jquery'],
      'smart-table':                  ['angular'],
      'ui-bootstrap':                 ['angular'],
      'bootstrap-ui-datetime-picker': ['angular'],
      'angular-gettext':              ['angular'],
      'biobankApp':                   { exports: 'biobankApp' }
    },

    // dynamically load all test files
    deps: allTestFiles,

    // we have to kickoff jasmine, as it is asynchronous
    callback: window.__karma__.start
  });

  require.onError = function (err) {
    console.log('requireJS error', err.requireType. err);
    if (err.requireType === 'timeout') {
      console.log('modules: ' + err.requireModules);
    }

    throw err;
  };

}(window, require));
