/**
 * User service, exposes user model to the rest of the app.
 */
define(['angular', 'common'], function(angular) {
  'use strict';

  var mod = angular.module('user.services', ['biobank.common', 'ngCookies']);
  mod.factory('userService', ['$http', '$q', 'playRoutes', '$cookies', '$log', function($http, $q, playRoutes, $cookies, $log) {
    var user, token = $cookies['XSRF-TOKEN'];

    /* If the token is assigned, check that the token is still valid on the server */
    if (token) {
      playRoutes.controllers.UserController.authUser().get()
        .success(function(data) {
          $log.info('Welcome back, ' + data.name);
          user = data;
        })
        .error(function() {
          /* the token is no longer valid */
          $log.info('Token no longer valid, please log in.');
          token = undefined;
          delete $cookies['XSRF-TOKEN'];
          return $q.reject("Token invalid");
        });
    }

    return {
      loginUser: function(credentials) {
        return playRoutes.controllers.Application.login().post(credentials).then(function(response) {
          token = response.data.token;
          return playRoutes.controllers.UserController.authUser().get();
        }).then(function(response) {
          user = response.data;
          $log.info('Welcome ' + user.name);
          return user;
        });
      },
      logout: function() {
        // Logout on server in a real app
        delete $cookies['XSRF-TOKEN'];
        token = undefined;
        user = undefined;
        var dummyObj = {};
        return playRoutes.controllers.Application.logout().post().then(function(response) {
          $log.info("Good bye ");
        });
      },
      getUser: function() {
        return user;
      }
    };
  }]);
  /**
   * Add this object to a route definition to only allow resolving the route if the user is
   * logged in. This also adds the contents of the objects as a dependency of the controller.
   */
  mod.constant('userResolve', {
    user: ['$q', 'userService', function($q, userService) {
      var deferred = $q.defer();
      var user = userService.getUser();
      if (user) {
        deferred.resolve(user);
      } else {
        deferred.reject();
      }
      return deferred.promise;
    }]
  });
  /**
   * If the current route does not resolve, go back to the start page.
   */
  var handleRouteError = function($rootScope, $location) {
    $rootScope.$on('$routeChangeError', function(e, next, current) {
      $location.path('/');
    });
  };
  handleRouteError.$inject = ['$rootScope', '$location'];
  mod.run(handleRouteError);
  return mod;
});
