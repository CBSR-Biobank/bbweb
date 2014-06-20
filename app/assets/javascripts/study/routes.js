/**
 * Configure routes of user module.
 */
define(['angular'], function(angular) {
  'use strict';

  var mod = angular.module('study.routes', ['study.controllers', 'study.services']);
  mod.config([
    '$routeProvider',
    'userResolve',
    function($routeProvider, userResolve) {
      $routeProvider
        .when(
          '/studies', {
            templateUrl: '/assets/javascripts/study/studies.html',
            controller: 'StudiesCtrl',
            resolve: userResolve
          })
        .when(
          '/studies/table', {
            templateUrl: '/assets/javascripts/study/studiesTable.html',
            controller: 'StudiesTableCtrl',
            resolve: userResolve
          })
        .when(
          '/studies/edit/:id?', {
            templateUrl: '/assets/javascripts/study/studyForm.html',
            controller: 'StudyEditCtrl',
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
            controller: 'StudyCtrl',
            resolve: userResolve
          })
        .when(
          '/studies/partannot/edit/:id?', {
            template: 'edit', //'/assets/javascripts/study/annotationTypeForm.html',
            controller: 'StudyAnnotationTypeEditCtrl',
            resolve: userResolve
          })
        .when(
          '/studies/partannot/remove/:id', {
            template: 'remove', //'/assets/javascripts/study/annotationTypeRemovescaForm.html',
            controller: 'StudyAnnotationTypeRemoveCtrl',
            resolve: userResolve
          });
    }]);
  return mod;
});
