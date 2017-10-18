/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function() {
  'use strict';

  /**
   * Communicates with the server to get user related information and perform user related commands.
   */
  /* @ngInject */
  function usersServiceFactory($q,
                               $cookies,
                               $log,
                               biobankApi,
                               User,
                               UrlService) {
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

      biobankApi.get(UrlService.url('users/authenticate'))
        .then((user) => {
          currentUser = User.create(user);
          $log.info('Welcome back, ' + currentUser.name);
        })
        .catch(() => {
          /* the token is no longer valid */
          $log.info('Token no longer valid, please log in.');
          currentUser = undefined;
          $cookies.remove('XSRF-TOKEN');
        });
    }

    function retrieveCurrentUser() {
      return biobankApi.get(UrlService.url('users/authenticate'))
        .then((user) => {
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
      return biobankApi.post(UrlService.url('users/login'), credentials)
        .then((user) => {
          currentUser = User.create(user);
          $log.info('Welcome ' + currentUser.name);
          return currentUser;
        });
    }

    function logout() {
      return biobankApi.post(UrlService.url('users/logout')).then(() => {
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
      return biobankApi.post(UrlService.url('users/passreset'), { email: email });
    }

  }

  return usersServiceFactory;
});
