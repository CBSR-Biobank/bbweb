/**
 * Study administration controllers.
 */
define(['angular', 'common'], function(angular, common) {
  'use strict';

  var mod = angular.module('admin.studies.annotTypes.controllers', ['studies.services']);

  /**
   * Common code to add or edit an annotation type.
   */
  function studyAnnotationTypeEditCommon(
    $scope,
    $state,
    $stateParams,
    modalService,
    StudyService,
    ParticipantAnnotTypeService) {

    $scope.hasRequiredField = (typeof $scope.annotType.required !== 'undefined');

    StudyService.valueTypes().then(function(response) {
      $scope.valueTypes = response.data.sort();
    });

    $scope.optionAdd = function() {
      var newOptionId = $scope.annotType.options.length;
      $scope.annotType.options.push("");
    };

    $scope.removeOption = function(option) {
      if ($scope.annotType.options.length <= 1) {
        throw new Error("invalid length for options");
      }

      var index = $scope.annotType.options.indexOf(option);
      if (index > -1) {
        $scope.annotType.options.splice(index, 1);
      }
    };

    $scope.removeButtonDisabled = function() {
      return $scope.annotType.options.length <= 1;
    };

    $scope.submit = function(annotType) {
      ParticipantAnnotTypeService.addOrUpdate(annotType)
        .success(function() {
          $state.go('admin.studies.study.participants', { studyId: $scope.study.id });
        })
        .error(function(error) {
          annotTypeSaveError(
            $scope, modalService, annotType, error,
            function() {
              // could use $state.reload() here but it does not re-initialize the
              // controller
              $state.transitionTo($state.current, $stateParams, {
                reload: true,
                inherit: false,
                notify: true
              });
            },
            function() {
              $state.go('admin.studies.study.participants');
            }
          );
        });
    };

    $scope.cancel = function() {
      $state.go('admin.studies.study.participants', { studyId: $scope.study.id });
    };
  }

  function annotTypeSaveError($scope, modalService, annotType, error, onOk, onCancel) {
    var modalOptions = {
      closeButtonText: 'Cancel',
      actionButtonText: 'OK'
    };

    if (error.message.indexOf("expected version doesn't match current version") > -1) {
      /* concurrent change error */
      modalOptions.headerText = 'Modified by another user';
      modalOptions.bodyText = 'Another user already made changes to this annotation type. Press OK to make ' +
        'your changes again, or Cancel to dismiss your changes.';
    } else {
      /* some other error */
      modalOptions.headerText = annotType.id ?  'Cannot update annotation type' : 'Cannot add annotation type';
      modalOptions.bodyText = 'Error: ' + error.message;
    }

    modalService.showModal({}, modalOptions).then(function (result) {
      onOk();
    }, function () {
      onCancel();
    });
  }

  mod.controller('StudyAnnotationTypeAddCtrl', [
    '$scope', '$state', '$stateParams', 'modalService', 'StudyService', 'ParticipantAnnotTypeService', 'study', 'annotType',
    function ($scope, $state, $stateParams, modalService, StudyService, ParticipantAnnotTypeService, study, annotType) {
      $scope.title =  "Add Annotation Type";
      $scope.study = study;
      $scope.annotType = annotType;
      studyAnnotationTypeEditCommon($scope, $state, $stateParams, modalService, StudyService, ParticipantAnnotTypeService);
    }]);

  mod.controller('StudyAnnotationTypeUpdateCtrl', [
    '$scope', '$state', '$stateParams', 'modalService', 'StudyService', 'ParticipantAnnotTypeService', 'study', 'annotType',
    function ($scope, $state, $stateParams, modalService, StudyService, ParticipantAnnotTypeService, study, annotType) {
      $scope.title =  "Update Participant Annotation Type";
      $scope.study = study;
      $scope.annotType = annotType;
      studyAnnotationTypeEditCommon($scope, $state, $stateParams, modalService, StudyService, ParticipantAnnotTypeService);
    }]);

  return mod;
});
