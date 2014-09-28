/** Specimen Group helpers */
define(['angular'], function(angular) {
  'use strict';

  var mod = angular.module('admin.studies.specimenGroups.helpers', []);

  /**
   * Displays a study annotation type in a modal. The information is displayed in an ng-table.
   *
   */
  mod.service('specimenGroupModalService', [
    'modelObjModalService', 'addTimeStampsService',
    function (modelObjModalService, addTimeStampsService) {
      this.show = function (specimenGroup) {
        var title = 'Specimen Group';
        var data = [];
        data.push({name: 'Name:', value: specimenGroup.name});
        data.push({name: 'Units:', value: specimenGroup.units});
        data.push({name: 'Anatomical Source:', value: specimenGroup.anatomicalSourceType});
        data.push({name: 'Preservation Type:', value: specimenGroup.preservationType});
        data.push({name: 'Preservation Temperature:', value: specimenGroup.preservationTemperatureType});
        data.push({name: 'Specimen Type:', value: specimenGroup.specimenType});
        data.push({name: 'Description:', value: specimenGroup.description});
        data = data.concat(addTimeStampsService.get(specimenGroup));
        modelObjModalService.show(title, data);
      };
    }
  ]);

  /**
   * Common code to add or edit an specimen Group.
   */
  mod.service('specimenGroupEditService', [
    '$state', '$stateParams', 'stateHelper', 'modalService', 'SpecimenGroupService',
    function($state, $stateParams, stateHelper, modalService, SpecimenGroupService) {

      var saveError = function ($scope, specimenGroup, error) {
        var modalOptions = {
          closeButtonText: 'Cancel',
          actionButtonText: 'OK'
        };

        if (error.message.indexOf('expected version doesn\'t match current version') > -1) {
          /* concurrent change error */
          modalOptions.headerText = 'Modified by another user';
          modalOptions.bodyText = 'Another user already made changes to this Specimen Group. Press OK to make ' +
            'your changes again, or Cancel to dismiss your changes.';
        } else {
          /* some other error */
          modalOptions.headerText =
            'Cannot ' + (specimenGroup.id ?  'update' : 'add ') +' Specimen Group';
          modalOptions.bodyText = 'Error: ' + error.message;
        }

        modalService.showModal({}, modalOptions).then(
          function () {
          stateHelper.reloadAndReinit();
        }, function () {
          $state.go('admin.studies.study.specimens');
        });
      };

      return {
        edit: function($scope) {
          SpecimenGroupService.specimenGroupValueTypes().then(
            function(valueTypes) {
              $scope.anatomicalSourceTypes = valueTypes.anatomicalSourceType.sort();
              $scope.preservTypes          = valueTypes.preservationType.sort();
              $scope.preservTempTypes      = valueTypes.preservationTemperatureType.sort();
              $scope.specimenTypes         = valueTypes.specimenType.sort();
            });

          $scope.submit = function(specimenGroup) {
            SpecimenGroupService.addOrUpdate(specimenGroup).then(
              function() {
                $state.transitionTo(
                  'admin.studies.study.specimens',
                  $stateParams,
                  { reload: true, inherit: false, notify: true });
              },
              function(errMessage) {
                saveError($scope, specimenGroup, errMessage);
              });
          };

          $scope.cancel = function() {
            $state.go('admin.studies.study.specimens', { studyId: $scope.study.id });
          };
        }
      };
    }
  ]);


  /**
   * Removes a specimen group.
   */
  mod.service('specimenGroupRemoveService', [
    '$state', 'stateHelper', 'studyRemoveModalService', 'SpecimenGroupService',
    function ($state, stateHelper, studyRemoveModalService, SpecimenGroupService) {
      return {
        remove: function(specimenGroup) {
          studyRemoveModalService.remove(
            'Remove Specimen Group',
            'Are you sure you want to remove specimen group ' + specimenGroup.name + '?',
            function () {
              SpecimenGroupService.remove(specimenGroup).then(
                function() {
                  stateHelper.reloadAndReinit();
                },
                function(error) {
                  var bodyText = 'Specimen group ' + specimenGroup.name + ' cannot be removed: ' + error.message;
                  studyRemoveModalService.orError(
                    bodyText,
                    'admin.studies.study.specimens',
                    'admin.studies.study.specimens');
                });
            },
            function() {
              $state.go('admin.studies.study.specimens');
            });
        }
      };
    }
  ]);

  return mod;
});
