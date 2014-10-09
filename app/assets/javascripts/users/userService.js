define(['./module'], function(module) {
  'use strict';

  module.service('userService', UserService);

  UserService.$inject = ['$q', '$cookies', '$log', 'biobankXhrReqService'];

  /**
   * Communicates with the server to get user related information and perform user related commands.
   */
  function UserService($q, $cookies, $log, biobankXhrReqService) {
    var self = this;
    self.user = undefined;
    self.token = $cookies['XSRF-TOKEN'];

    var service = {
      login:         login,
      logout:        logout,
      getUser:       getUser,
      query:         query,
      getAllUsers:   getAllUsers,
      getUsers:      getUsers,
      add:           add,
      update:        update,
      passwordReset: passwordReset,
      activate:      activate,
      lock:          lock,
      unlock:        unlock
    };

    init();
    return service;

    //-------

    /* If the token is assigned, check that the token is still valid on the server */
    function init() {
      if (self.token) {
        biobankXhrReqService.call('GET', '/authenticate').then(
          function(user) {
            self.user = user;
            $log.info('Welcome back, ' + self.user.name);
          },
          function() {
            /* the token is no longer valid */
            $log.info('Token no longer valid, please log in.');
            self.token = undefined;
            delete $cookies['XSRF-TOKEN'];
            return $q.reject('Token invalid');
          });
      }
    }

    function changeStatus(user, status) {
      var cmd = {
        id: user.id,
        expectedVersion: user.version
      };
      return biobankXhrReqService.call('POST', '/users/' + status, cmd);
    }

    function login(credentials) {
      return biobankXhrReqService.call('POST', '/login', credentials)
        .then(function(token) {
          self.token = token;
          return biobankXhrReqService.call('GET', '/authenticate');
        }).then(function(user) {
          self.user = user;
          $log.info('Welcome ' + self.user.name);
          return self.user;
        });
    }

    function logout() {
      return biobankXhrReqService.call('POST', '/logout').then(function() {
        $log.info('Good bye');
        delete $cookies['XSRF-TOKEN'];
        self.token = undefined;
        self.user = undefined;
      });
    }

    function getUser() {
      return self.user;
    }

    function query(userId) {
      return biobankXhrReqService.call('GET', '/users/' + userId);
    }

    function getAllUsers() {
      return biobankXhrReqService.call('GET', '/users');
    }

    function getUsers(query, sort, order) {
      return biobankXhrReqService.call(
        'GET',
        '/users?' + query + '&sort=' + sort + '&order=' + order);
    }

    function add(newUser) {
      var cmd = {
        name:     newUser.name,
        email:    newUser.email,
        password: newUser.password
      };
      if (newUser.avatarUrl) {
        cmd.avatarUrl = newUser.avatarUrl;
      }
      return biobankXhrReqService.call('POST', '/users', cmd);
    }

    function update(user, newPassword) {
      var cmd = {
        expectedVersion: user.version,
        name:            user.name,
        email:           user.email
      };

      if (user.password) {
        cmd.password = newPassword;
      }

      if (user.avatarUrl) {
        cmd.avatarUrl = user.avatarUrl;
      }
      return biobankXhrReqService.call('PUT', '/users/' + user.id, cmd);
    }

    function passwordReset(email) {
      return biobankXhrReqService.call('POST', '/passreset', { email: email });
    }

    function activate(user) {
      return changeStatus(user, 'activate');
    }

    function lock(user) {
      return changeStatus(user, 'lock');
    }

    function unlock(user) {
      return changeStatus(user, 'unlock');
    }
  }

});
