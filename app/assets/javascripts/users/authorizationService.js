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
          return usersService.requestCurrentUser().then(function (currentUser) {
            var defer = $q.defer();
            if (usersService.isAuthenticated()) {
              defer.resolve(currentUser);
            } else {
              defer.reject('user is not logged in');
            }
            return defer.promise;
          });
        },
        requireAdminUser: function () {
          return usersService.requestCurrentUser().then(function (currentUser) {
            var defer = $q.defer();
            if (usersService.isAdmin()) {
              defer.resolve(currentUser);
            } else {
              defer.reject('user is not an administrator');
            }
            return defer.promise;
          });
        }
      };
      return factoryService;
    }

    return service;
  }

});
