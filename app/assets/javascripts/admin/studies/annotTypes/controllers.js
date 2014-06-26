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
  mod.controller('AnnotationTypeModalCtrl', function ($scope, $modalInstance, ngTableParams, annotType) {
    $scope.annotType = annotType;
    $scope.data = [];

    $scope.data.push({name: 'Name:', value: annotType.name});
    $scope.data.push({name: 'Type:', value: annotType.valueType});

    if (annotType.required !== undefined) {
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
  });

  mod.controller('StudyAnnotationTypeEditCtrl', function ($scope, $log, $state) {
    $log.info("StudyAnnotationTypeEditCtrl:", $state.current.name);

    if ($state.current.name === "admin.studies.view.participantAnnotTypeAdd") {
        $scope.title =  "Add Annotation Type";
    } else {
        $scope.title =  "Update Annotation Type";
    }
  });

  mod.controller('StudyAnnotationTypeRemoveCtrl', function ($scope, $log, $routeParams) {
    $log.info("StudyAnnotationTypeRemoveCtrl:", $routeParams);
  });

  return mod;
});
