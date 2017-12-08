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
      url: '/:slug',
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
      url: '/:slug',
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
      .catch(resourceErrorService.goto404(`memberhsip slug not found: ${slug}`))
  }

}

export default ngModule => ngModule.config(config)
