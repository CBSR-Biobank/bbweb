/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  config.$inject = [ '$stateProvider' ];

  function config($stateProvider) {
    $stateProvider
      .state('home.admin.users', {
        url: '/users',
        resolve: {
          userCounts: resolveUserCounts
        },
        views: {
          'main@': 'userAdmin'
        }
      })
      .state('home.admin.users.user', {
        url: '/:userId',
        resolve: {
          user: resolveUser
        },
        views: {
          'main@': 'userProfile'
        }
      })
      .state('home.admin.users.user.roles', {
        url: '/roles',
        views: {
          'main@': 'userRoles'
        }
      });

    resolveUserCounts.$inject = ['UserCounts'];
    function resolveUserCounts(UserCounts) {
      return UserCounts.get();
    }

    resolveUser.$inject = ['$transition$', 'User'];
    function resolveUser($transition$, User) {
      return User.get($transition$.params().userId);
    }

  }

  return config;
});
