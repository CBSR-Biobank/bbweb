/**
 * Study administration controllers.
 */
define(['angular', 'common'], function(angular, common) {
  'use strict';

  var mod = angular.module('admin.studies.ceventTypes.controllers', ['studies.services']);

  mod.controller('ceventAnnotationTypeAddCtrl', [
    '$scope', 'ceventAnnotTypeEditService', 'study', 'annotType',
    function ($scope, ceventAnnotTypeEditService, study, annotType) {
      $scope.title =  "Add Annotation Type";
      $scope.study = study;
      $scope.annotType = annotType;
      ceventAnnotTypeEditService.edit($scope);
    }]);

  mod.controller('ceventAnnotationTypeUpdateCtrl', [
    '$scope', 'ceventAnnotTypeEditService', 'study', 'annotType',
    function ($scope, ceventAnnotTypeEditService, study, annotType) {
      $scope.title =  "Update Annotation Type";
      $scope.study = study;
      $scope.annotType = annotType;
      ceventAnnotTypeEditService.edit($scope);
    }]);

  mod.controller('CeventTypeAddCtrl', [
    '$scope', 'ceventTypeEditService', 'study', 'ceventType', 'annotTypes', 'specimenGroups',
    function ($scope, ceventTypeEditService, study, ceventType, annotTypes, specimenGroups) {
      $scope.title =  "Add Collection Event Type";
      $scope.study = study;
      $scope.ceventType = ceventType;
      $scope.annotTypes = annotTypes;
      $scope.specimenGroups = specimenGroups;
      ceventTypeEditService.edit($scope);
    }]);

  mod.controller('CeventTypeUpdateCtrl', [
    '$scope', 'ceventTypeEditService', 'study', 'ceventType', 'annotTypes', 'specimenGroups',
    function ($scope, ceventTypeEditService, study, ceventType, annotTypes, specimenGroups) {
      $scope.title =  "Update Collection Event Type";
      $scope.study = study;
      $scope.ceventType = ceventType;
      $scope.annotTypes = annotTypes;
      $scope.specimenGroups = specimenGroups;
      ceventTypeEditService.edit($scope);
    }]);

  return mod;
});
