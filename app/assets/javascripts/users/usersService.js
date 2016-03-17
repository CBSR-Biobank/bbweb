/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function(_) {
  'use strict';

  usersServiceFactory.$inject = ['$q', '$cookies', '$log', 'biobankApi', 'queryStringService'];

  /**
   * Communicates with the server to get user related information and perform user related commands.
   */
  function usersServiceFactory($q,
                               $cookies,
                               $log,
                               biobankApi,
                               queryStringService) {
    var currentUser = null;
    var token = $cookies['XSRF-TOKEN'];

    var service = {
      getCurrentUser:     getCurrentUser,
      requestCurrentUser: requestCurrentUser,
      login:              login,
      logout:             logout,
      isAuthenticated:    isAuthenticated,
      isAdmin:            isAdmin,
      getUserCounts:      getUserCounts,
      passwordReset:      passwordReset
    };

    init();
    return service;

    //-------

    /* If the token is assigned, check that the token is still valid on the server */
    function init() {
      if (token) {
        biobankApi.get('/authenticate')
          .then(function(user) {
            currentUser = user;
            $log.info('Welcome back, ' + currentUser.name);
          })
          .catch(function() {
            /* the token is no longer valid */
            $log.info('Token no longer valid, please log in.');
            token = undefined;
            delete $cookies['XSRF-TOKEN'];
            return $q.reject('Token invalid');
          });
      }
    }

    function uri(userId) {
      var result = '/users';
      if (arguments.length > 0) {
        result += '/' + userId;
      }
      return result;
    }

    function requestCurrentUser() {
      if (isAuthenticated()) {
        return $q.when(currentUser);
      } else {
        return biobankApi.get('/authenticate').then(function(user) {
          currentUser = user;
          return currentUser;
        });
      }
    }

    function getCurrentUser() {
      return currentUser;
    }

    function isAuthenticated() {
      return !!currentUser;
    }

    function isAdmin() {
      // FIXME this needs to be implemented once completed on the server, for now just return true if logged in
      return !!currentUser;
    }

    function changeStatus(user, status) {
      var cmd = {
        id: user.id,
        expectedVersion: user.version
      };
      return biobankApi.post(uri(user.id) + '/' + status, cmd);
    }

    function login(credentials) {
      return biobankApi.post('/login', credentials)
        .then(function(reply) {
          token = reply;
          return biobankApi.get('/authenticate');
        })
        .then(function(user) {
          currentUser = user;
          $log.info('Welcome ' + currentUser.name);
          return currentUser;
        });
    }

    function logout() {
      return biobankApi.post('/logout').then(function() {
        $log.info('Good bye');
        delete $cookies['XSRF-TOKEN'];
        token = undefined;
        currentUser = undefined;
      });
    }

    function getUserCounts() {
      return biobankApi.get(uri() + '/counts');
    }

    function passwordReset(email) {
      return biobankApi.post('/passreset', { email: email });
    }

  }

  return usersServiceFactory;
});
