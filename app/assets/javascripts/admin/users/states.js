/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  config.$inject = [
    '$urlRouterProvider', '$stateProvider', 'authorizationProvider'
  ];

  function config($urlRouterProvider, $stateProvider, authorizationProvider) {

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
        user: authorizationProvider.requireAuthenticatedUser,
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
