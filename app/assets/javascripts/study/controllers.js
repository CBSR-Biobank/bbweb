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

  return {
    StudiesCtrl: StudiesCtrl,
    StudyCtrl: StudyCtrl,
    StudyAddCtrl: StudyAddCtrl
  };

});
