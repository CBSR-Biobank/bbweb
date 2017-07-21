/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function() {
  'use strict';

  usersServiceFactory.$inject = [
    '$q',
    '$cookies',
    '$log',
    'biobankApi',
    'User'
  ];

  /**
   * Communicates with the server to get user related information and perform user related commands.
   */
  function usersServiceFactory($q,
                               $cookies,
                               $log,
                               biobankApi,
                               User) {
    var currentUser;

    var service = {
      getCurrentUser:      getCurrentUser,
      retrieveCurrentUser: retrieveCurrentUser,
      requestCurrentUser:  requestCurrentUser,
      login:               login,
      logout:              logout,
      isAuthenticated:     isAuthenticated,
      sessionTimeout:      sessionTimeout,
      passwordReset:       passwordReset
    };

    init();
    return service;

    //-------

    /* If the token is assigned, check that the token is still valid on the server */
    function init() {
      var token = $cookies.get('XSRF-TOKEN');

      if (!token) { return; }

      biobankApi.get('/users/authenticate')
        .then(function(user) {
          currentUser = User.create(user);
          $log.info('Welcome back, ' + currentUser.name);
        })
        .catch(function() {
          /* the token is no longer valid */
          $log.info('Token no longer valid, please log in.');
          currentUser = undefined;
          $cookies.remove('XSRF-TOKEN');
        });
    }

    function retrieveCurrentUser() {
      return biobankApi.get('/users/authenticate').then(function(user) {
        currentUser = User.create(user);
        return currentUser;
      });
    }

    function requestCurrentUser() {
      if (isAuthenticated()) {
        return $q.when(currentUser);
      }
      return retrieveCurrentUser();
    }

    function getCurrentUser() {
      return currentUser;
    }

    function isAuthenticated() {
      return !!currentUser;
    }

    function login(credentials) {
      return biobankApi.post('/users/login', credentials)
        .then(function(user) {
          currentUser = user;
          $log.info('Welcome ' + currentUser.name);
          return currentUser;
        });
    }

    function logout() {
      return biobankApi.post('/users/logout').then(function() {
        $log.info('Good bye');
        $cookies.remove('XSRF-TOKEN');
        currentUser = undefined;
      });
    }

    function sessionTimeout() {
      $cookies.remove('XSRF-TOKEN');
      currentUser = undefined;
    }

    function passwordReset(email) {
      return biobankApi.post('/users/passreset', { email: email });
    }

  }

  return usersServiceFactory;
});
