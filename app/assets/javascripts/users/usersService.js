define(['./module', 'jquery', 'underscore'], function(module, $, _) {
  'use strict';

  module.service('usersService', UsersService);

  UsersService.$inject = ['$q', '$cookies', '$log', 'biobankApi', 'queryStringService'];

  /**
   * Communicates with the server to get user related information and perform user related commands.
   */
  function UsersService($q,
                        $cookies,
                        $log,
                        biobankApi,
                        queryStringService) {
    var self = this;
    self.currentUser = null;
    self.token = $cookies['XSRF-TOKEN'];

    var service = {
      getCurrentUser:     getCurrentUser,
      requestCurrentUser: requestCurrentUser,
      login:              login,
      logout:             logout,
      isAuthenticated:    isAuthenticated,
      isAdmin:            isAdmin,
      query:              query,
      getAllUsers:        getAllUsers,
      getUserCounts:      getUserCounts,
      getUsers:           getUsers,
      add:                add,
      updateName:         updateName,
      updateEmail:        updateEmail,
      updatePassword:     updatePassword,
      updateAvatarUrl:    updateAvatarUrl,
      passwordReset:      passwordReset,
      activate:           activate,
      lock:               lock,
      unlock:             unlock
    };

    init();
    return service;

    //-------

    /* If the token is assigned, check that the token is still valid on the server */
    function init() {
      if (self.token) {
        biobankApi.call('GET', '/authenticate')
          .then(function(currentUser) {
            self.currentUser = currentUser;
            $log.info('Welcome back, ' + self.currentUser.name);
          })
          .catch(function() {
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

    function requestCurrentUser() {
      if (isAuthenticated()) {
        return $q.when(self.currentUser);
      } else {
        return biobankApi.call('GET', '/authenticate').then(function(currentUser) {
          self.currentUser = currentUser;
          return self.currentUser;
        });
      }
    }

    function getCurrentUser() {
      return self.currentUser;
    }

    function isAuthenticated() {
      return !!self.currentUser;
    }

    function isAdmin() {
      // FIXME this needs to be implemented once completed on the server, for now just return true if logged in
      return !!self.currentUser;
    }

    function changeStatus(user, status) {
      var cmd = {
        id: user.id,
        expectedVersion: user.version
      };
      return biobankApi.call('POST', uri(user.id) + '/' + status, cmd);
    }

    function login(credentials) {
      return biobankApi.call('POST', '/login', credentials)
        .then(function(token) {
          self.token = token;
          return biobankApi.call('GET', '/authenticate');
        })
        .then(function(user) {
          self.currentUser = user;
          $log.info('Welcome ' + self.currentUser.name);
          return self.currentUser;
        });
    }

    function logout() {
      return biobankApi.call('POST', '/logout').then(function() {
        $log.info('Good bye');
        delete $cookies['XSRF-TOKEN'];
        self.token = undefined;
        self.currentUser = undefined;
      });
    }

    function query(userId) {
      return biobankApi.call('GET', uri(userId));
    }

    function getAllUsers() {
      return biobankApi.call('GET', uri());
    }

    function getUserCounts() {
      return biobankApi.call('GET', uri() + '/counts');
    }

    /**
     * @param {string} options.nameFilter The filter to use on user names. Default is empty string.
     *
     * @param {string} options.emailFilter The filter to use on user emails. Default is empty string.
     *
     * @param {string} options.status Returns users filtered by status. The following are valid: 'all' to
     * return all users, 'retired' to return only retired users, 'active' to reutrn only active
     * users, and 'locked' to return only locked users. For any other values the response is an error.
     *
     * @param {string} options.sortField Users can be sorted by 'name', 'email' or by 'status'. Values other
     * than these yield an error.
     *
     * @param {int} options.page If the total results are longer than pageSize, then page selects which
     * users should be returned. If an invalid value is used then the response is an error.
     *
     * @param {int} options.pageSize The total number of users to return per page. The maximum page size is
     * 10. If a value larger than 10 is used then the response is an error.
     *
     * @param {string} options.order One of 'asc' or 'desc'. If an invalid value is used then
     * the response is an error.
     *
     * @return A promise. If the promise succeeds then a paged result is returned.
     */
    function getUsers(options) {
      var validKeys = [
        'nameFilter',
        'emailFilter',
        'status',
        'sort',
        'page',
        'pageSize',
        'order'
      ];
      var url = uri();
      var paramsStr = '';

      if (arguments.length) {
        paramsStr = queryStringService.param(options, function (value, key) {
          return _.contains(validKeys, key);
        });
      }

      if (paramsStr) {
        url += paramsStr;
      }

      return biobankApi.call('GET', url);
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
      return biobankApi.call('POST', uri(), cmd);
    }

    function updateName(user, newName) {
      var cmd = {
        id:              user.id,
        expectedVersion: user.version,
        name:            newName
      };
      return biobankApi.call('PUT', uri(user.id) + '/name', cmd);
    }

    function updateEmail(user, newEmail) {
      var cmd = {
        id:              user.id,
        expectedVersion: user.version,
        email:           newEmail
      };
      return biobankApi.call('PUT', uri(user.id) + '/email', cmd);
    }

    function updatePassword(user, currentPassword, newPassword) {
      var cmd = {
        id:              user.id,
        expectedVersion: user.version,
        currentPassword: currentPassword,
        newPassword:     newPassword
      };
      return biobankApi.call('PUT', uri(user.id) + '/password', cmd);
    }

    function updateAvatarUrl(user, avatarUrl) {
      var cmd = {
        id:              user.id,
        expectedVersion: user.version,
        avatarUrl:       avatarUrl
      };
      return biobankApi.call('PUT', uri(user.id) + '/avatarurl', cmd);
    }

    function passwordReset(email) {
      return biobankApi.call('POST', '/passreset', { email: email });
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
