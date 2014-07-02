/**
 * Study administration controllers.
 */
define(['angular', 'common'], function(angular, common) {
  'use strict';

  var mod = angular.module('admin.studies.annotTypes.controllers', ['study.services']);

  /**
   * This controller displays a study annotation type in a modal. The information is displayed
   * in an ng-table.
   */
  mod.controller('AnnotationTypeModalCtrl', [
    '$scope', '$modalInstance', 'ngTableParams', 'annotType',
    function ($scope, $modalInstance, ngTableParams, annotType) {
      $scope.annotType = annotType;
      $scope.data = [];

      $scope.data.push({name: 'Name:', value: annotType.name});
      $scope.data.push({name: 'Type:', value: annotType.valueType});

      if (!annotType.required) {
        $scope.data.push({name: 'Required:', value: annotType.required ? "Yes" : "No"});
      }

      if (annotType.valueType === 'Select') {
        var optionValues = [];
        for (var name in annotType.options) {
          optionValues.push(annotType.options[name]);
        }

        $scope.data.push({
          name: '# Selections Allowed:',
          value: annotType.maxValueCount === 1 ? "Single" : "Multiple"});
        $scope.data.push({
          name: 'Selections:',
          value: optionValues.join(",")});
      }

      $scope.data.push({name: 'Description:', value: annotType.description});

      /* jshint ignore:start */
      $scope.tableParams = new ngTableParams({
        page:1,
        count: 10
      }, {
        counts: [], // hide page counts control
        total: $scope.data.length,           // length of data
        getData: function($defer, params) {
          $defer.resolve($scope.data);
        }
      });
      /* jshint ignore:end */

      $scope.ok = function () {
        $modalInstance.close();
      };
    }]);

  /**
   * Common code to add or edit an annotation type.
   */
  function studyAnnotationTypeEditCommon($scope, $state, $stateParams, $modal, StudyService, ParticipantAnnotTypeService) {
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
            $scope, $modal, annotType, error,
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
      $state.go('admin.studies.study.participants', { studyId: study.id });
    };
  }

  function annotTypeSaveError($scope, $modal, annotType, error, onOk, onCancel) {
    var modalInstance = {};
    var modalParams = {};

    if (error.message.indexOf("expected version doesn't match current version") > -1) {
      /* concurrent change error */
      modalParams.title = "Modified by another user";
      modalParams.message = "Another user already made changes to this annotation type. Press OK to make " +
        " your changes again, or Cancel to dismiss your changes.";
    } else {
      /* some other error */
      modalParams.title = annotType.id ?  "Cannot update annotation type" : "Cannot add annotation type";
      modalParams.message = "Error: " + error.message;
    }

    modalInstance = $modal.open({
      resolve: {
        title: function () {
          return modalParams.title;
        },
        message: function() {
          return modalParams.message;
        }
      },
      templateUrl: '/assets/javascripts/common/okCancelModal.html',
      controller: 'OkCancelModal'
    });

    modalInstance.result.then(function(selectedItem) {
      onOk();
    }, function () {
      onCancel();
    });
  }

  mod.controller('StudyAnnotationTypeAddCtrl', [
    '$scope', '$state', '$stateParams', '$modal', 'StudyService', 'ParticipantAnnotTypeService', 'study', 'annotType',
    function ($scope, $state, $stateParams, $modal, StudyService, ParticipantAnnotTypeService, study, annotType) {
      $scope.title =  "Add Annotation Type";
      $scope.study = study;
      $scope.annotType = annotType;
      studyAnnotationTypeEditCommon($scope, $state, $stateParams, $modal, StudyService, ParticipantAnnotTypeService);
    }]);

  mod.controller('StudyAnnotationTypeUpdateCtrl', [
    '$scope', '$state', '$stateParams', '$modal', 'StudyService', 'ParticipantAnnotTypeService', 'study', 'annotType',
    function ($scope, $state, $stateParams, $modal, StudyService, ParticipantAnnotTypeService, study, annotType) {
      $scope.title =  "Update Participant Annotation Type";
      $scope.study = study;
      $scope.annotType = annotType;
      studyAnnotationTypeEditCommon($scope, $state, $stateParams, $modal, StudyService, ParticipantAnnotTypeService);
    }]);

  return mod;
});
