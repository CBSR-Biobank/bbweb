define(['./module', 'jquery'], function(module, jquery) {
  'use strict';

  module.service('usersService', UsersService);

  UsersService.$inject = ['$q', '$cookies', '$log', 'biobankXhrReqService'];

  /**
   * Communicates with the server to get user related information and perform user related commands.
   */
  function UsersService($q, $cookies, $log, biobankXhrReqService) {
    var self = this;
    self.user = undefined;
    self.token = $cookies['XSRF-TOKEN'];

    var service = {
      login:           login,
      logout:          logout,
      getUser:         getUser,
      query:           query,
      getAllUsers:     getAllUsers,
      getUsers:        getUsers,
      add:             add,
      updateName:      updateName,
      updateEmail:     updateEmail,
      updatePassword:  updatePassword,
      updateAvatarUrl: updateAvatarUrl,
      passwordReset:   passwordReset,
      activate:        activate,
      lock:            lock,
      unlock:          unlock
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

    function uri(userId) {
      var result = '/users';
      if (arguments.length > 0) {
        result += '/' + userId;
      }
      return result;
    }

    function changeStatus(user, status) {
      var cmd = {
        id: user.id,
        expectedVersion: user.version
      };
      return biobankXhrReqService.call('POST', uri(user.id) + '/' + status, cmd);
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
      return biobankXhrReqService.call('GET', uri(userId));
    }

    function getAllUsers() {
      return biobankXhrReqService.call('GET', uri());
    }

    function getUsers(query, sort, order) {
      if (arguments.length > 0) {
        var params = {};

        if (query) {
          params.query = query;
        }

        if ((arguments.length > 1) && sort) {
          params.sort = sort;
        }

        if ((arguments.length > 2) && order) {
          params.order = order;
        }

        return biobankXhrReqService.call('GET', uri() + '?' + jquery.param(params));
      } else {
        return biobankXhrReqService.call('GET', uri());
      }
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
      return biobankXhrReqService.call('POST', uri(), cmd);
    }

    function updateName(user, newName) {
      var cmd = {
        id:              user.id,
        expectedVersion: user.version,
        name:            newName
      };
      return biobankXhrReqService.call('PUT', uri(user.id) + '/name', cmd);
    }

    function updateEmail(user, newEmail) {
      var cmd = {
        id:              user.id,
        expectedVersion: user.version,
        email:           newEmail
      };
      return biobankXhrReqService.call('PUT', uri(user.id) + '/email', cmd);
    }

    function updatePassword(user, currentPassword, newPassword) {
      var cmd = {
        id:              user.id,
        expectedVersion: user.version,
        currentPassword: currentPassword,
        newPassword:     newPassword
      };
      return biobankXhrReqService.call('PUT', uri(user.id) + '/password', cmd);
    }

    function updateAvatarUrl(user, avatarUrl) {
      var cmd = {
        id:              user.id,
        expectedVersion: user.version,
        avatarUrl:       avatarUrl
      };
      return biobankXhrReqService.call('PUT', uri(user.id) + '/avatarurl', cmd);
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
