define(['../module', 'underscore', 'moment'], function(module, _, moment) {
  'use strict';

  module.factory('User', UserFactory);

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

    var requiredKeys = ['id', 'name', 'email'];

    var objRequiredKeys = requiredKeys.concat('status');

    var validateIsMap = validationService.condition1(
      validationService.validator('must be a map', _.isObject));

    var createObj = funutils.partial1(validateIsMap, _.identity);

    var validateObj = funutils.partial1(
      validationService.condition1(
        validationService.validator('has the correct keys',
                                    validationService.hasKeys.apply(null, objRequiredKeys))),
      createObj);

    var validateRegisteredEvent = funutils.partial1(
      validationService.condition1(
        validationService.validator('has the correct keys',
                                    validationService.hasKeys.apply(null, requiredKeys))),
      createObj);

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

    User.create = function (obj) {
      var validation = validateObj(obj);
      if (!_.isObject(validation)) {
        throw new Error('invalid object: ' + validation);
      }
      return new User(obj);
    };

    /**
     * Meant to be called from a promise chain, therefore it does not throw and error but returns one.
     */
    User.createFromEvent = function (event) {
      var validation = validateRegisteredEvent(event);
      if (!_.isObject(validation)) {
        return new Error('invalid event: ' + validation);
      }
      return new User(event);
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

    User.get = function(id) {
      return usersService.query(id).then(function(reply) {
        return User.create(reply);
      });
    };

    User.prototype.register = function (password) {
      var self = this;

      return usersService.add(self, password).then(function(event) {
        return User.createFromEvent(event);
      });
    };

    User.prototype.updateName = function (name) {
      var self = this;

      return usersService.updateName(self, name).then(function(reply) {
        return new User(_.extend(_.pick(self, 'email', 'avatarUrl'), reply));
      });
    };

    User.prototype.updateEmail = function (email) {
      var self = this;
      return usersService.updateEmail(self, email).then(function(reply) {
        return new User(_.extend(_.pick(self, 'name', 'avatarUrl'), reply));
      });
    };

    User.prototype.updatePassword = function (currentPassword, newPassword) {
      var self = this;
      return usersService.updatePassword(self, currentPassword, newPassword).then(function(reply) {
        return new User(_.extend(_.pick(self, 'name', 'email', 'avatarUrl'), reply));
      });
    };

    User.prototype.updateAvatarUrl = function (avatarUrl) {
      var self = this;
      return usersService.updateAvatarUrl(self, avatarUrl).then(function(reply) {
        return new User(_.extend(_.pick(self, 'name', 'email'), reply));
      });
    };

    User.prototype.activate = function () {
      var self = this;

      if (self.status !== UserStatus.REGISTERED()) {
        throw new Error('user status is not registered: ' + self.status);
      }

      return usersService.activate(self).then(function(reply) {
        return new User(_.extend(_.pick(self, 'id', 'name', 'email'), { status: UserStatus.ACTIVE() }));
      });
    };

    User.prototype.lock = function () {
      var self = this;

      if (self.status !== UserStatus.ACTIVE()) {
        throw new Error('user status is not active: ' + self.status);
      }

      return usersService.lock(self).then(function(reply) {
        return new User(_.extend(_.pick(self, 'id', 'name', 'email'), { status: UserStatus.LOCKED() }));
      });
    };

    User.prototype.unlock = function () {
      var self = this;

      if (self.status !== UserStatus.LOCKED()) {
        throw new Error('user status is not locked: ' + self.status);
      }

      return usersService.unlock(self).then(function(reply) {
        return new User(_.extend(_.pick(self, 'id', 'name', 'email'), { status: UserStatus.ACTIVE() }));
      });
    };

    return User;
  }

});
