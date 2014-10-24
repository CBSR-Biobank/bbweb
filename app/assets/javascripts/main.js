(function(requirejs) {
  'use strict';

  // -- RequireJS config --
  requirejs.config({
    // Packages = top-level folders; loads a contained file named 'main.js'
    packages: [],
    shim: {
      'angular': {
        deps: ['jquery'],
        exports: 'angular'
      },
      'underscore': {
        exports: '_'
      },
      'bootstrap':         ['jquery'],
      'toastr':            ['angular', 'jquery']
    },
    paths: {
      'requirejs':         '../lib/requirejs/require',
      'jquery':            '../lib/jquery/jquery',
      'angular':           '../lib/angularjs/angular',
      'underscore':        '../lib/underscorejs/underscore',
      'angular-cookies':   '../lib/angularjs/angular-cookies',
      'angular-ui-router': '../lib/angular-ui-router/angular-ui-router',
      'bootstrap':         '../lib/bootstrap/js/bootstrap',
      'ui-bootstrap':      '../lib/angular-ui-bootstrap/ui-bootstrap-tpls',
      'ngTable':           '../lib/ng-table/ng-table',
      'toastr':            '../lib/toastr/toastr',
      'angular-sanitize':  '../lib/angular-sanitize/angular-sanitize'
    }
  });

  requirejs.onError = function(err) {
    console.log(err);
    if (err.requireType === 'timeout') {
      console.log('modules: ' + err.requireModules);
    }

    throw err;
  };

  // Load the app. This is kept minimal so it doesn't need much updating.
  require([
    'angular',
    'jquery',
    'bootstrap',
    './app'
  ], function(angular) {
    angular.bootstrap(document, ['biobankApp']);
  });
})(requirejs);
