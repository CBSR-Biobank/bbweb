/** Common helpers */
define(['angular'], function(angular) {
  'use strict';

  var mod = angular.module('admin.studies.processing.helpers', []);

  /**
   * Displays a processing type in a modal. The information is displayed in an ng-table.
   *
   */
  mod.service('processingTypeModalService', [
    '$filter', 'modelObjModalService', 'addTimeStampsService',
    function ($filter, modelObjModalService, addTimeStampsService) {
      return {
        show: function (processingType) {
          var title = 'Processing Type';
          var data = [];
          data.push({name: 'Name:', value: processingType.name});
          data.push({name: 'Enabled:', value: processingType.enabled ? 'Yes' : 'No'});
          data.push({name: 'Description:', value: processingType.description});
          data = data.concat(addTimeStampsService.get(processingType));
          modelObjModalService.show(title, data);
        }
      };
    }]);

  /**
   * Common code to add or edit a processing type.
   */
  mod.service('processingTypeEditService', [
    '$state', '$stateParams', '$filter', 'stateHelper', 'modalService', 'ProcessingTypeService',
    function($state, $stateParams, $filter, stateHelper, modalService, ProcessingTypeService) {

      /*
       * Called when the submission failed due to an error.
       */
      var saveError = function ($scope, processingType, error) {
        var modalOptions = {
          closeButtonText: 'Cancel',
          actionButtonText: 'OK'
        };

        if (error.message.indexOf("expected version doesn't match current version") > -1) {
          /* concurrent change error */
          modalOptions.headerText = 'Modified by another user';
          modalOptions.bodyText = 'Another user already made changes to this processing type. Press OK to make ' +
            'your changes again, or Cancel to dismiss your changes.';
        } else {
          /* some other error */
          modalOptions.headerText =
            'Cannot ' +  (processingType.id ?  'update' : 'add ') + ' Processing Type';
          modalOptions.bodyText = 'Error: ' + error.message;
        }

        modalService.showModal({}, modalOptions).then(function (result) {
          stateHelper.reloadAndReinit();
        }, function () {
          $state.go('admin.studies.study.processing');
        });
      };

      return {
        edit: function($scope) {
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
        }
      };
    }]);

  /**
   * Removes a processing type.
   */
  mod.service('processingTypeRemoveService', [
    '$state', 'stateHelper', 'studyRemoveModalService', 'ProcessingTypeService', 'modalService',
    function ($state, stateHelper, studyRemoveModalService, ProcessingTypeService, modalService) {
      return {
        remove: function(processingType) {
          studyRemoveModalService.remove(
            'Remove Processing Type',
            'Are you sure you want to remove processing type ' + processingType.name + '?',
            function (result) {
              ProcessingTypeService.remove(processingType).then(
                function() {
                  stateHelper.reloadAndReinit();
                },
                function(error) {
                  var bodyText = 'Processing type ' + processingType.name + ' cannot be removed: ' + error.message;
                  studyRemoveModalService.orError(
                    bodyText,
                    'admin.studies.study.processing',
                    'admin.studies.study.processing');
                });
            },
            function() {
              $state.go('admin.studies.study.processing');
            });
        }
      };
    }]);

  mod.service('spcLinkAnnotTypeEditService', [
    '$state', '$stateParams', 'modalService', 'studyAnnotTypeEditService', 'SpcLinkAnnotTypeService',
    function($state, $stateParams, modalService, studyAnnotTypeEditService, SpcLinkAnnotTypeService) {
      return {
        edit: function ($scope) {

          var onSubmit = function (annotType) {
            SpcLinkAnnotTypeService.addOrUpdate(annotType).then(
              function() {
                $state.transitionTo(
                  'admin.studies.study.processing',
                  $stateParams,
                  { reload: true, inherit: false, notify: true });
              },
              function(error) {
                studyAnnotTypeEditService.onError($scope, error, 'admin.studies.study.processing');
              });
          };

          var onCancel = function () {
            $state.go('admin.studies.study.processing');
          };

          studyAnnotTypeEditService.edit($scope, onSubmit, onCancel);
        }
      };
    }]);

  /**
   * Removes a participant annotation type.
   */
  mod.service('spcLinkAnnotTypeRemoveService', [
    '$state', 'stateHelper', 'studyRemoveModalService', 'SpcLinkAnnotTypeService', 'modalService',
    function ($state, stateHelper, studyRemoveModalService, SpcLinkAnnotTypeService, modalService) {
      return {
        remove: function(annotType) {
          studyRemoveModalService.remove(
            'Remove Specimen Link Annotation Type',
            'Are you sure you want to remove annotation type ' + annotType.name + '?',
            function (result) {
              SpcLinkAnnotTypeService.remove(annotType).then(
                function() {
                  stateHelper.reloadAndReinit();
                },
                function(error) {
                  var bodyText = 'Annotation type ' + annotType.name + ' cannot be removed: ' + error.message;
                  studyRemoveModalService.orError(
                    bodyText,
                    'admin.studies.study.processing',
                    'admin.studies.study.processing');
                });
            },
            function() {
              $state.go('admin.studies.study.processing');
            });
        }
      };
    }]);

  /**
   * Displays a specimen link type in a modal. The information is displayed in an ng-table.
   *
   */
  mod.service('spcLinkTypeModalService', [
    '$filter', 'modelObjModalService', 'addTimeStampsService',
    function ($filter, modelObjModalService, addTimeStampsService) {
      return {
        show: function (spcLinkType, processingTypesById, specimenGropusById) {
          var title = 'Specimen Link Type';
          var data = [];
          var inputGroup =  specimenGropusById[spcLinkType.inputGroupId];
          var outputGroup =  specimenGropusById[spcLinkType.outputGroupId];

          data.push({name: 'Processing Type:',
                     value: processingTypesById[spcLinkType.processingTypeId].name});
          data.push({name: 'Input Group:', value: inputGroup.name});
          data.push({name: 'Expected input change:',
                     value: spcLinkType.expectedInputChange + ' ' + inputGroup.units});
          data.push({name: 'Input count:', value: spcLinkType.inputCount});
          data.push({name: 'Input Container Type:', value: 'None'});
          data.push({name: 'Output Group:', value: outputGroup.name});
          data.push({name: 'Expected output change:',
                     value: spcLinkType.expectedInputChange + ' ' + outputGroup.units});
          data.push({name: 'Output count:', value: spcLinkType.outputCount});
          data.push({name: 'Output Container Type:', value: 'None'});
          data = data.concat(addTimeStampsService.get(spcLinkType));
          modelObjModalService.show(title, data);
        }
      };
    }]);


  /**
   * Common code to add or edit a specimen link type.
   */
  mod.service('spcLinkTypeEditService', [
    '$state',
    '$stateParams',
    '$filter',
    'stateHelper',
    'modalService',
    'SpcLinkTypeService',
    function($state,
             $stateParams,
             $filter,
             stateHelper,
             modalService,
             SpcLinkTypeService) {
      /*
       * Called when the submission failed due to an error.
       */
      var saveError = function ($scope, spcLinkType, error) {
        var modalOptions = {
          closeButtonText: 'Cancel',
          actionButtonText: 'OK'
        };

        if (error.message.indexOf("expected version doesn't match current version") > -1) {
          /* concurrent change error */
          modalOptions.headerText = 'Modified by another user';
          modalOptions.bodyText = 'Another user already made changes to this specimen link type. Press OK to make ' +
            'your changes again, or Cancel to dismiss your changes.';
        } else {
          /* some other error */
          modalOptions.headerText =
            'Cannot ' +  (spcLinkType.id ?  'update' : 'add ') + ' Specimen Link Type';
          modalOptions.bodyText = 'Error: ' + error.message;
        }

        modalService.showModal({}, modalOptions).then(function (result) {
          stateHelper.reloadAndReinit();
        }, function () {
          $state.go('admin.studies.study.processing');
        });
      };

      return {
        edit: function($scope) {
          $scope.form = {
            submit: function(spcLinkType) {
              var checkFields = ['inputContainerTypeId', 'outputContainerTypeId'];
              checkFields.forEach(function(fieldName) {
                if (typeof spcLinkType[fieldName] === 'undefined') {
                  spcLinkType[fieldName] = null;
                }
              });

              if (typeof spcLinkType.annotationTypeData === 'undefined') {
                spcLinkType.annotationTypeData = [];
              }

              SpcLinkTypeService.addOrUpdate(spcLinkType).then(
                function() {
                  $state.transitionTo(
                    'admin.studies.study.processing',
                    $stateParams,
                    { reload: true, inherit: false, notify: true });
                },
                function(error) {
                  saveError($scope, spcLinkType, error);
                });
            },
            cancel: function() {
              $state.go('admin.studies.study.processing', { studyId: $scope.study.id });
            },
            addAnnotType: function () {
              $scope.spcLinkType.annotationTypeData.push({name:'', annotationTypeId:'', required: false});
            },
            removeAnnotType: function (atData) {
              if ($scope.spcLinkType.annotationTypeData.length < 1) {
                throw new Error("invalid length for annotation type data");
              }

              var index = $scope.spcLinkType.annotationTypeData.indexOf(atData);
              if (index > -1) {
                $scope.spcLinkType.annotationTypeData.splice(index, 1);
              }
            }
          };
        }
      };
    }]);

  /**
   * Removes a specimen link type.
   */
  mod.service('spcLinkTypeRemoveService', [
    '$state', 'stateHelper', 'studyRemoveModalService', 'SpcLinkTypeService', 'modalService',
    function ($state, stateHelper, studyRemoveModalService, SpcLinkTypeService, modalService) {
      return {
        remove: function(spcLinkType) {
          studyRemoveModalService.remove(
            'Remove Specimen Link Type',
            'Are you sure you want to remove specimen link type ' + spcLinkType.name + '?',
            function (result) {
              SpcLinkTypeService.remove(spcLinkType).then(
                function() {
                  stateHelper.reloadAndReinit();
                },
                function(error) {
                  var bodyText = 'specimen link type ' + spcLinkType.name + ' cannot be removed: ' + error.message;
                  studyRemoveModalService.orError(
                    bodyText,
                    'admin.studies.study.processing',
                    'admin.studies.study.processing');
                });
            },
            function() {
              $state.go('admin.studies.study.processing');
            });
        }
      };
    }]);

  return mod;
});
