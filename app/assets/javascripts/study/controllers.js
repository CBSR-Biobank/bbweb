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
    studyService.list().then(function(response) {
      $scope.studies = response.data;
      $scope.user = user;
    });

    $scope.loadTab = function(tabName) {
    };

  };

  /**
   * See http://stackoverflow.com/questions/22881782/angularjs-tabset-does-not-show-the-correct-state-when-the-page-is-reloaded
   */
  var StudyCtrl = function($scope, $rootScope, $location, user, studyService) {
    $rootScope.pageTitle = 'Biobank study';
    studyService.query().then(function(response) {
      $scope.study = response.data;
      $scope.user = user;
    });
  };

  var StudyAddCtrl = function($scope, $rootScope, $location, user, studyService) {
    $rootScope.pageTitle = 'Biobank study';
    $scope.title = "AddStudy";
  };

  return {
    StudiesCtrl: StudiesCtrl,
    StudyCtrl: StudyCtrl,
    StudyAddCtrl: StudyAddCtrl
  };

});
