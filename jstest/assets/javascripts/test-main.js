var allTestFiles = [];
var TEST_REGEXP = /(spec|test)\.js$/i;

var pathToModule = function(path) {
  return path.replace(/^\/base\//, '').replace(/\.js$/, '');
};

Object.keys(window.__karma__.files).forEach(function(file) {
  if (TEST_REGEXP.test(file)) {
    // Normalize paths to RequireJS module names.
    var path = '../../../' + pathToModule(file);
    //console.log(file, path);
    allTestFiles.push(path);
  }
});

require.config({
  // Karma serves files under /base, which is the basePath from your config file
  baseUrl: '/base/app/assets/javascripts',

  shim: {
    'angular' : {
      'exports' : 'angular'
    },
    'angularMocks': {
      deps: ['angular'],
      exports: 'angular.mock'
    },
    'angular-ui-router': ['angular'],
    'biobankApp': {
      deps: ['angular'],
      exports: 'biobankApp'
    },
    'angular-cookies':   ['angular'],
    'angular-ui-router': ['angular'],
    'ui-bootstrap-tpls': ['angular'],
    'toastr':            ['jquery'],
    'angular-sanitize':  ['angular'],
    'ngTable':           ['angular']
  },

  paths: {
    'jquery':            '../../../target/web/web-modules/main/webjars/lib/jquery/jquery',
    'angular':           '../../../target/web/web-modules/main/webjars/lib/angularjs/angular',
    'angularMocks':      '../../../target/web/web-modules/main/webjars/lib/angularjs/angular-mocks',
    'underscore':        '../../../target/web/web-modules/main/webjars/lib/underscorejs/underscore',
    'toastr':            '../../../target/web/web-modules/main/webjars/lib/toastr/toastr',
    'ngTable':           '../../../target/web/web-modules/main/webjars/lib/ng-table/ng-table',
    'biobankApp':        'app'
  },

  // dynamically load all test files
  deps: allTestFiles,

  // we have to kickoff jasmine, as it is asynchronous
  callback: window.__karma__.start
});

requirejs.onError = function (err) {
  console.log(err.requireType);
  if (err.requireType === 'timeout') {
    console.log('modules: ' + err.requireModules);
  }

  throw err;
};
