/**
 * Study administration controllers.
 */
define(['angular'], function(angular) {
  'use strict';

  var mod = angular.module('admin.studies.processing.controllers', [
    'studies.services', 'admin.studies.helpers'
  ]);


  /** Common class fro editing a Processing Type
   */
  var ProcessingTypeEditCtrl = function(
    $scope,
    $state,
    $stateParams,
    stateHelper,
    modalService,
    ProcessingTypeService,
    title,
    study,
    processingType) {
    $scope.title =  title;
    $scope.study = study;
    $scope.processingType = processingType;

    var saveError = function ($scope, processingType, error) {
      var modalDefaults = {};
      var modalOptions = {
        closeButtonText: 'Cancel',
        actionButtonText: 'OK'
      };

      if (error.message.indexOf('expected version doesn\'t match current version') > -1) {
        /* concurrent change error */
        modalDefaults.templateUrl = '/assets/javascripts/common/modalConcurrencyError.html';
        modalOptions.domainType = 'processing type';
      } else {
        /* some other error */
        modalOptions.headerText =
          'Cannot ' +  (processingType.id ?  'update' : 'add ') + ' Processing Type';
        modalOptions.bodyText = 'Error: ' + error.message;
      }

      modalService.showModal(modalDefaults, modalOptions).then(
        function () {
          stateHelper.reloadAndReinit();
        },
        function () {
          $state.go('admin.studies.study.processing');
        });
    };

    $scope.form = {
      submit: function(processingType) {
        ProcessingTypeService.addOrUpdate(processingType).then(
          function() {
            $state.transitionTo(
              'admin.studies.study.processing',
              $stateParams,
              { reload: true, inherit: false, notify: true });
          },
          function(error) {
            saveError($scope, processingType, error);
          });
      },
      cancel: function() {
        $state.go('admin.studies.study.processing', { studyId: $scope.study.id });
      }
    };
  };

  /**
   * Add Processing Type
   */
  mod.controller('ProcessingTypeAddCtrl', [
    '$scope',
    '$state',
    '$stateParams',
    'stateHelper',
    'modalService',
    'ProcessingTypeService',
    'study',
    'processingType',
    function ($scope,
              $state,
              $stateParams,
              stateHelper,
              modalService,
              ProcessingTypeService,
              study,
              processingType) {
      angular.extend(this, new ProcessingTypeEditCtrl(
        $scope, $state, $stateParams, stateHelper, modalService, ProcessingTypeService, 'Add Processing Type',
        study, processingType));
    }
  ]);

  /**
   * Update Processing Type
   */
  mod.controller('ProcessingTypeUpdateCtrl', [
    '$scope',
    '$state',
    '$stateParams',
    'stateHelper',
    'modalService',
    'ProcessingTypeService',
    'study',
    'processingType',
    function ($scope,
              $state,
              $stateParams,
              stateHelper,
              modalService,
              ProcessingTypeService,
              study,
              processingType) {
      angular.extend(this, new ProcessingTypeEditCtrl(
        $scope, $state, $stateParams, stateHelper, modalService, ProcessingTypeService, 'Update Processing Type',
        study, processingType));
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
