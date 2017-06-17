/**
 * Configure routes of centres module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function (_) {
  'use strict';

  centreStates.$inject = [ '$stateProvider' ];

  function centreStates($stateProvider) {

    $stateProvider
      .state('home.admin.centres', {
        url: '/centres',
        views: {
          'main@': {
            template: '<centres-admin></centres-admin>'
          }
        }
      })
      .state('home.admin.centres.add', {
        url: '/add',
        views: {
          'main@': {
            template: '<centre-add></centre-add>'
          }
        }
      })
      .state('home.admin.centres.centre', {
        abstract: true,
        url: '/{centreId}',
        resolve: {
          centre: resolveCentre
        },
        views: {
          'main@': {
            template: '<centre-view centre="vm.centre"></centre-view>',
            controller: [
              'centre',
              function (centre) {
                this.centre = centre;
              }
            ],
            controllerAs: 'vm'
          }
        }
      })
      .state('home.admin.centres.centre.summary', {
        url: '/summary',
        views: {
          'centreDetails': {
            template: '<centre-summary centre="vm.centre"></centre-summary>',
            controller: [
              'centre',
              function (centre) {
                this.centre = centre;
              }
            ],
            controllerAs: 'vm'
          }
        }
      })
      .state('home.admin.centres.centre.locations', {
        url: '/locations',
        views: {
          'centreDetails': {
            template: '<locations-panel centre="vm.centre"></locations-panel>',
            controller: [
              'centre',
              function(centre) {
                this.centre = centre;
              }
            ],
            controllerAs: 'vm'
          }
        }
      })
      .state('home.admin.centres.centre.locations.locationAdd', {
        url: '/location/add',
        views: {
          'main@': {
            template: '<centre-location-add centre="vm.centre"></centre-location-add>',
            controller: [
              'centre',
              function (centre) {
                this.centre = centre;
              }
            ],
            controllerAs: 'vm'
          }
        }
      })
      .state('home.admin.centres.centre.locations.locationView', {
        url: '/location/view/:locationId',
        resolve: {
          location: [
            '$transition$',
            'centre',
            function ($transition$, centre) {
              return _.find(centre.locations, { id: $transition$.params().locationId });
            }
          ]
        },
        views: {
          'main@': {
            template: '<centre-location-view centre="vm.centre" location="vm.location"></centre-location-view>',
            controller: [
              'centre',
              'location',
              function (centre, location) {
                this.centre = centre;
                this.location = location;
              }
            ],
            controllerAs: 'vm'
          }
        }
      })
      .state('home.admin.centres.centre.studies', {
        url: '/studies',
        resolve: {
          centre: resolveCentre,
          studyNames: ['Study', function(Study) {
            return Study.names();
          }]
        },
        views: {
          'centreDetails': {
            template: [
              '<centre-studies-panel',
              '  centre="vm.centre" ',
              '  centre-studies="vm.centreStudies" ',
              '  study-names="vm.studyNames"> ',
              '</centre-studies-panel>'
            ].join(''),
            controller: [
              'centre', 'studyNames',
              function(centre, studyNames) {
                var vm = this;
                vm.centre     = centre;
                vm.studyNames = studyNames;
              }
            ],
            controllerAs: 'vm'
          }
        }
      });

    resolveCentre.$inject = ['$transition$', 'Centre'];
    function resolveCentre($transition$, Centre) {
      return Centre.get($transition$.params().centreId);
    }

  }

  return centreStates;
});
