define(['../module', 'underscore'], function(module, _) {
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
        throw new Error('invalid object from server: ' + validation);
      }
      return new User(obj);
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
      return usersService.add(self, password).then(function(reply) {
        var validation = validateRegisteredEvent(reply);

        if (!_.isObject(validation)) {
          throw new Error('invalid event from server: ' + validation);
        }

        return new User(_.extend({}, reply, { version: 0 }));
      });
    };

    User.prototype.activate = function () {
      var self = this;
      return usersService.activate(self).then(function(reply) {
        self.status = UserStatus.ACTIVE();
        self.version = reply.version;
        return self;
      });
    };

    User.prototype.lock = function () {
      var self = this;
      return usersService.lock(self).then(function(reply) {
        self.status = UserStatus.LOCKED();
        self.version = reply.version;
        return self;
      });
    };

    User.prototype.unlock = function () {
      var self = this;
      return usersService.unlock(self).then(function(reply) {
        self.status = UserStatus.ACTIVE();
        self.version = reply.version;
        return self;
      });
    };

    User.prototype.updateName = function (name) {
      var self = this;
      return usersService.updateName(self).then(function(reply) {
        self.name = reply.name;
        self.version = reply.version;
        return self;
      });
    };

    User.prototype.updateEmail = function (email) {
      var self = this;
      return usersService.updateName(self).then(function(reply) {
        self.email = reply.email;
        self.version = reply.version;
        return self;
      });
    };

    User.prototype.updatePassword = function (password) {
      var self = this;
      return usersService.updateName(self).then(function(reply) {
        self.version = reply.version;
        return self;
      });
    };

    User.prototype.updateAvatarUrl = function (avatarUrl) {
      var self = this;
      return usersService.updateName(self).then(function(reply) {
        self.avatarUrl = reply.avatarUrl;
        self.version = reply.version;
        return self;
      });
    };

    return User;
  }

});
