/** Common controllers */
define(['angular'], function(angular) {
  'use strict';

  var mod = angular.module('common.versionMismatchModal', []);
  mod.controller('versionMismatchModal', [
    '$scope',
    '$modalInstance',
    'message',
    function($scope, $modalInstance, message) {
      $scope.title = "Modified by another user";
      $scope.message = message;

      $scope.ok = function () {
        $modalInstance.close();
      };

      $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
      };
    }]);
  return mod;
});
