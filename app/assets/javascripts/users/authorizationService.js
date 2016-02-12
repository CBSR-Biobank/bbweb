/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

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
            var deferred = $q.defer();
            if (usersService.isAuthenticated()) {
              deferred.resolve(currentUser);
            } else {
              deferred.reject('user is not logged in');
            }
            return deferred.promise;
          });
        },
        requireAdminUser: function () {
          return usersService.requestCurrentUser().then(function (currentUser) {
            var deferred = $q.defer();
            if (usersService.isAdmin()) {
              deferred.resolve(currentUser);
            } else {
              deferred.reject('user is not an administrator');
            }
            return deferred.promise;
          });
        }
      };
      return factoryService;
    }

    return service;
  }

  return authorizationService;
});
