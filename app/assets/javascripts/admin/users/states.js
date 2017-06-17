/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  config.$inject = [ '$stateProvider' ];

  function config($stateProvider) {

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
          component: 'userAdmin'
        }
      }
    });

  }

  return config;
});
