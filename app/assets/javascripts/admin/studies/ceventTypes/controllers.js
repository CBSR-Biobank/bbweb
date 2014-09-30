/**
 * Study administration controllers.
 */
define(['angular'], function(angular) {
  'use strict';

  var mod = angular.module('admin.studies.ceventTypes.controllers', ['studies.services']);

  mod.controller('CeventTypeAddCtrl', [
    '$scope', 'ceventTypeEditService', 'study', 'ceventType', 'annotTypes', 'specimenGroups',
    function ($scope, ceventTypeEditService, study, ceventType, annotTypes, specimenGroups) {
      $scope.title =  'Add Collection Event Type';
      $scope.study = study;
      $scope.ceventType = ceventType;
      $scope.annotTypes = annotTypes;
      $scope.specimenGroups = specimenGroups;
      ceventTypeEditService.edit($scope);
    }
  ]);

  mod.controller('CeventTypeUpdateCtrl', [
    '$scope', 'ceventTypeEditService', 'study', 'ceventType', 'annotTypes', 'specimenGroups',
    function ($scope, ceventTypeEditService, study, ceventType, annotTypes, specimenGroups) {
      $scope.title =  'Update Collection Event Type';
      $scope.study = study;
      $scope.ceventType = ceventType;
      $scope.annotTypes = annotTypes;
      $scope.specimenGroups = specimenGroups;
      ceventTypeEditService.edit($scope);
    }
  ]);

  return mod;
});
