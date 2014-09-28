/**
 * Study administration controllers.
 */
define(['angular'], function(angular) {
  'use strict';

  var mod = angular.module('admin.studies.processing.controllers', [
    'studies.services', 'admin.studies.helpers'
  ]);

  /**
   * Add Processing Type
   */
  mod.controller('ProcessingTypeAddCtrl', [
    '$scope', 'processingTypeEditService', 'study', 'processingType',
    function ($scope, processingTypeEditService, study, processingType) {
      $scope.title =  'Add Processing Type';
      $scope.study = study;
      $scope.processingType = processingType;
      processingTypeEditService.edit($scope);
    }
  ]);

  /**
   * Update Processing Type
   */
  mod.controller('ProcessingTypeUpdateCtrl', [
    '$scope', 'processingTypeEditService', 'study', 'processingType',
    function ($scope, processingTypeEditService, study, processingType) {
      $scope.title =  'Update Processing Type';
      $scope.study = study;
      $scope.processingType = processingType;
      processingTypeEditService.edit($scope);
    }
  ]);

  /**
   * Add Specimen Link Annotation Type
   */
  mod.controller('spcLinkAnnotationTypeAddCtrl', [
    '$scope', 'spcLinkAnnotTypeEditService', 'study', 'annotType',
    function ($scope, spcLinkAnnotTypeEditService, study, annotType) {
      $scope.title =  'Add Annotation Type';
      $scope.study = study;
      $scope.annotType = annotType;
      spcLinkAnnotTypeEditService.edit($scope);
    }
  ]);

  /**
   * Update Specimen Link Annotation Type
   */
  mod.controller('spcLinkAnnotationTypeUpdateCtrl', [
    '$scope', 'spcLinkAnnotTypeEditService', 'study', 'annotType',
    function ($scope, spcLinkAnnotTypeEditService, study, annotType) {
      $scope.title =  'Update Annotation Type';
      $scope.study = study;
      $scope.annotType = annotType;
      spcLinkAnnotTypeEditService.edit($scope);
    }
  ]);

  /**
   * Add Specimen Link Type
   */
  mod.controller('SpcLinkTypeAddCtrl', [
    '$scope',
    'spcLinkTypeEditService',
    'study',
    'spcLinkType',
    'dtoProcessing',
    function ($scope,
              spcLinkTypeEditService,
              study,
              spcLinkType,
              dtoProcessing) {
      $scope.title           =  'Add Spcecimen Link Type';
      $scope.study           = study;
      $scope.spcLinkType     = spcLinkType;
      $scope.processingTypes = dtoProcessing.processingTypes;
      $scope.annotTypes      = dtoProcessing.specimenLinkAnnotationTypes;
      $scope.specimenGroups  = dtoProcessing.specimenGroups;
      spcLinkTypeEditService.edit($scope);
    }
  ]);

  /**
   * Update Specimen Link Type
   */
  mod.controller('SpcLinkTypeUpdateCtrl', [
    '$scope',
    'spcLinkTypeEditService',
    'study',
    'spcLinkType',
    'dtoProcessing',
    function ($scope,
              spcLinkTypeEditService,
              study,
              spcLinkType,
              dtoProcessing) {
      $scope.title           = 'Update Spcecimen Link Type';
      $scope.study           = study;
      $scope.spcLinkType     = spcLinkType;
      $scope.processingTypes = dtoProcessing.processingTypes;
      $scope.annotTypes      = dtoProcessing.specimenLinkAnnotationTypes;
      $scope.specimenGroups  = dtoProcessing.specimenGroups;
      spcLinkTypeEditService.edit($scope);
    }
  ]);

  return mod;
});
