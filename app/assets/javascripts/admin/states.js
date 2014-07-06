/**
 * Configure routes for the administraiotn module.
 */
define(['angular', './controllers', 'common'], function(angular, controllers) {
  'use strict';

  var mod = angular.module('admin.states', ['ui.router', 'user.services', 'biobank.common']);

  mod.config([
    '$urlRouterProvider', '$stateProvider', 'userResolve',
    function($urlRouterProvider, $stateProvider, userResolve ) {

    $urlRouterProvider.otherwise('/');

    $stateProvider
      .state('admin', {
        url: '/admin',
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/admin/adminDetails.html'
          }
        },
        resolve: {
          user: userResolve.user
        },
        data: {
          displayName: 'Administration'
        }
      });
    //.when('/users', {templateUrl:'/assets/javascripts/user/users.html', controller:controllers.UserCtrl})
    //.when('/users/:id', {templateUrl:'/assets/javascripts/user/editUser.html', controller:controllers.UserCtrl});
  }]);

  return mod;
});
