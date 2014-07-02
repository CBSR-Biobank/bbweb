/**
 * Home controllers.
 */
define(['angular'], function(angular) {
  'use strict';

    var mod = angular.module('home.controllers', ['user.services']);

  /** Controls the index page */
  mod.controller('HomeCtrl', ['$scope', '$rootScope', function($scope, $rootScope) {
    $rootScope.pageTitle = 'Biobank';
  }]);

  /** Controls the header */
  mod.controller('HeaderCtrl', ['$scope', '$state', 'userService', function($scope, $state, userService) {
    // Wrap the current user from the service in a watch expression
    $scope.$watch(function() {
      var user = userService.getUser();
      return user;
    }, function(user) {
      $scope.user = user;
    }, true);

    $scope.logout = function() {
      userService.logout();
      $scope.user = undefined;
      $state.go('home');
    };
  }]);

  /** Controls the footer */
  mod.controller('FooterCtrl', function(/*$scope*/) {
  });

});
