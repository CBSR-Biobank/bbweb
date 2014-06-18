/**
 * Configure routes of user module.
 */
define(['angular', './controllers', 'common'], function(angular, controllers) {
  var mod = angular.module('study.routes', ['study.services', 'biobank.common']);
  mod.config(['$routeProvider', 'userResolve', function($routeProvider, userResolve) {
    $routeProvider
      .when(
        '/studies', {
        templateUrl: '/assets/templates/study/studies.html',
        controller: controllers.StudiesCtrl,
        resolve: userResolve
      }).when(
        '/studies/edit/:id?', {
        templateUrl: '/assets/templates/study/studyForm.html',
        controller: controllers.StudyEditCtrl,
        resolve: userResolve
      }).when(
        '/studies/error', {
        template: '<div><h1>Study does not exist</h1></div>',
        resolve: userResolve
      }).when(
        '/studies/:id', {
        templateUrl: '/assets/templates/study/study.html',
        controller: controllers.StudyCtrl,
        resolve: userResolve
      }).when(
        '/studies/partannot/edit/:id?', {
        template: 'edit', //'/assets/templates/study/annotationTypeForm.html',
        controller: controllers.StudyAnnotationTypeEditCtrl,
        resolve: userResolve
      }).when(
        '/studies/partannot/remove/:id', {
        template: 'remove', //'/assets/templates/study/annotationTypeRemovescaForm.html',
        controller: controllers.StudyAnnotationTypeRemoveCtrl,
        resolve: userResolve
      });
  }]);
  return mod;
});
