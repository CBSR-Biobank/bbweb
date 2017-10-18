/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */

/* @ngInject */
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
        'main@': 'membershipAdmin'
      }
    })
    .state('home.admin.users.memberships.add', {
      url: '/add',
      views: {
        'main@': 'membershipAdd'
      }
    })
    .state('home.admin.users.memberships.membership', {
      url: '/{membershipId}',
      resolve: {
        membership: resolveMembership
      },
      views: {
        'main@': 'membershipView'
      }
    });

  /* @ngInject */
  function resolveUser($transition$, User) {
    return User.get($transition$.params().userId);
  }

  /* @ngInject */
  function resolveMembership($transition$, Membership) {
    return Membership.get($transition$.params().membershipId);
  }

}

export default ngModule => ngModule.config(config)
