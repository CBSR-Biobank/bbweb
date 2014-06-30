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

  mod.controller('StudyAnnotationTypeEditCtrl', [
    '$scope', '$log', '$state', 'StudyService', 'study', 'annotType',
    function ($scope, $log, $state, StudyService, study, annotType) {
      $log.info("StudyAnnotationTypeEditCtrl:", $state.current.name);

      if ($state.current.name === "admin.studies.study.participantAnnotTypeAdd") {
        $scope.title =  "Add Annotation Type";
        $scope.annotType = { required: false };
      } else {
        $scope.title =  "Update Annotation Type";
        $scope.annotType = annotType;
      }

      $scope.hasRequiredField = (typeof $scope.annotType.required !== 'undefined');

      StudyService.valueTypes().then(function(response) {
        $scope.valueTypes = response.data.sort();
      });

      $scope.addNewOption = function() {
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
        $log.info($scope.annotType.options);
        alert('save annotation type');
      };

      $scope.cancel = function() {
        $state.go('admin.studies.study.participants', { studyId: study.id });
      };
    }]);

  mod.controller('StudyAnnotationTypeRemoveCtrl', [
    '$scope', '$log',
    function ($scope, $log) {
      $log.info("StudyAnnotationTypeRemoveCtrl:");
    }]);

  return mod;
});
