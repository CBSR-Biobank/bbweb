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
      'domain',
      'home',
      'shipmentSpecimens',
      'studies',
      'users'
    ],

    paths: {
      'angular':                      '../lib/angular/angular',
      'angular-animate':              '../lib/angular-animate/angular-animate',
      'angular-cookies':              '../lib/angular-cookies/angular-cookies',
      'angular-gettext':              '../lib/angular-gettext/dist/angular-gettext',
      'angular-messages':             '../lib/angular-messages/angular-messages',
      'angular-sanitize':             '../lib/angular-sanitize/angular-sanitize',
      'angular-toastr':               '../lib/angular-toastr/dist/angular-toastr.tpls',
      'angular-ui-router':            '../lib/angular-ui-router/release/angular-ui-router',
      'angular-utils-ui-breadcrumbs': '../lib/angular-utils-ui-breadcrumbs/uiBreadcrumbs',
      'bootstrap':                    '../lib/bootstrap/dist/js/bootstrap',
      'bootstrap-ui-datetime-picker': '../lib/bootstrap-ui-datetime-picker/dist/datetime-picker',
      'jquery':                       '../lib/jquery/dist/jquery',
      'lodash':                       '../lib/lodash/lodash',
      'moment':                       '../lib/moment/moment',
      'requirejs':                    '../lib/requirejs/require',
      'smart-table':                  '../lib/angular-smart-table/dist/smart-table',
      'sprintf-js':                   '../lib/sprintf-js/dist/sprintf.min',
      'tv4':                          '../lib/tv4/tv4',
      'ui-bootstrap':                 '../lib/angular-ui-bootstrap/dist/ui-bootstrap-tpls'
    },

    shim: {
      'angular': {
        deps: ['jquery'],
        exports: 'angular'
      },
      'lodash': {
        exports: '_'
      },
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
      'ui-bootstrap':                 ['angular']
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
