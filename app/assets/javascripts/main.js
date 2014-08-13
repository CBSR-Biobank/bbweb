(function(requirejs) {
  'use strict';

  // -- RequireJS config --
  requirejs.config({
    // Packages = top-level folders; loads a contained file named 'main.js'
    packages: ['common', 'home', 'users', 'admin', 'dashboard'],
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
      'underscore': {
        exports: '_'
      },
      'angular-route': ['angular'],
      'angular-cookies': ['angular'],
      'bootstrap': ['jquery'],
      'angular-ui-router': ['angular'],
      'ui-bootstrap-tpls': ['angular', 'bootstrap'],
      'ng-table': ['angular']
    },
    paths: {
      'requirejs': ['../lib/requirejs/require'],
      'jquery': ['../lib/jquery/jquery'],
      'angular': ['../lib/angularjs/angular'],
      'underscore': '../lib/underscorejs/underscore',
      'angular-route': ['../lib/angularjs/angular-route'],
      'angular-cookies': ['../lib/angularjs/angular-cookies'],
      'angular-ui-router': ['../lib/angular-ui-router/angular-ui-router'],
      'bootstrap': ['../lib/bootstrap/js/bootstrap'],
      'ui-bootstrap-tpls': ['../lib/angular-ui-bootstrap/ui-bootstrap-tpls'],
      'ng-table': ['../lib/ng-table/ng-table'],
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
    'angular-ui-router',
    'ui-bootstrap-tpls',
    'ng-table',
    'jquery',
    'bootstrap',
    './app'
  ], function(angular) {
    angular.bootstrap(document, ['app']);
  }
         );
})(requirejs);
