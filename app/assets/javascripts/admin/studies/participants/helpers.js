/** Common helpers */
define(['angular'], function(angular) {
  'use strict';

  var mod = angular.module('admin.studies.participants.helpers', []);

  /**
   * Displays a study annotation type in a modal. The information is displayed in an ng-table.
   *
   */
  mod.service('annotTypeModalService', [
    'modelObjModalService', 'addTimeStampsService',
    function (modelObjModalService, addTimeStampsService) {
      this.show = function (title, annotType) {
        var data = [];
        data.push({name: 'Name:', value: annotType.name});
        data.push({name: 'Type:', value: annotType.valueType});

        if (typeof annotType.required !== 'undefined') {
          data.push({name: 'Required:', value: annotType.required ? "Yes" : "No"});
        }

        if (annotType.valueType === 'Select') {
          var optionValues = [];
          for (var name in annotType.options) {
            optionValues.push(annotType.options[name]);
          }

          data.push({
            name: '# Selections Allowed:',
            value: annotType.maxValueCount === 1 ? "Single" : "Multiple"});
          data.push({
            name: 'Selections:',
            value: optionValues.join(", ")});
        }

        data.push({name: 'Description:', value: annotType.description});
        data = data.concat(addTimeStampsService.get(annotType));

        modelObjModalService.show(title, data);
      };
    }]);

  mod.service('paricipantAnnotTypeEditService', [
    '$state', '$stateParams', 'modalService', 'studyAnnotationTypeService', 'ParticipantAnnotTypeService',
    function($state, $stateParams, modalService, studyAnnotationTypeService, ParticipantAnnotTypeService) {
      return {
        edit: function ($scope) {

          var onSubmit = function (annotType) {
            ParticipantAnnotTypeService.addOrUpdate(annotType)
              .success(function() {
                $state.go('admin.studies.study.participants');
              })
              .error(function(error) {
                studyAnnotationTypeService.onError($scope, error, 'admin.studies.study.participants');
              });
          };

          var onCancel = function () {
            $state.go('admin.studies.study.participants');
          };

          studyAnnotationTypeService.edit($scope, onSubmit, onCancel);
        }
      };
    }]);

  /**
   * Removes a participant annotation type.
   */
  mod.service('participantAnnotTypeRemoveService', [
    '$state', '$stateParams', 'stateHelper', 'studyAnnotTypeRemoveService', 'ParticipantAnnotTypeService', 'modalService',
    function ($state, $stateParams, stateHelper, studyAnnotTypeRemoveService, ParticipantAnnotTypeService, modalService) {
      return {
        remove: function(annotType) {
          studyAnnotTypeRemoveService.remove(
            'Remove Participant Annotation Type',
            'Are you sure you want to remove annotation type ' + annotType.name + '?',
            function (result) {
              ParticipantAnnotTypeService.remove(annotType)

                .success(function() {
                  stateHelper.reloadAndReinit();
                })

                .error(function(error) {
                  var modalOptions = {
                    closeButtonText: 'Cancel',
                    headerText: 'Remove failed',
                    bodyText: 'Annotation type ' + annotType.name + ' cannot be removed: ' + error.message
                  };

                  modalService.showModal({}, modalOptions).then(function (result) {
                    $state.go('admin.studies.study.participants');
                  }, function () {
                    $state.go('admin.studies.study.participants');
                  });
                });
            },
            function() {
              $state.go('admin.studies.study.participants');
            });
        }
      };
    }]);

  return mod;
});
