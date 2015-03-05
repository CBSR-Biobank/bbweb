define(['./module', 'underscore'], function(module, _) {
  'use strict';

  module.factory('User', UserFactory);

  UserFactory.$inject = [
    'validationService',
    'ConcurrencySafeEntity',
    'UserStatus',
    'usersService'
  ];

  /**
   *
   */
  function UserFactory(validationService,
                       ConcurrencySafeEntity,
                       UserStatus,
                       usersService) {

    var checkObject = validationService.checker(
      validationService.validator('must be a map', validationService.aMap),
      validationService.hasKeys('id', 'name', 'email', 'status')
    );

    function createUser(obj) {
      var checks = checkObject(obj);
      if (checks.length) {
        throw new Error('invalid object from server: ' + checks.join(', '));
      }
      return new User(obj);
    }

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

    User.list = function(options) {
      options = options || {};
      return usersService.list(options).then(function(reply) {
        // reply is a paged result
        reply.items = _.map(reply.items, function(obj){
          return createUser(obj);
        });
        return reply;
      });
    };

    User.get = function(id) {
      return usersService.get(id).then(function(reply) {
        return createUser(reply);
      });
    };

    User.prototype.addOrUpdate = function () {
      var self = this;
      return usersService.addOrUpdate(self).then(function(reply) {
        self.id = reply.id;
        self.name = reply.name;

        if (_.isUndefined(reply.version)) {
          self.version = 0;
        } else {
          self.version = reply.version;
        }
        return self;
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
