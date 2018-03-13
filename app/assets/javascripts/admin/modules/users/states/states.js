/**
 * UI Router states for {@link domain.users.User User} administration.
 *
 * @namespace admin.users.states
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 * UI Router states used for {@link domain.users.User User} Administration.
 *
 * @name admin.users.states.adminUsersUiRouterConfig
 * @function
 *
 * @param {AngularJS_Service} $stateProvider
 */
/* @ngInject */
function adminUsersUiRouterConfig($stateProvider) {
  $stateProvider
    .state('home.admin.access', {
      url: '/users',
      views: {
        'main@': 'userAdmin'
      }
    })
    .state('home.admin.access.users', {
      url: '/manage',
      views: {
        'main@': 'manageUsers'
      }
    })
    .state('home.admin.access.users.user', {
      url: '/:slug',
      resolve: {
        user: resolveUser
      },
      views: {
        'main@': 'userProfile'
      }
    })
    .state('home.admin.access.roles', {
      url: '/roles',
      views: {
        'main@': 'accessRoles'
      }
    })
    .state('home.admin.access.roles.role', {
      url: '/:slug',
      resolve: {
        role: resolveRole
      },
      views: {
        'main@': 'roleView'
      }
    })
    .state('home.admin.access.memberships', {
      url: '/memberships',
      views: {
        'main@': 'membershipAdmin'
      }
    })
    .state('home.admin.access.memberships.add', {
      url: '/add',
      views: {
        'main@': 'membershipAdd'
      }
    })
    .state('home.admin.access.memberships.membership', {
      url: '/:slug',
      resolve: {
        membership: resolveMembership
      },
      views: {
        'main@': 'membershipView'
      }
    });

  /* @ngInject */
  function resolveUser($transition$, User, resourceErrorService) {
    const slug = $transition$.params().slug
    return User.get(slug)
      .catch(resourceErrorService.goto404(`user slug not found: ${slug}`))
  }

  /* @ngInject */
  function resolveRole($transition$, Role, resourceErrorService) {
    const slug = $transition$.params().slug
    return Role.get(slug)
      .catch(resourceErrorService.goto404(`role slug not found: ${slug}`))
  }

  /* @ngInject */
  function resolveMembership($transition$, Membership, resourceErrorService) {
    const slug = $transition$.params().slug
    return Membership.get(slug)
      .catch(resourceErrorService.goto404(`membership slug not found: ${slug}`))
  }

}

export default ngModule => ngModule.config(adminUsersUiRouterConfig)
