/**
 * Configure routes of user module.
 */
define(['angular', './controllers'], function(angular, controllers) {
  var mod = angular.module('study.routes', ['study.services']);
  mod.config(['$routeProvider', 'userResolve', function($routeProvider, userResolve) {
    $routeProvider
      .when(
        '/studies', {
        templateUrl: '/assets/javascripts/study/studies.html',
        controller: controllers.StudiesCtrl,
        resolve: userResolve
      })
      .when(
        '/studies/table', {
        templateUrl: '/assets/javascripts/study/studiesTable.html',
        controller: controllers.StudiesTableCtrl,
        resolve: userResolve
      })
      .when(
        '/studies/edit/:id?', {
        templateUrl: '/assets/javascripts/study/studyForm.html',
        controller: controllers.StudyEditCtrl,
        resolve: userResolve
      })
      .when(
        '/studies/error', {
        template: '<div><h1>Study does not exist</h1></div>',
        resolve: userResolve
      })
      .when(
        '/studies/:id', {
        templateUrl: '/assets/javascripts/study/study.html',
        controller: controllers.StudyCtrl,
        resolve: userResolve
      })
      .when(
        '/studies/partannot/edit/:id?', {
        template: 'edit', //'/assets/javascripts/study/annotationTypeForm.html',
        controller: controllers.StudyAnnotationTypeEditCtrl,
        resolve: userResolve
      })
      .when(
        '/studies/partannot/remove/:id', {
        template: 'remove', //'/assets/javascripts/study/annotationTypeRemovescaForm.html',
        controller: controllers.StudyAnnotationTypeRemoveCtrl,
        resolve: userResolve
      });
  }]);
  return mod;
});
