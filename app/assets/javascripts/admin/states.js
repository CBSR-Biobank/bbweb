/**
 * Configure routes for the administraiotn module.
 */
define(['angular', './controllers', 'common'], function(angular, controllers) {
  'use strict';

  var mod = angular.module('admin.states', ['ui.router', 'user.services', 'biobank.common']);

  mod.config(function($stateProvider, userResolve) {
    $stateProvider
      .state('admin', {
        abstract: true,
        url: '/admin',
        resolve: userResolve,
        data: {
          breadcrumbProxy: 'admin.details'
        }
      })
      .state('admin.details', {
        url: '',
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/admin/adminDetails.html'
          }
        },
        resolve: userResolve,
        data: {
          displayName: 'Administration'
        }
      });
    //.when('/users', {templateUrl:'/assets/javascripts/user/users.html', controller:controllers.UserCtrl})
    //.when('/users/:id', {templateUrl:'/assets/javascripts/user/editUser.html', controller:controllers.UserCtrl});
  });

  return mod;
});
