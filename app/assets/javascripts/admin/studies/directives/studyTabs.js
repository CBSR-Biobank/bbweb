/**
 * Tabs used when displaying a study.
 *
 *  FIXME: See https://docs.angularjs.org/guide/directive, section "Creating Directives that Communicate" to
 * improve this code.
 */
define(['angular'], function(angular) {
  'use strict';

  var mod = angular.module('study.directives.studyTabs', ['study.services']);
  //   .directive('summaryPane', function() {
  //     return {
  //       require: '^tab',
  //       restrict: 'E',
  //       templateUrl: '/assets/javascripts/study/studySummaryPane.html'
  //     };
  //   })

  //   .directive('participantsPane', function() {
  //       /**
  //        * Displays study annotation type summary information in a table. The user can then select an
  //        * annotation to display more informaiton.
  //        */
  //       return {
  //         require: '^tab',
  //         restrict: 'E',
  //         templateUrl: '/assets/javascripts/study/studyParticipantsPane.html',
  //         controller: controllers.AnnotationTypeDirectiveCtrl
  //       };
  //     });

  return mod;
});
