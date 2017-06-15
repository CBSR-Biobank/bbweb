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
      'users',
      {
        name: 'biobankTest',
        location: '../../../test/assets/javascripts/test'
      },
    ],

    paths: {
      'angular':                      '../../../target/web/web-modules/main/webjars/lib/angular/angular',
      'angular-animate':              '../../../target/web/web-modules/main/webjars/lib/angular-animate/angular-animate',
      'angular-cookies':              '../../../target/web/web-modules/main/webjars/lib/angular-cookies/angular-cookies',
      'angular-gettext':              '../../../target/web/web-modules/main/webjars/lib/angular-gettext/dist/angular-gettext',
      'angular-messages':             '../../../target/web/web-modules/main/webjars/lib/angular-messages/angular-messages',
      'angular-sanitize':             '../../../target/web/web-modules/main/webjars/lib/angular-sanitize/angular-sanitize',
      'angular-toastr':               '../../../target/web/web-modules/main/webjars/lib/angular-toastr/dist/angular-toastr.tpls',
      'angular-ui-router':            '../../../target/web/web-modules/main/webjars/lib/angular-ui-router/release/angular-ui-router',
      'bootstrap':                    '../../../target/web/web-modules/main/webjars/lib/bootstrap/dist/js/bootstrap',
      'bootstrap-ui-datetime-picker': '../../../target/web/web-modules/main/webjars/lib/bootstrap-ui-datetime-picker/dist/datetime-picker',
      'angularMocks':                 '../../../node_modules/angular-mocks/angular-mocks',
      'faker':                        '../../../node_modules/faker/build/build/faker',
      'jquery':                       '../../../target/web/web-modules/main/webjars/lib/jquery/dist/jquery',
      'lodash':                       '../../../target/web/web-modules/main/webjars/lib/lodash/lodash',
      'moment':                       '../../../target/web/web-modules/main/webjars/lib/moment/moment',
      'smart-table':                  '../../../target/web/web-modules/main/webjars/lib/angular-smart-table/dist/smart-table',
      'sprintf-js':                   '../../../target/web/web-modules/main/webjars/lib/sprintf-js/src/sprintf',
      'tv4':                          '../../../target/web/web-modules/main/webjars/lib/tv4/tv4',
      'ui-bootstrap':                 '../../../target/web/web-modules/main/webjars/lib/angular-ui-bootstrap/dist/ui-bootstrap-tpls',

      //
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
      'bootstrap':                    ['jquery'],
      'bootstrap-ui-datetime-picker': ['angular'],
      'smart-table':                  ['angular'],
      'ui-bootstrap':                 ['angular'],
      'biobankTest':                  ['angular'],
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
