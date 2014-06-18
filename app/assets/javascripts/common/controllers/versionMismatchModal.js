/** Common controllers */
define(['angular'], function(angular) {
  'use strict';

  var mod = angular.module('common.versionMismatchModal', []);
  mod.controller('versionMismatchModal', [
    '$scope',
    '$modalInstance',
    'title',
    'message',
    function($scope, $modalInstance, title, message) {
      $scope.title = title;
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
