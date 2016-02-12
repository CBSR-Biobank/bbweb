/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  config.$inject = [
    '$urlRouterProvider',
    '$stateProvider',
    'authorizationProvider'
  ];

  function config($urlRouterProvider, $stateProvider, authorizationProvider ) {
    // FIXME does this need to be in each state definition file?
    $urlRouterProvider.otherwise('/');

    /**
     * Used to add a specimen group.
     */
    $stateProvider.state('home.admin.studies.study.specimens.groupAdd', {
      url: '/spcgroup/add',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
        specimenGroup: [
          '$stateParams',
          'SpecimenGroup',
          function($stateParams, SpecimenGroup) {
            var sg = new SpecimenGroup();
            sg.studyId = $stateParams.studyId;
            return sg;
          }]
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/studies/specimenGroups/specimenGroupForm.html',
          controller: 'SpecimenGroupEditCtrl as vm'
        }
      },
      data: {
        displayName: 'Specimen Group'
      }
    });

    /**
     * Used to update a specimen group.
     */
    $stateProvider.state('home.admin.studies.study.specimens.groupUpdate', {
      url: '/spcgroup/update/{specimenGroupId}',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
        specimenGroup: [
          '$stateParams',
          'SpecimenGroup',
          function($stateParams, SpecimenGroup) {
            return SpecimenGroup.get($stateParams.studyId, $stateParams.specimenGroupId);
          }
        ]
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/studies/specimenGroups/specimenGroupForm.html',
          controller: 'SpecimenGroupEditCtrl as vm'
        }
      },
      data: {
        displayName: 'Specimen Group'
      }
    });
  }

  return config;
});
