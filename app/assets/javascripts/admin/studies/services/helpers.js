/** Common helpers */
define(['angular', 'underscore'], function(angular, _) {
  'use strict';

  var mod = angular.module('admin.studies.helpers', []);

  mod.service('studyViewSettings', function() {
    var initialSettings = function() {
      return {
        studyId: null,
        panelStates: {
          participantAnnotTypes: true,
          specimenGroups:        true,
          ceventAnnotTypes:      true,
          ceventTypes:           true,
          processingTypes:       true,
          spcLinkAnnotTypes:     true,
          spcLinkTypes:          true
        }
      };
    };

    var currentState = initialSettings();

    return {
      panelState: function(panel, state) {
        if (typeof currentState.panelStates[panel] === 'undefined') {
          throw new Error("panel not defined: " + panel);
        }

        if (state === undefined) {
          return currentState.panelStates[panel];
        }
        currentState.panelStates[panel] = state;
        return state;
      },
      panelStateToggle: function(panel) {
        if (typeof currentState.panelStates[panel] === 'undefined') {
          throw new Error("panel not defined: " + panel);
        }
        currentState.panelStates[panel] = !currentState.panelStates[panel];
        return currentState.panelStates[panel];
      },
      initialize: function(studyId) {
        if (studyId != currentState.studyId) {
          // initialize state only when a new study is selected
          currentState = initialSettings();
          currentState.studyId = studyId;
        }
      },
      getState: function() {
        return currentState;
      }
    };
  });

  mod.service('panelTableService', ['$filter', 'ngTableParams', function ($filter, ngTableParams) {
    this.getTableParams = function(data) {
      /* jshint ignore:start */
      return new ngTableParams({
        page: 1,            // show first page
        count: 10,          // count per page
        sorting: {
          name: 'asc'       // initial sorting
        }
      },{
        counts: [], // hide page counts control
        total: data.length,
        getData: function($defer, params) {
          var orderedData = params.sorting()
            ? $filter('orderBy')(data, params.orderBy())
            : data;
          $defer.resolve(orderedData.slice(
            (params.page() - 1) * params.count(),
            params.page() * params.count()));
        }
      });
      /* jshint ignore:end */
    };
  }]);

  /**
   * Called where there was an error on attempting to add or update a study.
   */
  mod.service('studyEditService', [
    '$state', 'modalService', 'stateHelper', 'StudyService',
    function ($state, modalService, stateHelper, StudyService) {

      var onError = function (study, error, onCancel) {
        var modalOptions = {
          closeButtonText: 'Cancel',
          actionButtonText: 'OK'
        };

        if (error.message.indexOf("expected version doesn't match current version") > -1) {
          /* concurrent change error */
          modalOptions.headerText = 'Modified by another user';
          modalOptions.bodyText = 'Another user already made changes to this study. Press OK to make ' +
            ' your changes again, or Cancel to dismiss your changes.';
        } else {
          /* some other error */
          modalOptions.headerText = 'Cannot ' + (study.id ?  'update' : 'add') + ' study';
          modalOptions.bodyText = 'Error: ' + error.message;
        }

        modalService.showModal({}, modalOptions).then(function (result) {
          stateHelper.reloadAndReinit();
        }, function () {
          onCancel();
        });
      };

      return {
        edit : function($scope, onSubmitSuccess, onSubmitErrorCancel, onCancel) {
          $scope.submit = function(study) {
            StudyService.addOrUpdate(study)
              .success(function() {
                onSubmitSuccess();
              })
              .error(function(error) {
                onError($scope.study, error, onSubmitErrorCancel);
              });
          };

          $scope.cancel = function(study) {
            onCancel();
          };
        }
      };
    }]);

  /**
   * Displays a study annotation type in a modal. The information is displayed in an ng-table.
   *
   */
  mod.service('addTimeStampsService', function() {
    return {
      get: function(modelObj) {
        var data = [];
        data.push({name: 'Date added:', value: modelObj.addedDate});
        if (modelObj.lastUpdateDate !== null) {
          data.push({name: 'Last updated:', value: modelObj.lastUpdateDate});
        }
        return data;
      }
    };
  });

  /**
   * Displays a study annotation type in a modal. The information is displayed in an ng-table.
   *
   */
  mod.service('annotTypeModalService', [
    'modelObjModalService', 'addTimeStampsService',
    function (modelObjModalService, addTimeStampsService) {
      return {
        show: function (title, annotType) {
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
        }
      };
    }]);

  /**
   * Common code to add or edit an annotation type.
   */
  mod.service('studyAnnotTypeEditService', [
    '$state', 'stateHelper', 'StudyAnnotTypeService', 'modalService',
    function($state, stateHelper, StudyAnnotTypeService, modalService) {
      return {
        edit: function($scope, onSubmit, onCancel) {
          $scope.hasRequiredField = (typeof $scope.annotType.required !== 'undefined');

          StudyAnnotTypeService.valueTypes().then(function(response) {
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
            onSubmit(annotType);
          };

          $scope.cancel = function() {
            onCancel();
          };
        },
        onError: function($scope, error, stateOnCancel) {
          var modalOptions = {
            closeButtonText: 'Cancel',
            actionButtonText: 'OK'
          };

          if (error.message.indexOf("expected version doesn't match current version") > -1) {
            /* concurrent change error */
            modalOptions.headerText = 'Modified by another user';
            modalOptions.bodyText = 'Another user already made changes to this annotation type. ' +
              'Press OK to make your changes again, or Cancel to dismiss your changes.';
          } else {
            /* some other error */
            modalOptions.headerText =
              'Cannot ' + $scope.annotType.id ?  'update' : 'add' + ' annotation type';
            modalOptions.bodyText = 'Error: ' + error.message;
          }

          modalService.showModal({}, modalOptions).then(function (result) {
            stateHelper.reloadAndReinit();
          }, function () {
            $state.go(stateOnCancel);
          });
        }
      };
    }]);

  mod.service('studyRemoveModalService', [
    '$state', 'modalService', function ($state, modalService) {
      return {
        remove: function (title, message, onConfirm, onCancel) {
          var modalOptions = {
            closeButtonText: 'Cancel',
            headerText: title,
            bodyText: message
          };

          modalService.showModal({}, modalOptions).then(function (result) {
            onConfirm();
          }, function() {
            onCancel();
          });
        },
        onError: function(bodyText, onModalOkState, onModalCancelState) {
          var modalOptions = {
            closeButtonText: 'Cancel',
            headerText: 'Remove failed',
            bodyText: bodyText
          };

          modalService.showModal({}, modalOptions).then(function (result) {
            $state.go(onModalOkState);
          }, function () {
            $state.go(onModalCancelState);
          });
        }
      };
    }]);

  return mod;
});
