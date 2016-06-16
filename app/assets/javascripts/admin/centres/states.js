/**
 * Configure routes of centres module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function (_) {
  'use strict';

  centreStates.$inject = ['$urlRouterProvider', '$stateProvider'];

  function centreStates($urlRouterProvider, $stateProvider, usersService ) {

    resolveCentre.$inject = ['$stateParams', 'Centre'];
    function resolveCentre($stateParams, Centre) {
      return Centre.get($stateParams.centreId);
    }

    $urlRouterProvider.otherwise('/');

    /**
     * Centres - view all centres
     */
    $stateProvider.state('home.admin.centres', {
      url: '/centres',
      views: {
        'main@': {
          template: '<centres-list></centres-list>'
        }
      },
      data: {
        displayName: 'Centres'
      }
    });

    /**
     * Centre add
     */
    $stateProvider.state('home.admin.centres.add', {
      url: '/add',
      views: {
        'main@': {
          template: '<centre-add></centre-add>'
        }
      },
      data: {
        displayName: 'Add centre'
      }
    });

    /**
     * Centre view
     */
    $stateProvider.state('home.admin.centres.centre', {
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
      },
      data: {
        breadcrumProxy: 'home.admin.centres.centre.summary'
      }
    });

    /**
     * Centre view summary information
     */
    $stateProvider.state('home.admin.centres.centre.summary', {
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
      },
      data: {
        displayName: '{{centre.name}}'
      }
    });

    /**
     * Centre view location information
     */
    $stateProvider.state('home.admin.centres.centre.locations', {
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
      },
      data: {
        displayName: '{{centre.name}}'
      }
    });

    /**
     * Used to add a centre location.
     */
    $stateProvider.state('home.admin.centres.centre.locations.locationAdd', {
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
      },
      data: {
        displayName: 'Add location'
      }
    });

    /**
     * Used to add a centre location.
     */
    $stateProvider.state('home.admin.centres.centre.locations.locationView', {
      url: '/location/view/:uniqueId',
      resolve: {
        location: [
          '$stateParams',
          'centre',
          function ($stateParams, centre) {
            return _.findWhere(centre.locations, { uniqueId: $stateParams.uniqueId });
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
      },
      data: {
        displayName: 'Location: {{location.name}}'
      }
    });

    /**
     * Centre view studies information
     */
    $stateProvider.state('home.admin.centres.centre.studies', {
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
      },
      data: {
        displayName: '{{centre.name}}'
      }
    });
  }

  return centreStates;
});
