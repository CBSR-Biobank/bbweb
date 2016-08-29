/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  config.$inject = [
    '$urlRouterProvider',
    '$stateProvider'
  ];

  function config($urlRouterProvider, $stateProvider) {

    $urlRouterProvider.otherwise('/');

    resolveUserCounts.$inject = ['UserCounts'];
    function resolveUserCounts(UserCounts) {
      return UserCounts.get();
    }

    /**
     * Displays all users in a table
     */
    $stateProvider.state('home.admin.users', {
      url: '/users',
      resolve: {
        userCounts: resolveUserCounts
      },
      views: {
        'main@': {
          template: '<user-admin></user-admin>'
        }
      },
      data: {
        displayName: 'Users'
      }
    });

  }

  return config;
});
