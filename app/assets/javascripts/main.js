/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
(function(requirejs) {
  'use strict';

  // -- RequireJS config --
  requirejs.config({
    baseUrl: '/assets/javascripts',

    // Packages = top-level directories; loads a contained file named 'main.js'
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
      'dashboard',
      'domain',
      'home',
      'studies',
      'users'
    ],

    paths: {
      'requirejs':                    '../lib/requirejs/require',
      'jquery':                       '../lib/jquery/jquery',
      'toastr':                       '../lib/toastr/toastr',
      'lodash':                       '../lib/lodash/lodash',
      'underscore':                   '../lib/underscorejs/underscore',
      'moment':                       '../lib/momentjs/moment',
      'bootstrap':                    '../lib/bootstrap/js/bootstrap',
      'angular':                      '../lib/angularjs/angular',
      'angular-cookies':              '../lib/angularjs/angular-cookies',
      'angular-messages':             '../lib/angularjs/angular-messages',
      'angular-ui-router':            '../lib/angular-ui-router/angular-ui-router',
      'angular-sanitize':             '../lib/angular-sanitize/angular-sanitize',
      'angular-utils-ui-breadcrumbs': '../lib/angular-utils-ui-breadcrumbs/uiBreadcrumbs',
      'ui-bootstrap':                 '../lib/angular-ui-bootstrap/ui-bootstrap-tpls',
      'smart-table':                  '../lib/smart-table/smart-table',
      'sprintf':                      '../lib/sprintf.js/sprintf.min',
      'tv4':                          '../lib/tv4/tv4',
      'bootstrap-ui-datetime-picker': '../lib/bootstrap-ui-datetime-picker/dist/datetime-picker'
    },

    shim: {
      'angular': {
        deps: ['jquery'],
        exports: 'angular'
      },
      'underscore': {
        exports: '_'
      },
      'bootstrap':                    ['jquery'],
      'angular-cookies':              ['angular'],
      'angular-messages':             ['angular'],
      'angular-sanitize':             ['angular'],
      'angular-ui-router':            ['angular'],
      'angular-utils-ui-breadcrumbs': ['angular'],
      'smart-table':                  ['angular'],
      'ui-bootstrap':                 ['angular'],
      'bootstrap-ui-datetime-picker': ['angular']
    }
  });

  requirejs.onError = function(err) {
    console.log(err);
  };

  // Load the app. This is kept minimal so it doesn't need much updating.
  require([ 'angular', 'app' ], function(angular) {
    angular.bootstrap(document, ['biobankApp']);
  });
})(requirejs);
