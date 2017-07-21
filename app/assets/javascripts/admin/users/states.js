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
        views: {
          'main@': 'userAdmin'
        }
      })
      .state('home.admin.users.manage', {
        url: '/manage',
        views: {
          'main@': 'manageUsers'
        }
      })
      .state('home.admin.users.manage.user', {
        url: '/:userId',
        resolve: {
          user: resolveUser
        },
        views: {
          'main@': 'userProfile'
        }
      })
      .state('home.admin.users.roles', {
        url: '/roles',
        views: {
          'main@': 'userRoles'
        }
      })
      .state('home.admin.users.memberships', {
        url: '/memberships',
        views: {
          'main@': 'userMemberships'
        }
      });

    resolveUserCounts.$inject = ['$state', 'UserCounts'];
    function resolveUserCounts($state, UserCounts) {
      return UserCounts.get()
        .catch(function (error) {
          if (error.status && (error.status === 401)) {
            $state.go('home.users.login', {}, { reload: true });
          }
        });
    }

    resolveUser.$inject = ['$transition$', 'User'];
    function resolveUser($transition$, User) {
      return User.get($transition$.params().userId);
    }

  }

  return config;
});
