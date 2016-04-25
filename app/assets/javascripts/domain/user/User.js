/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['underscore', 'tv4'], function(_, tv4) {
  'use strict';

  UserFactory.$inject = [
    '$q',
    'funutils',
    'biobankApi',
    'ConcurrencySafeEntity',
    'UserStatus',
    'usersService'
  ];

  /**
   *
   */
  function UserFactory($q,
                       funutils,
                       biobankApi,
                       ConcurrencySafeEntity,
                       UserStatus,
                       usersService) {

    var schema = {
      'id': 'User',
      'type': 'object',
      'properties': {
        'id':              { 'type': 'string' },
        'version':         { 'type': 'integer', 'minimum': 0 },
        'timeAdded':       { 'type': 'string' },
        'timeModified':    { 'type': [ 'string', 'null' ] },
        'name':            { 'type': 'string' },
        'email':           { 'type': 'string' },
        'avatarUrl':       { 'type': [ 'string', 'null' ] },
        'status':          { 'type': 'string' }
      },
      'required': [ 'id', 'version', 'timeAdded', 'name', 'email', 'status' ]
    };

    function User(obj) {
      var defaults = {
        name:      '',
        email:     '',
        avatarUrl: null,
        status:    UserStatus.REGISTERED()
      };

      ConcurrencySafeEntity.call(this, obj);
      obj = obj || {};
      _.extend(this, defaults, _.pick(obj, _.keys(defaults)));
      this.statusLabel = UserStatus.label(this.status);
    }

    User.prototype = Object.create(ConcurrencySafeEntity.prototype);
    User.prototype.constructor = User;

    /**
     * Used by promise code, so it must return an error rather than throw one.
     */
    User.create = function (obj) {
      if (!tv4.validate(obj, schema)) {
        console.error('invalid object from server: ' + tv4.error);
        throw new Error('invalid object from server: ' + tv4.error);
      }
      return new User(obj);
    };

    User.get = function(id) {
      return biobankApi.get(uri(id)).then(User.prototype.asyncCreate);
    };

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
    User.list = function(options) {
      var validKeys = [ 'nameFilter',
                        'emailFilter',
                        'status',
                        'sort',
                        'page',
                        'pageSize',
                        'order'
                      ],
          url = uri(),
          params;

      options = options || {};
      params = _.pick(options, validKeys);

      console.log(params);

      return biobankApi.get(url, params).then(function(reply) {
        // reply is a paged result
        var deferred = $q.defer();
        try {
          reply.items = _.map(reply.items, function(obj){
            return User.create(obj);
          });
          deferred.resolve(reply);
        } catch (e) {
          deferred.reject('invalid users from server');
        }
        return deferred.promise;
      });
    };

    User.prototype.asyncCreate = function (obj) {
      var deferred = $q.defer();

      if (!tv4.validate(obj, schema)) {
        console.error('invalid object from server: ' + tv4.error);
        deferred.reject('invalid object from server: ' + tv4.error);
      } else {
        deferred.resolve(new User(obj));
      }

      return deferred.promise;
    };

    User.prototype.register = function (password) {
      var json = {
        name:      this.name,
        email:     this.email,
        password:  password,
        avatarUrl: this.avatarUrl
      };
      return biobankApi.post(uri(), json)
        .then(User.prototype.asyncCreate);
    };

    User.prototype.updateName = function (name) {
      return ConcurrencySafeEntity.prototype.update.call(
        this, updateUri('name', this.id), { name: name });
    };

    User.prototype.updateEmail = function (email) {
      return ConcurrencySafeEntity.prototype.update.call(
        this, updateUri('email', this.id), { email: email });
    };

    User.prototype.updatePassword = function (currentPassword, newPassword) {
      return ConcurrencySafeEntity.prototype.update.call(
        this, updateUri('password', this.id),
        {
          currentPassword: currentPassword,
          newPassword:     newPassword
        });
    };

    User.prototype.updateAvatarUrl = function (avatarUrl) {
      return ConcurrencySafeEntity.prototype.update.call(
        this, updateUri('avatarurl', this.id), { avatarUrl: avatarUrl });
    };

    User.prototype.activate = function () {
      var self = this;

      if (self.status !== UserStatus.REGISTERED()) {
        throw new Error('user status is not registered: ' + self.status);
      }

      return changeStatus(this, 'activate');
    };

    User.prototype.lock = function () {
      var self = this;

      if (self.status !== UserStatus.ACTIVE()) {
        throw new Error('user status is not active: ' + self.status);
      }

      return changeStatus(this, 'lock');
    };

    User.prototype.unlock = function () {
      var self = this;

      if (self.status !== UserStatus.LOCKED()) {
        throw new Error('user status is not locked: ' + self.status);
      }

      return changeStatus(this, 'unlock');
    };

    User.prototype.isRegistered = function () {
      return (this.status === UserStatus.REGISTERED());
    };

    User.prototype.isActive = function () {
      return (this.status === UserStatus.ACTIVE());
    };

    User.prototype.isLocked = function () {
      return (this.status === UserStatus.LOCKED());
    };

    function changeStatus(user, status) {
      var json = {
        id:              user.id,
        expectedVersion: user.version
      };
      return biobankApi.post(uri(user.id) + '/' + status, json)
        .then(User.prototype.asyncCreate);
    }

    function uri(userId) {
      var result = '/users';
      if (arguments.length > 0) {
        result += '/' + userId;
      }
      return result;
    }

    function updateUri(/* path, userId */) {
      var result = '/users',
          args = _.toArray(arguments),
          path,
          userId;

      if (args.length > 0) {
        path = args.shift();
        result += '/' + path;
      }

      if (args.length > 0) {
        userId = args.shift();
        result += '/' + userId;
      }
      return result;
    }

    return User;
  }

  return UserFactory;
});
