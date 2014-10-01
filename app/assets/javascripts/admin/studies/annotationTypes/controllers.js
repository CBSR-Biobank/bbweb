define(['angular'], function(angular) {
  'use strict';

  var mod = angular.module('admin.studies.annotationTypes.controllers', [
    'biobank.common', 'admin.studies.helpers', 'studies.services'
  ]);

  mod.controller('StudyAnnotationTypeEditCtrl', StudyAnnotationTypeEditCtrl);

  StudyAnnotationTypeEditCtrl.$inject = [
    '$scope',
    '$state',
    '$stateParams',
    'stateHelper',
    'modalService',
    'StudyAnnotTypeService',
    'study',
    'annotType',
    'returnState',
    'addOrUpdateFn'
  ];

  /** Used for all 3 different study annotation types.
   */
  function StudyAnnotationTypeEditCtrl($scope,
                                       $state,
                                       $stateParams,
                                       stateHelper,
                                       modalService,
                                       StudyAnnotTypeService,
                                       study,
                                       annotType,
                                       returnState,
                                       addOrUpdateFn) {
    var action = (annotType.id) ? 'Update' : 'Add';
    $scope.study = study;
    $scope.annotType = annotType;
    $scope.title =  action + ' Annotation Type';
    $scope.hasRequiredField = (typeof annotType.required !== 'undefined');

    $scope.valueTypes = [];
    StudyAnnotTypeService.valueTypes().then(function(valueTypes) {
      $scope.valueTypes = valueTypes;
    });

    $scope.optionAdd = function() {
      $scope.annotType.options.push('');
    };

    $scope.removeOption = function(option) {
      if ($scope.annotType.options.length <= 1) {
        throw new Error('invalid length for options');
      }

      var index = $scope.annotType.options.indexOf(option);
      if (index > -1) {
        $scope.annotType.options.splice(index, 1);
      }
    };

    $scope.removeButtonDisabled = function() {
      return $scope.annotType.options.length <= 1;
    };

    $scope.updateError = function(error, stateOnCancel) {
      var modalDefaults = {};
      var modalOptions = {
        closeButtonText: 'Cancel',
        actionButtonText: 'OK'
      };

      if (error.message.indexOf('expected version doesn\'t match current version') > -1) {
        /* concurrent change error */
        modalDefaults.templateUrl = '/assets/javascripts/common/modalConcurrencyError.html';
        modalOptions.domainType = 'annotation type';
      } else {
        /* some other error */
        modalOptions.headerText =
          'Cannot ' + $scope.annotType.id ?  'update' : 'add' + ' annotation type';
        modalOptions.bodyText = 'Error: ' + error.message;
      }

      modalService.showModal({}, modalOptions).then(
        function () {
          stateHelper.reloadAndReinit();
        }, function () {
          $state.go(stateOnCancel);
        });
    };

    $scope.submit = function (annotType) {
      addOrUpdateFn(annotType).then(
        function() {
          stateHelper.reloadStateAndReinit(returnState);
        },
        function(error) {
          $scope.updateError(error, returnState);
        });
    };

    $scope.cancel = function () {
      $state.go(returnState);
    };
  }

  function studyAnnotationTypeRemove($state,
                                     stateHelper,
                                     studyRemoveModalService,
                                     removeFn,
                                     annotType,
                                     returnState) {
    studyRemoveModalService.remove(
      'Remove Annotation Type',
      'Are you sure you want to remove annotation type ' + annotType.name + '?').then(
        function () {
          removeFn(annotType).then(
            function() {
              stateHelper.reloadAndReinit();
            },
            function(error) {
              var bodyText = 'Annotation type ' +
                  annotType.name +
                  ' cannot be removed: ' +
                  error.message;
              studyRemoveModalService.orError(bodyText, returnState, returnState);
            });
        },
        function() {
          $state.go(returnState);
        });
  }

  /**
   * Removes a collection event annotation type.
   */
  mod.service('ceventAnnotTypeRemoveService', [
    '$state', 'stateHelper', 'studyRemoveModalService', 'CeventAnnotTypeService',
    function ($state, stateHelper, studyRemoveModalService, CeventAnnotTypeService) {
      return {
        remove: function(annotType) {
          studyAnnotationTypeRemove(
            $state,
            stateHelper,
            studyRemoveModalService,
            CeventAnnotTypeService.remove,
            annotType,
            'admin.studies.study.collection');
        }
      };
    }
  ]);

  /**
   * Removes a participant annotation type.
   */
  mod.service('participantAnnotTypeRemoveService', [
    '$state',
    'stateHelper',
    'ParticipantAnnotTypeService',
    'studyRemoveModalService',
    function participantAnnotTypeRemoveService($state,
                                               stateHelper,
                                               ParticipantAnnotTypeService,
                                               studyRemoveModalService) {
      return {
        remove: function(annotType) {
          studyAnnotationTypeRemove(
            $state,
            stateHelper,
            studyRemoveModalService,
            ParticipantAnnotTypeService.remove,
            annotType,
            'admin.studies.study.participants');
        }
      };
    }
  ]);

  /**
   * Removes a specimen link annotation type.
   */
  mod.service('spcLinkAnnotTypeRemoveService', [
    '$state', 'stateHelper', 'studyRemoveModalService', 'SpcLinkAnnotTypeService',
    function ($state, stateHelper, studyRemoveModalService, SpcLinkAnnotTypeService) {
      return {
        remove: function(annotType) {
          studyAnnotationTypeRemove(
            $state,
            stateHelper,
            studyRemoveModalService,
            SpcLinkAnnotTypeService.remove,
            annotType,
            'admin.studies.study.processing');
        }
      };
    }
  ]);

});
