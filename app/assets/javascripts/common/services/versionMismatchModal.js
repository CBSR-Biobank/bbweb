/** Common helpers */
define(['angular'], function(angular) {
  'use strict';

  var mod = angular.module('common.versionMismatchModal', []);
  mod.service('versionMismatchModal', ['$scope', function($scope, message, newLocation) {
    $scope.title = "Changed by another user";
    $scope.message = message;

    $scope.ok = function () {
      $location.path(newLocation);
    };
  }]);
  return mod;
});
