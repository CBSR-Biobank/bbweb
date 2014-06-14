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

  /**
   * Displays study annotation type summary information in a table. The user can then select a row
   * to display more informaiton for te annotation type.
   */
  var AnnotationTypeDirectiveCtrl = function($log, $route, $modal, $filter, ngTableParams, studyService, $scope) {
    /* jshint ignore:start */
    $scope.tableParams = new ngTableParams({
      page: 1,            // show first page
      count: 10,          // count per page
      sorting: {
        name: 'asc'     // initial sorting
      }
    }, {
      counts: [], // hide page counts control
      total: 0,           // length of data
      getData: function($defer, params) {
        var study = { id: $route.current.params.id };
        studyService.participantInfo(study).then(function(response) {
          var orderedData = params.sorting()
            ? $filter('orderBy')(response.data, params.orderBy())
            : response.data;
          params.total(orderedData.length);
          $defer.resolve(orderedData.slice(
            (params.page() - 1) * params.count(),
            params.page() * params.count()));
        });
      }
    });
    /* jshint ignore:end */

    $scope.changeSelection = function(annotType) {
      $log.debug(annotType);

      var modalInstance = $modal.open({
        templateUrl: '/assets/templates/study/annotationType.html',
        controller: AnnotationTypeCtrl,
        backdrop: true,
        size: 'sm',
        resolve: {
          annotType: function () {
            return annotType;
          }
        }
      });
    };
  };

  /**
   * This controller displays a study annotation type in a modal. The information is displayed
   * in a table.
   */
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
    AnnotationTypeDirectiveCtrl: AnnotationTypeDirectiveCtrl,
    AnnotationTypeCtrl: AnnotationTypeCtrl
  };

});
