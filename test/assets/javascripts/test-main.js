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
      'shipmentSpecimens',
      'studies',
      'users'
    ],

    paths: {
      'angular':                      '../../../target/web/public/main/lib/angular/angular',
      'angularMocks':                 '../../../node_modules/angular-mocks/angular-mocks',
      'angular-animate':              '../../../target/web/public/main/lib/angular-animate/angular-animate',
      'angular-cookies':              '../../../target/web/public/main/lib/angular-cookies/angular-cookies',
      'angular-gettext':              '../../../target/web/public/main/lib/angular-gettext/dist/angular-gettext',
      'angular-messages':             '../../../target/web/public/main/lib/angular-messages/angular-messages',
      'angular-sanitize':             '../../../target/web/public/main/lib/angular-sanitize/angular-sanitize',
      'angular-toastr':               '../../../target/web/public/main/lib/angular-toastr/dist/angular-toastr.tpls',
      'angular-ui-router':            '../../../target/web/public/main/lib/angular-ui-router/release/angular-ui-router',
      'angular-utils-ui-breadcrumbs': '../../../target/web/public/main/lib/angular-utils-ui-breadcrumbs/uiBreadcrumbs',
      'bootstrap':                    '../../../target/web/public/main/lib/bootstrap/dist/js/bootstrap',
      'bootstrap-ui-datetime-picker': '../../../target/web/public/main/lib/bootstrap-ui-datetime-picker/dist/datetime-picker',
      'faker':                        '../../../target/web/public/main/lib/faker/build/build/faker',
      'jquery':                       '../../../target/web/public/main/lib/jquery/dist/jquery',
      'lodash':                       '../../../target/web/public/main/lib/lodash/lodash',
      'moment':                       '../../../target/web/public/main/lib/moment/moment',
      'smart-table':                  '../../../target/web/public/main/lib/angular-smart-table/dist/smart-table',
      'sprintf-js':                   '../../../target/web/public/main/lib/sprintf-js/src/sprintf',
      'tv4':                          '../../../target/web/public/main/lib/tv4/tv4',
      'ui-bootstrap':                 '../../../target/web/public/main/lib/angular-ui-bootstrap/dist/ui-bootstrap-tpls',

      //
      'biobankTest':                  '../../../test/assets/javascripts/test/module',
      'biobankApp':                   'app'
    },

    shim: {
      'angular' :                     { exports : 'angular' },
      'angularMocks':                 { deps: ['angular'], exports: 'angular.mock' },
      'angular-animate':              ['angular'],
      'angular-cookies':              ['angular'],
      'angular-gettext':              ['angular'],
      'angular-messages':             ['angular'],
      'angular-sanitize':             ['angular'],
      'angular-toastr':               { deps: ['angular'], exports: 'toastr' },
      'angular-ui-router':            ['angular'],
      'angular-utils-ui-breadcrumbs': ['angular'],
      'bootstrap':                    ['jquery'],
      'bootstrap-ui-datetime-picker': ['angular'],
      'smart-table':                  ['angular'],
      'ui-bootstrap':                 ['angular'],
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
