/** Common helpers */
define(['angular', 'underscore'], function(angular, _) {
  'use strict';

  var mod = angular.module('admin.studies.ceventTypes.helpers', ['admin.studies.helpers']);

  /**
   * Displays a collection event type in a modal. The information is displayed in an ng-table.
   *
   */
  mod.service('ceventTypeModalService', [
    '$filter', 'modelObjModalService', 'addTimeStampsService',
    function ($filter, modelObjModalService, addTimeStampsService) {
      return {
        show: function (ceventType, specimenGroups, annotTypes) {
          var title = 'Collection Event Type';
          var specimenGroupsById = _.indexBy(specimenGroups, 'id');
          var annotTypesById = _.indexBy(annotTypes, 'id');

          var sgDataStrings = [];
          ceventType.specimenGroupData.forEach(function (sgItem) {
            var specimenGroup = specimenGroupsById[sgItem.specimenGroupId];
            if (!specimenGroup) {
              throw new Error("specimen group not found");
            }
            sgDataStrings.push(specimenGroup.name + ' (' + sgItem.maxCount + ', ' + sgItem.amount +
                               ' ' + specimenGroup.units + ')');
          });

          var atDataStrings = [];
          ceventType.annotationTypeData.forEach(function (atItem) {
            var annotType = annotTypesById[atItem.annotationTypeId];
            if (!annotType) {
              throw new Error("annotation type not found");
            }
            atDataStrings.push(annotType.name + (atItem.required ? ' (Req)' : ' (N/R)'));
          });

          var data = [];
          data.push({name: 'Name:', value: ceventType.name});
          data.push({name: 'Recurring:', value: ceventType.recurring ? 'Yes' : 'No'});
          data.push({name: 'Specimen Groups (Count, Amount):', value: sgDataStrings.join(", ")});
          data.push({name: 'Annotation Types:', value: atDataStrings.join(", ")});
          data.push({name: 'Description:', value: ceventType.description});
          data = data.concat(addTimeStampsService.get(ceventType));
          modelObjModalService.show(title, data);
        }
      };
    }]);

  /**
   * Common code to edit a collection event annotation type.
   *
   */
  mod.service('ceventAnnotTypeEditService', [
    '$state', '$stateParams', 'modalService', 'studyAnnotTypeEditService', 'CeventAnnotTypeService',
    function($state, $stateParams, modalService, studyAnnotTypeEditService, CeventAnnotTypeService) {
      return {
        edit: function ($scope) {

          var onSubmit = function (annotType) {
            CeventAnnotTypeService.addOrUpdate(annotType).then(
              function() {
                $state.transitionTo(
                  'admin.studies.study.collection',
                  $stateParams,
                  { reload: true, inherit: false, notify: true });
              },
              function(error) {
                studyAnnotTypeEditService.onError($scope, error, 'admin.studies.study.collection');
              });
          };

          var onCancel = function () {
            $state.go('admin.studies.study.collection');
          };

          studyAnnotTypeEditService.edit($scope, onSubmit, onCancel);
        }
      };
    }]);

  /**
   * Removes a collection event annotation type.
   */
  mod.service('ceventAnnotTypeRemoveService', [
    '$state', 'stateHelper', 'studyRemoveModalService', 'CeventAnnotTypeService', 'modalService',
    function ($state, stateHelper, studyRemoveModalService, CeventAnnotTypeService, modalService) {
      return {
        remove: function(ceventAnnotType) {
          studyRemoveModalService.remove(
            'Remove Collection Event Annotation Type',
            'Are you sure you want to remove collection event annotation type ' + ceventAnnotType.name + '?',
            function (result) {
              CeventAnnotTypeService.remove(ceventAnnotType).then(
                function() {
                  stateHelper.reloadAndReinit();
                },
                function(error) {
                  var bodyText = 'Collection event annotation type ' + ceventAnnotType.name + ' cannot be removed: ' + error.message;
                  studyRemoveModalService.orError(
                    bodyText,
                    'admin.studies.study.collection',
                    'admin.studies.study.collection');
                });
            },
            function() {
              $state.go('admin.studies.study.collection');
            });
        }
      };
    }]);

  /**
   * Common code to add or edit a collection event type.
   */
  mod.service('ceventTypeEditService', [
    '$state', '$stateParams', '$filter', 'stateHelper', 'modalService', 'CeventTypeService',
    function($state, $stateParams, $filter, stateHelper, modalService, CeventTypeService) {

      /*
       * Called when the submission failed due to an error.
       */
      var saveError = function ($scope, ceventType, error) {
        var modalOptions = {
          closeButtonText: 'Cancel',
          actionButtonText: 'OK'
        };

        if (error.message.indexOf("expected version doesn't match current version") > -1) {
          /* concurrent change error */
          modalOptions.headerText = 'Modified by another user';
          modalOptions.bodyText = 'Another user already made changes to this collection event type. Press OK to make ' +
            'your changes again, or Cancel to dismiss your changes.';
        } else {
          /* some other error */
          modalOptions.headerText =
            'Cannot ' +  (ceventType.id ?  'update' : 'add ') + ' Collection Event';
          modalOptions.bodyText = 'Error: ' + error.message;
        }

        modalService.showModal({}, modalOptions).then(function (result) {
          stateHelper.reloadAndReinit();
        }, function () {
          $state.go('admin.studies.study.collection');
        });
      };

      return {
        edit: function($scope) {
          $scope.form = {
            submit: function(ceventType) {
              CeventTypeService.addOrUpdate(ceventType).then(
                function() {
                  $state.transitionTo(
                    'admin.studies.study.collection',
                    $stateParams,
                    { reload: true, inherit: false, notify: true });
                },
                function(error) {
                  saveError($scope, ceventType, error);
                });
            },
            cancel: function() {
              $state.go('admin.studies.study.collection', { studyId: $scope.study.id });
            },
            addSpecimenGroup: function () {
              $scope.ceventType.specimenGroupData.push({name:'', specimenGroupId:'', maxCount: '', amount: ''});
            },
            removeSpecimenGroupButtonDisabled: function () {
              return $scope.ceventType.specimenGroupData.length <= 1;
            },
            removeSpecimenGroup: function (sgData) {
              if ($scope.ceventType.specimenGroupData.length <= 1) {
                throw new Error("invalid length for specimen group data");
              }

              var index = $scope.ceventType.specimenGroupData.indexOf(sgData);
              if (index > -1) {
                $scope.ceventType.specimenGroupData.splice(index, 1);
              }
            },
            addAnnotType: function () {
              $scope.ceventType.annotationTypeData.push({name:'', annotationTypeId:'', required: false});
            },
            removeAnnotType: function (atData) {
              if ($scope.ceventType.annotationTypeData.length < 1) {
                throw new Error("invalid length for annotation type data");
              }

              var index = $scope.ceventType.annotationTypeData.indexOf(atData);
              if (index > -1) {
                $scope.ceventType.annotationTypeData.splice(index, 1);
              }
            }
          };

          // used to display the specimen group units label in the form
          $scope.specimenGroupsById = _.indexBy($scope.specimenGroups, 'id');
        }
      };
    }]);

  /**
   * Removes a collection event type.
   */
  mod.service('ceventTypeRemoveService', [
    '$state', 'stateHelper', 'studyRemoveModalService', 'CeventTypeService', 'modalService',
    function ($state, stateHelper, studyRemoveModalService, CeventTypeService, modalService) {
      return {
        remove: function(ceventType) {
          studyRemoveModalService.remove(
            'Remove Collection Event Type',
            'Are you sure you want to remove collection event type ' + ceventType.name + '?',
            function (result) {
              CeventTypeService.remove(ceventType).then(
                function() {
                  stateHelper.reloadAndReinit();
                },
                function(error) {
                  var bodyText = 'Collection event type ' + ceventType.name + ' cannot be removed: ' + error.message;
                  studyRemoveModalService.orError(
                    bodyText,
                    'admin.studies.study.collection',
                    'admin.studies.study.collection');
                });
            },
            function() {
              $state.go('admin.studies.study.collection');
            });
        }
      };
    }]);

  return mod;
});
