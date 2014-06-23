/**
 * Configure routes of user module.
 */
define(['angular', './controllers', 'common'], function(angular, controllers) {
  'use strict';

  var mod = angular.module('user.states', ['ui.router', 'user.services', 'biobank.common']);

  mod.config(function($stateProvider) {
    $stateProvider
      .state('user', {
        url: '/login',
        templateUrl: '/assets/javascripts/user/login.html',
        controller: controllers.LoginCtrl
      });
    //.when('/users', {templateUrl:'/assets/javascripts/user/users.html', controller:controllers.UserCtrl})
    //.when('/users/:id', {templateUrl:'/assets/javascripts/user/editUser.html', controller:controllers.UserCtrl});
  });

  return mod;
});
