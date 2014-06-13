/**
 * User controllers.
 */
define(['angular'], function(angular) {
  'use strict';

  /**
   * user is not a service, but stems from userResolve (Check ../user/services.js) object.
   */
  var StudiesCtrl = function($scope, $rootScope, $location, user, studyService) {
    $rootScope.pageTitle = 'Biobank studies';
    $scope.studies = [];
    $scope.user = user;

    studyService.list().then(function(response) {
      $scope.studies = response.data;
    });

  };

  /**
   * See http://stackoverflow.com/questions/22881782/angularjs-tabset-does-not-show-the-correct-state-when-the-page-is-reloaded
   */
  var StudyCtrl = function($scope, $rootScope, $location, user, studyService) {
    $rootScope.pageTitle = 'Biobank study';
    $scope.user = user;
    $scope.study = {};
    $scope.tableParams = {};

    studyService.query().then(function(response) {
      $scope.study = response.data;
    });
  };

  var StudyAddCtrl = function($scope, $rootScope, $location, user, studyService) {
    $rootScope.pageTitle = 'Biobank study';
    $scope.form = {
      title: "Add new study",
      study: {
        type: "AddStudyCmd",
        name: "",
        description: null
      }
    };

    $scope.submit = function(study) {
      studyService.add(study).then(function(response) {
        $location.path('/studies');
      });
    };
  };

  var AnnotationTypeCtrl = function ($scope, $modalInstance, ngTableParams, annotType) {
    $scope.annotType = annotType;
    $scope.data = [];

    $scope.data.push({name: 'Name:',     value: annotType.name});
    $scope.data.push({name: 'Type:',     value: annotType.valueType});

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
  };

  return {
    StudiesCtrl: StudiesCtrl,
    StudyCtrl: StudyCtrl,
    StudyAddCtrl: StudyAddCtrl,
    AnnotationTypeCtrl: AnnotationTypeCtrl
  };

});
