/**
 * The app module, as both AngularJS as well as RequireJS module.
 *
 * Splitting an app in several Angular modules serves no real purpose in Angular 1.0/1.1.
 * (Hopefully this will change in the near future.)
 *
 * Splitting it into several RequireJS modules allows async loading. We cannot take full advantage
 * of RequireJS and lazy-load stuff 'because the angular modules have their own dependency system.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function(require) {
  'use strict';

  var angular    = require('angular'),
      toastr     = require('toastr'),
      admin      = require('admin'),
      centres    = require('centres'),
      common     = require('common'),
      collection = require('collection'),
      dashboard  = require('dashboard'),
      domain     = require('domain'),
      home       = require('home'),
      studies    = require('studies'),
      users      = require('users'),
      app;

  require('jquery');
  require('bootstrap');
  require('ui-bootstrap');
  require('angular-ui-router');
  require('angular-sanitize');
  require('angular-cookies');
  require('angular-messages');
  require('smart-table');
  require('angular-utils-ui-breadcrumbs');
  require('bootstrap-ui-datetime-picker');

  // We must already declare most dependencies here (except for common), or the submodules' routes
  // will not be resolved
  app = angular.module('biobankApp', [
    'ui.bootstrap',
    'ui.router',
    'ngSanitize',
    'ngCookies',
    'ngMessages',
    'smart-table',
    'angularUtils.directives.uiBreadcrumbs',
    'ui.bootstrap.datetimepicker',
    admin.name,
    centres.name,
    common.name,
    collection.name,
    dashboard.name,
    domain.name,
    home.name,
    studies.name,
    users.name
  ]);

  // For debugging
  //
  app.run(debugFunc);
  app.constant('bbwebConfig',
               {
                 dateFormat:       'YYYY-MM-DD',
                 dateTimeFormat:   'YYYY-MM-DD HH:mm',
                 datepickerFormat: 'yyyy-MM-dd HH:mm'
               });
  app.config(exceptionConfig);
  app.config(loggingConfig);
  app.config(httpInterceptorConfig);

  extendExceptionHandler.$inject = ['$delegate'];

  //--

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

  exceptionConfig.$inject = ['$provide'];

  function exceptionConfig($provide) {
    $provide.decorator('$exceptionHandler', extendExceptionHandler);
  }

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

  loggingConfig.$inject = ['$logProvider'];

  function loggingConfig($logProvider) {
    $logProvider.debugEnabled(true);
  }

  httpInterceptorConfig.$inject = ['$httpProvider'];

  function httpInterceptorConfig($httpProvider) {

    $httpProvider.interceptors.push(httpInterceptor);

    //---

    httpInterceptor.$inject = ['$q', '$timeout', '$injector'];

    function httpInterceptor($q, $timeout, $injector) {
      return { 'responseError': responseError };

      //--

      function responseError(rejection) {
        if (rejection.status === 401) {
          $injector.get('usersService').sessionTimeout();
          $injector.get('$state').go('home.users.login', {}, { reload: true });
          $injector.get('$log').info('your session has timed out');
        }
        return $q.reject(rejection);
      }
    }
  }

  return app;
});
