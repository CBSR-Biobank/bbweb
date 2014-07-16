/**
 * Study administration controllers.
 */
define(['angular', 'common'], function(angular, common) {
  'use strict';

  var mod = angular.module('admin.studies.specimenGroups.controllers', ['studies.services']);

  mod.controller('SpecimenGroupAddCtrl', [
    '$scope', 'specimenGroupEditService', 'study', 'specimenGroup',
    function ($scope, specimenGroupEditService, study, specimenGroup) {
      $scope.title =  "Add Specimen Group";
      $scope.study = study;
      $scope.specimenGroup = specimenGroup;
      specimenGroupEditService.edit($scope);
    }]);

  mod.controller('SpecimenGroupUpdateCtrl', [
    '$scope', 'specimenGroupEditService', 'study', 'specimenGroup',
    function ($scope, specimenGroupEditService, study, specimenGroup) {
      $scope.title =  "Update Specimen Group";
      $scope.study = study;
      $scope.specimenGroup = specimenGroup;
      specimenGroupEditService.edit($scope);
    }]);

  return mod;
});
