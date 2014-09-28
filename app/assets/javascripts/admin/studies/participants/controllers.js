/**
 * Study administration controllers.
 */
define(['angular'], function(angular) {
  'use strict';

  var mod = angular.module('admin.studies.participants.controllers', [
    'studies.services', 'admin.studies.helpers'
  ]);

  mod.controller('participantAnnotationTypeAddCtrl', [
    '$scope', 'participantAnnotTypeEditService', 'study', 'annotType',
    function ($scope, participantAnnotTypeEditService, study, annotType) {
      $scope.title =  'Add Annotation Type';
      $scope.study = study;
      $scope.annotType = annotType;
      participantAnnotTypeEditService.edit($scope);
    }
  ]);

  mod.controller('participantAnnotationTypeUpdateCtrl', [
    '$scope', 'participantAnnotTypeEditService', 'study', 'annotType',
    function ($scope, participantAnnotTypeEditService, study, annotType) {
      $scope.title =  'Update Annotation Type';
      $scope.study = study;
      $scope.annotType = annotType;
      participantAnnotTypeEditService.edit($scope);
    }
  ]);

  return mod;
});
