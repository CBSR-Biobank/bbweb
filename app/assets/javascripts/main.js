/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
(function(requirejs) {
   'use strict';

   // -- RequireJS config --
   requirejs.config({
      // Packages = top-level folders; loads a contained file named 'main.js'
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
         'requirejs':                    '../lib/requirejs/require',
         'jquery':                       '../lib/jquery/jquery',
         'angular':                      '../lib/angularjs/angular',
         'underscore':                   '../lib/underscorejs/underscore',
         'angular-route':                '../lib/angularjs/angular-route',
         'angular-cookies':              '../lib/angularjs/angular-cookies',
         'angular-ui-router':            '../lib/angular-ui-router/angular-ui-router',
         'bootstrap':                    '../lib/bootstrap/js/bootstrap',
         'ui-bootstrap':                 '../lib/angular-ui-bootstrap/ui-bootstrap-tpls',
         'ngTable':                      '../lib/ng-table/ng-table',
         'angular-utils-ui-breadcrumbs': '../lib/angular-utils-ui-breadcrumbs/uiBreadcrumbs',
         'toastr':                       '../lib/toastr/toastr',
         'angular-sanitize':             '../lib/angular-sanitize/angular-sanitize',
         'moment':                       '../lib/momentjs/moment'
      },

      shim: {
         'angular': {
            deps: ['jquery'],
            exports: 'angular'
         },
         'underscore': {
            exports: '_'
         },
         'angular-cookies':              ['angular'],
         'angular-sanitize':             ['angular'],
         'angular-ui-router':            ['angular'],
         'bootstrap':                    ['jquery'],
         'ngTable':                      ['angular'],
         'ui-bootstrap':                 ['angular'],
         'angular-utils-ui-breadcrumbs': ['angular']
      }
   });

   requirejs.onError = function(err) {
      console.log(err);
   };

   // Load the app. This is kept minimal so it doesn't need much updating.
   require([
      'angular',
      'jquery',
      'bootstrap',
      'angular-cookies',
      'angular-ui-router',
      'ui-bootstrap',
      'ngTable',
      'angular-utils-ui-breadcrumbs',
      'angular-sanitize',
      './app'
   ], function(angular) {
      angular.bootstrap(document, ['biobankApp']);
   });
})(requirejs);
