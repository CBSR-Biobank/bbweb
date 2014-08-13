/**
 * User service, exposes user model to the rest of the app.
 */
define(['angular', 'common'], function(angular) {
  'use strict';

  var mod = angular.module('users.services', ['biobank.common', 'ngCookies']);

  mod.factory('userService', [
    '$http', '$q', '$cookies', '$log',
    function($http, $q, $cookies, $log) {
    var user, token = $cookies['XSRF-TOKEN'];

    /* If the token is assigned, check that the token is still valid on the server */
    if (token) {
      $http.get('/authuser')
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
        return $http.post('/login', credentials).then(function(response) {
          token = response.data.token;
          return $http.get('/authuser');
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
        return $http.post('/logout').then(function(response) {
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
    user: ['$cookies', '$q', '$http', 'userService', function($cookies, $q, $http, userService) {
      var token;
      var deferred = $q.defer();
      var user = userService.getUser();

      if (user) {
        deferred.resolve(user);
      } else {
        token = $cookies['XSRF-TOKEN'];

        if (token) {
          $http.get('/authuser')
            .success(function(data) {
              deferred.resolve(data);
            })
            .error(function() {
              deferred.reject();
            });
        } else {
          deferred.reject();
        }
      }
      return deferred.promise;
    }]
  });
  /**
   * If the current route does not resolve, go back to the start page.
   */
  var handleRouteError = function($rootScope, $state) {
    $rootScope.$on('$routeChangeError', function(e, next, current) {
      $state.go('home');
    });
  };
  handleRouteError.$inject = ['$rootScope', '$state'];
  mod.run(handleRouteError);
  return mod;
});
