define(['underscore'], function(_) {
  'use strict';

  UserFactory.$inject = [
    'funutils',
    'validationService',
    'ConcurrencySafeEntity',
    'UserStatus',
    'usersService'
  ];

  /**
   *
   */
  function UserFactory(funutils,
                       validationService,
                       ConcurrencySafeEntity,
                       UserStatus,
                       usersService) {

    var requiredKeys = ['id', 'name', 'email', 'status', 'version'];

    var validateObj = funutils.partial(
      validationService.condition1(
        validationService.validator('must be a map', _.isObject),
        validationService.validator('has the correct keys',
                                    validationService.hasKeys.apply(null, requiredKeys))),
      _.identity);

    function User(obj) {
      obj = obj || {};

      ConcurrencySafeEntity.call(this, obj);

      _.extend(this, _.defaults(obj, {
        name:      '',
        email:     '',
        avatarUrl: null,
        status:    UserStatus.REGISTERED()
      }));
    }

    User.prototype = Object.create(ConcurrencySafeEntity.prototype);

    /**
     * Used by promise code, so it must return an error rather than throw one.
     */
    User.create = function (obj) {
      var validation = validateObj(obj);
      if (!_.isObject(validation)) {
        return new Error('invalid object: ' + validation);
      }
      return new User(obj);
    };

    User.get = function(id) {
      return usersService.query(id).then(function(reply) {
        return User.create(reply);
      });
    };

    User.list = function(options) {
      options = options || {};
      return usersService.getUsers(options).then(function(reply) {
        // reply is a paged result
        reply.items = _.map(reply.items, function(obj){
          return User.create(obj);
        });
        return reply;
      });
    };

    User.prototype.register = function (password) {
      return usersService.add(this, password);
    };

    User.prototype.updateName = function (name) {
      var self = this;

      return usersService.updateName(self, name).then(function(reply) {
        return new User(reply);
      });
    };

    User.prototype.updateEmail = function (email) {
      var self = this;
      return usersService.updateEmail(self, email).then(function(reply) {
        return new User(reply);
      });
    };

    User.prototype.updatePassword = function (currentPassword, newPassword) {
      var self = this;
      return usersService.updatePassword(self, currentPassword, newPassword).then(function(reply) {
        return new User(reply);
      });
    };

    User.prototype.updateAvatarUrl = function (avatarUrl) {
      var self = this;
      return usersService.updateAvatarUrl(self, avatarUrl).then(function(reply) {
        return new User(reply);
      });
    };

    User.prototype.activate = function () {
      var self = this;

      if (self.status !== UserStatus.REGISTERED()) {
        throw new Error('user status is not registered: ' + self.status);
      }

      return usersService.activate(self).then(function(reply) {
        return new User(reply);
      });
    };

    User.prototype.lock = function () {
      var self = this;

      if (self.status !== UserStatus.ACTIVE()) {
        throw new Error('user status is not active: ' + self.status);
      }

      return usersService.lock(self).then(function(reply) {
        return new User(reply);
      });
    };

    User.prototype.unlock = function () {
      var self = this;

      if (self.status !== UserStatus.LOCKED()) {
        throw new Error('user status is not locked: ' + self.status);
      }

      return usersService.unlock(self).then(function(reply) {
        return new User(reply);
      });
    };

    return User;
  }

  return UserFactory;
});
