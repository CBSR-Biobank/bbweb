(function(requirejs) {
  'use strict';

  // -- RequireJS config --
  requirejs.config({
    // Packages = top-level folders; loads a contained file named 'main.js'
    packages: ['common', 'home', 'user', 'study', 'dashboard'],
    shim: {
      'jsRoutes' : {
        deps : [],
        // it's not a RequireJS module, so we have to tell it what var is returned
        exports : 'jsRoutes'
      },
      // Hopefully this all will not be necessary but can be fetched from WebJars in the future
      'angular': {
        deps: ['jquery'],
        exports: 'angular'
      },
      'angular-route': ['angular'],
      'angular-cookies': ['angular'],
      'bootstrap': ['jquery'],
      'ui-bootstrap-tpls': ['angular', 'bootstrap'],
      'ng-grid': ['angular', 'jquery']
    },
    paths: {
      'requirejs': ['../lib/requirejs/require'],
      'jquery': ['../lib/jquery/jquery'],
      'angular': ['../lib/angularjs/angular'],
      'angular-route': ['../lib/angularjs/angular-route'],
      'angular-cookies': ['../lib/angularjs/angular-cookies'],
      'bootstrap': ['../lib/bootstrap/js/bootstrap'],
      'ui-bootstrap-tpls': ['../lib/angular-ui-bootstrap/ui-bootstrap-tpls'],
      'ng-grid': ['../lib/ng-grid/ng-grid'],
      'jsRoutes': ['/jsroutes']
    }
  });

  requirejs.onError = function(err) {
    console.log(err);
  };

  // Load the app. This is kept minimal so it doesn't need much updating.
  require([
    'angular',
    'angular-cookies',
    'angular-route',
    'ui-bootstrap-tpls',
    'ng-grid',
    'jquery',
    'bootstrap',
    './app'],
          function(angular) {
            angular.bootstrap(document, ['app']);
          }
         );
})(requirejs);
