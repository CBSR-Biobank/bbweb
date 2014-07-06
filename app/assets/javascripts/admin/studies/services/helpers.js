/** Common helpers */
define(['angular'], function(angular) {
  'use strict';

  var mod = angular.module('admin.studies.helpers', []);

  /**
   * Used to sort a list of studies. Returns the comparison of names of the two studies.
   */
  mod.service('studyCompareService', function() {
    this.compare = function (a, b) {
      if (a.name < b.name) {
        return -1;
      } else if (a.name > b.name) {
        return 1;
      }
      return 0;
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
   * Common code to add or edit an annotation type.
   */
  mod.service('studyAnnotationTypeService', [
    '$state', '$stateParams', 'stateHelper', 'StudyService', 'modalService',
    function($state, $stateParams, stateHelper, StudyService, modalService) {
      return {
        edit: function($scope, onSubmit, onCancel) {
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

  mod.service('studyAnnotTypeRemoveService', ['modalService', function (modalService) {
    this.remove = function (title, message, onConfirm, onCancel) {
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
    };
  }]);

  return mod;
});
