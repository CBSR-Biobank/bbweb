/**
 * Returns the currently logged in user extracted from the XSRF-TOKEN cookie.
 */
define(['./module'], function(module) {
  'use strict';

  /**
   * Add this object to a state's resolve attibute to only allow the state transition if the user is
   * logged in. This also adds the contents of the object as a dependency of the controller.
   */
  module.constant('userResolve', { user: getUser });

  getUser.$inject = ['$cookies', '$q', 'usersService', 'biobankXhrReqService'];

  function getUser($cookies, $q, usersService, biobankXhrReqService) {
    var deferred = $q.defer();
    var user = usersService.getUser();
    var token;

    if (user) {
      deferred.resolve(user);
    } else {
      token = $cookies['XSRF-TOKEN'];

      if (token) {
        biobankXhrReqService.call('GET', '/authenticate').then(
          function(user) {
            deferred.resolve(user);
          },
          function(error) {
            deferred.reject(error);
          });
      } else {
        deferred.reject();
      }
    }
    return deferred.promise;
  }

});
