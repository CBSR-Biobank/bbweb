/**
 * The app module, as both AngularJS as well as RequireJS module.
 * Splitting an app in several Angular modules serves no real purpose in Angular 1.0/1.1.
 * (Hopefully this will change in the near future.)
 * Splitting it into several RequireJS modules allows async loading. We cannot take full advantage
 * of RequireJS and lazy-load stuff because the angular modules have their own dependency system.
 */
define([
  'angular',
  'toastr',
  'common/index',
  'home/index',
  'centres/index',
  'studies/index',
  'users/index',
  'admin/index',
  'dashboard/index'
], function(angular, toastr) {
  'use strict';

  // We must already declare most dependencies here (except for common), or the submodules' routes
  // will not be resolved
  var app = angular.module('app', [
    'biobank.admin',
    'biobank.centres',
    'biobank.dashboard',
    'biobank.home',
    'biobank.studies',
    'biobank.users'
  ]);
  // For debugging
  //
  app.run(debugFunc);

  debugFunc.$inject = ['$rootScope'];

  function debugFunc($rootScope) {
    /*jshint unused: false*/

    // change these to true to have the information displayed in the console
    var debugStateChangeStart   = false;
    var debugStateChangeSuccess = false;
    var debugViewContentLoading = false;
    var debugViewContentLoaded  = false;
    var debugStateNotFound      = false;

    // $rootScope.$state = $state;
    // $rootScope.$stateParams = $stateParams;

    $rootScope.$on('$stateChangeError',function(event, toState, toParams, fromState , fromParams){
      console.log('$stateChangeError - fired when an error occurs during transition.');
      console.log(arguments);
    });

    if (debugStateChangeStart) {
      $rootScope.$on('$stateChangeStart',function(event, toState, toParams, fromState, fromParams){
        console.log('$stateChangeStart to '+ toState.name + ' from ' + fromState.name +
                    ' - fired when the transition begins. toState, toParams : \n', toState, toParams);
      });
    }

    if (debugStateChangeSuccess) {
      $rootScope.$on('$stateChangeSuccess',function(event, toState, toParams, fromState, fromParams){
        console.log('$stateChangeSuccess to '+ toState.name +
                    '- fired once the state transition is complete.');
      });
    }

    if (debugViewContentLoading) {
      $rootScope.$on('$viewContentLoading',function(event, viewConfig){
        // runs on individual scopes, so putting it in 'run' doesn't work.
        console.log('$viewContentLoading - view begins loading - dom not rendered',viewConfig);
      });
    }

    if (debugViewContentLoaded) {
      $rootScope.$on('$viewContentLoaded',function(event){
        console.log('$viewContentLoaded - fired after dom rendered',event);
      });
    }

    if (debugStateNotFound) {
      $rootScope.$on('$stateNotFound',function(event, unfoundState, fromState, fromParams){
        console.log('$stateNotFound '+ unfoundState.to +
                    '  - fired when a state cannot be found by its name.');
        console.log(unfoundState, fromState, fromParams);
      });
    }

    /*jshint unused: true*/
  }

  app.config(exceptionConfig);

  exceptionConfig.$inject = ['$provide'];

  function exceptionConfig($provide) {
    $provide.decorator('$exceptionHandler', extendExceptionHandler);
  }

  extendExceptionHandler.$inject = ['$delegate'];

  function extendExceptionHandler($delegate) {
    return function (exception, cause) {
      $delegate(exception, cause);
      // var errorData = {
      //   exception: exception,
      //   cause: cause
      // };

      /**
       * Could add the error to a service's collection,
       * add errors to $rootScope, log errors to remote web server,
       * or log locally. Or throw hard. It is entirely up to you.
       * throw exception;
       */
      toastr.error(
        exception.message,
        'Exception',
        { positionClass: 'toast-bottom-right' });
    };
  }

  return app;
});
