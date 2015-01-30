define(['./module'], function(module) {
  'use strict';

  module.provider('authorization', authorizationService);

  //authorizationService.$inject = [];

  /**
   *
   *
   * Implementation borrowed from book:
   *
   * Mastering Web Application Development with AngularJS
   */
  function authorizationService() {
    var service = {
      requireAuthenticatedUser: requireAuthenticatedUser,
      requireAdminUser: requireAdminUser,
      $get: authorizationServiceFactory
    };

    //-------

    requireAuthenticatedUser.$inject = ['authorization'];
    function requireAuthenticatedUser(authorization) {
      return authorization.requireAuthenticatedUser();
    }

    requireAdminUser.$inject = ['authorization'];
    function requireAdminUser(authorization) {
      return authorization.requireAdminUser();
    }

    authorizationServiceFactory.$inject = ['$q', 'usersService'];
    function authorizationServiceFactory($q, usersService) {
      var factoryService = {
        requireAuthenticatedUser: function () {
          var promise = usersService.requestCurrentUser().then(function () {
            if (!usersService.isAuthenticated()) {
              return $q.reject('user is not logged in');
            }
          });
          return promise;
        },
        requireAdminUser: function () {
          var promise = usersService.requestCurrentUser().then(function () {
            if (!usersService.isAdmin()) {
              return $q.reject('user is not an administrator');
            }
          });
          return promise;
        }
      };
      return factoryService;
    }

    return service;
  }

});
