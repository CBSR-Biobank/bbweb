/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  config.$inject = [ '$stateProvider' ];

  function config($stateProvider) {
    $stateProvider
      .state('home.admin.studies.study.specimens.groupAdd', {
        url: '/spcgroup/add',
        resolve: {
          specimenGroup: [
            '$transition$',
            'SpecimenGroup',
            function($transition$, SpecimenGroup) {
              var sg = new SpecimenGroup();
              sg.studyId = $transition$.params().studyId;
              return sg;
            }]
        },
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/admin/studies/specimenGroups/specimenGroupForm.html',
            controller: 'SpecimenGroupEditCtrl as vm'
          }
        }
      })
      .state('home.admin.studies.study.specimens.groupUpdate', {
        url: '/spcgroup/update/{specimenGroupId}',
        resolve: {
          specimenGroup: [
            '$transition$',
            'SpecimenGroup',
            function($transition$, SpecimenGroup) {
              return SpecimenGroup.get($transition$.params().studyId, $transition$.params().specimenGroupId);
            }
          ]
        },
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/admin/studies/specimenGroups/specimenGroupForm.html',
            controller: 'SpecimenGroupEditCtrl as vm'
          }
        }
      });
  }

  return config;
});
