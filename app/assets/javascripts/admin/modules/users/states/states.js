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
    .state('home.admin.users.roles.role', {
      url: '/{roleId}',
      resolve: {
        role: resolveRole
      },
      views: {
        'main@': 'roleView'
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
  function resolveUser($transition$, User, resourceErrorService) {
    const userId = $transition$.params().userId
    return User.get(userId)
      .catch(resourceErrorService.goto404(`user ID not found: ${userId}`))
  }

  /* @ngInject */
  function resolveMembership($transition$, Membership, resourceErrorService) {
    const id = $transition$.params().membershipId
    return Membership.get(id)
      .catch(resourceErrorService.goto404(`memberhsip ID not found: ${id}`))
  }

  /* @ngInject */
  function resolveRole($transition$, Role, resourceErrorService) {
    const id = $transition$.params().roleId
    return Role.get(id)
      .catch(resourceErrorService.goto404(`role ID not found: ${id}`))
  }

}

export default ngModule => ngModule.config(config)
