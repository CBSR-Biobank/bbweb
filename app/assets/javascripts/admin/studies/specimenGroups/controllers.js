/**
 * Study administration controllers.
 */
define(['angular', 'common'], function(angular, common) {
  'use strict';

  var mod = angular.module('admin.studies.specimenGroups.controllers', ['studies.services']);

  /**
   * Common code to add or edit an specimen Group.
   */
  function specimenGroupEditCommon(
    $scope,
    $state,
    $stateParams,
    modalService,
    StudyService,
    SpecimenGroupService) {

    StudyService.anatomicalSourceTypes().then(function(response) {
      $scope.anatomicalSourceTypes = response.data.sort();
    });

    StudyService.preservTypes().then(function(response) {
      $scope.preservTypes = response.data.sort();
    });

    StudyService.preservTempTypes().then(function(response) {
      $scope.preservTempTypes = response.data.sort();
    });

    StudyService.specimenTypes().then(function(response) {
      $scope.specimenTypes = response.data.sort();
    });


    $scope.submit = function(specimenGroup) {
      SpecimenGroupService.addOrUpdate(specimenGroup)
        .success(function() {
          $state.go('admin.studies.study.specimens', { studyId: $scope.study.id });
        })
        .error(function(error) {
          specimenGroupSaveError(
            $scope, modalService, specimenGroup, error,
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
              $state.go('admin.studies.study.specimens');
            }
          );
        });
    };

    $scope.cancel = function() {
      $state.go('admin.studies.study.participants', { studyId: $scope.study.id });
    };
  }

  function specimenGroupSaveError($scope, modalService, specimenGroup, error, onOk, onCancel) {
    var modalOptions = {
      closeButtonText: 'Cancel',
      actionButtonText: 'OK'
    };

    if (error.message.indexOf("expected version doesn't match current version") > -1) {
      /* concurrent change error */
      modalOptions.headerText = 'Modified by another user';
      modalOptions.bodyText = 'Another user already made changes to this Apecimen Group. Press OK to make ' +
        'your changes again, or Cancel to dismiss your changes.';
    } else {
      /* some other error */
      modalOptions.headerText = specimenGroup.id ?  'Cannot update Specimen Group' : 'Cannot add Specimen Group';
      modalOptions.bodyText = 'Error: ' + error.message;
    }

    modalService.showModal({}, modalOptions).then(function (result) {
      onOk();
    }, function () {
      onCancel();
    });
  }

  mod.controller('SpecimenGroupAddCtrl', [
    '$scope', '$state', '$stateParams', 'modalService', 'StudyService', 'SpecimenGroupService', 'study', 'specimenGroup',
    function ($scope, $state, $stateParams, modalService, StudyService, SpecimenGroupService, study, specimenGroup) {
      $scope.title =  "Add Specimen Group";
      $scope.study = study;
      $scope.specimenGroup = specimenGroup;
      specimenGroupEditCommon($scope, $state, $stateParams, modalService, StudyService, SpecimenGroupService);
    }]);

  mod.controller('SpecimenGroupUpdateCtrl', [
    '$scope', '$state', '$stateParams', 'modalService', 'StudyService', 'SpecimenGroupService', 'study', 'specimenGroup',
    function ($scope, $state, $stateParams, modalService, StudyService, SpecimenGroupService, study, specimenGroup) {
      $scope.title =  "Update Specimen Group";
      $scope.study = study;
      $scope.specimenGroup = specimenGroup;
      specimenGroupEditCommon($scope, $state, $stateParams, modalService, StudyService, SpecimenGroupService);
    }]);

  return mod;
});
