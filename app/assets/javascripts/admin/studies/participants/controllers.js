/**
 * Study administration controllers.
 */
define(['angular', 'common'], function(angular, common) {
  'use strict';

  var mod = angular.module('admin.studies.participants.controllers', [
    'studies.services', 'admin.studies.helpers']);

  mod.controller('participantAnnotationTypeAddCtrl', [
    '$scope', 'paricipantAnnotTypeEditService', 'study', 'annotType',
    function ($scope, paricipantAnnotTypeEditService, study, annotType) {
      $scope.title =  "Add Annotation Type";
      $scope.study = study;
      $scope.annotType = annotType;
      paricipantAnnotTypeEditService.edit($scope);
    }]);

  mod.controller('participantAnnotationTypeUpdateCtrl', [
    '$scope', 'paricipantAnnotTypeEditService', 'study', 'annotType',
    function ($scope, paricipantAnnotTypeEditService, study, annotType) {
      $scope.title =  "Update Annotation Type";
      $scope.study = study;
      $scope.annotType = annotType;
      paricipantAnnotTypeEditService.edit($scope);
    }]);

  return mod;
});
