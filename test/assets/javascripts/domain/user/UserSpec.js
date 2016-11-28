/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var mocks   = require('angularMocks'),
      _       = require('lodash'),
      sprintf = require('sprintf').sprintf;

  describe('User', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(EntityTestSuiteMixin, ServerReplyMixin, extendedDomainEntities) {
      var self = this;

      _.extend(self, EntityTestSuiteMixin.prototype, ServerReplyMixin.prototype);

      self.injectDependencies('$httpBackend',
                              'User',
                              'UserState',
                              'factory');

      self.statusChangeShared = statusChangeShared;

      //--

      function statusChangeShared(user, statusChangePath, userMethod, newStatus) {
        var json = { id: user.id, expectedVersion: user.version };
        var reply = self.factory.user(user);

        self.$httpBackend.expectPOST(updateUri(statusChangePath, user.id), json)
          .respond(self.reply(reply));

        user[userMethod]().then(function (replyUser) {
          expect(replyUser).toEqual(jasmine.any(self.User));
        });
        self.$httpBackend.flush();
      }
    }));

    it('creating a user with no parameters has default values', function() {
      var user = new this.User();
      expect(user.id).toBeNull();
      expect(user.version).toBe(0);
      expect(user.timeAdded).toBeNull();
      expect(user.timeModified).toBeNull();
      expect(user.name).toBeEmptyString();
      expect(user.email).toBeEmptyString();
      expect(user.state).toBe(this.UserState.REGISTERED);
    });

    it('creating a user with an object does not modify object', function() {
      var obj = null, user = new this.User(obj);
      expect(user).toBeObject();
      expect(obj).toBeNull();
    });

    it('fails when creating from object with a non object', function() {
      var self = this,
          nonObj = 1;
      expect(function () {
        self.User.create(nonObj);
      }).toThrowError(/invalid object from server/);
    });

    it('fails when creating from object with missing required keys', function() {
      var self = this,
          obj = this.factory.user(),
          requiredKeys = ['id', 'name', 'email', 'state'];

      _.each(requiredKeys, function (key) {
        var badObj = _.omit(obj, key);

        expect(function () {
          self.User.create(badObj);
        }).toThrowError(/invalid object from server/);
      });
    });

    describe('when listing multiple users', function() {

      it('can list using when filtering by name only', function() {
        var self = this,
            filter = 'name::test',
            url = uri() + '?filter=' + filter,
            jsonUser = self.factory.user(),
            reply = self.factory.pagedResult([ jsonUser ]);

        self.$httpBackend.whenGET(url).respond(this.reply(reply));

        self.User.list({ filter: filter }).then(function(pagedResult) {
          expect(pagedResult.items).toBeArrayOfSize(1);
          expect(pagedResult.items[0]).toEqual(jasmine.any(self.User));
        });
        self.$httpBackend.flush();
      });

      it('can list when using filtering by email only', function() {
        var self = this,
            filter = 'email::test',
            url = uri() + '?filter=' + filter,
            jsonUser = self.factory.user(),
            reply = self.factory.pagedResult([ jsonUser ]);

        this.$httpBackend.whenGET(url).respond(this.reply(reply));

        self.User.list({ filter: filter }).then(function(pagedResult) {
          expect(pagedResult.items).toBeArrayOfSize(1);
          expect(pagedResult.items[0]).toEqual(jasmine.any(self.User));
        });
        this.$httpBackend.flush();
      });

      it('can list using sort parameter only', function() {
        var self = this,
            sort = 'name',
            url = uri() + '?sort=' + sort,
            jsonUser = self.factory.user(),
            reply = self.factory.pagedResult([ jsonUser ]);

        this.$httpBackend.whenGET(url).respond(this.reply(reply));

        self.User.list({ sort: sort }).then(function(pagedResult) {
          expect(pagedResult.items).toBeArrayOfSize(1);
          expect(pagedResult.items[0]).toEqual(jasmine.any(self.User));
        });
        this.$httpBackend.flush();
      });

      it('should query for multiple users', function() {
        var self     = this,
            filter   = 'email::test',
            sort     = '-email',
            url      = sprintf('%s?filter=%s&sort=%s', uri(), filter, sort),
            jsonUser = self.factory.user(),
            reply    = self.factory.pagedResult([ jsonUser ]);

        this.$httpBackend.whenGET(url).respond(this.reply(reply));

        self.User.list({ filter: filter, sort: sort }).then(function(pagedResult) {
          expect(pagedResult.items).toBeArrayOfSize(1);
          expect(pagedResult.items[0]).toEqual(jasmine.any(self.User));
        });
        this.$httpBackend.flush();
      });

      it('should handle an invalid response', function() {
        var reply = this.factory.pagedResult([ { 'a': 1 } ]);

        this.$httpBackend.whenGET(uri()).respond(this.reply(reply));

        this.User.list().then(testFail).catch(checkError);
        this.$httpBackend.flush();

        function testFail() {
          fail('should not be called');
        }

        function checkError(error) {
          expect(error).toContain('invalid users from server');
        }
      });

    });

    it('can retrieve a single user', function() {
      var self = this,
          user = this.factory.user();

      self.$httpBackend.whenGET(uri(user.id)).respond(this.reply(user));

      self.User.get(user.id).then(function (reply) {
        expect(reply).toEqual(jasmine.any(self.User));
        reply.compareToJsonEntity(user);
      });
      self.$httpBackend.flush();
    });

    it('can register a user', function() {
      var password = this.factory.stringNext();
      var user = new this.User(_.omit(this.factory.user(), 'id'));
      var cmd = registerCommand(user, password);

      this.$httpBackend.expectPOST(uri(), cmd).respond(this.reply());

      user.register(password).then(function(reply) {
        expect(reply).toEqual({});
      });
      this.$httpBackend.flush();
    });

    it('can update a users name', function() {
      var jsonUser = this.factory.user(),
          user = new this.User(jsonUser);

      this.updateEntity.call(this,
                             user,
                             'updateName',
                             user.name,
                             updateUri('name', user.id),
                             { name: user.name },
                             jsonUser,
                             this.expectUser,
                             failTest);
    });

    it('can update a users email', function() {
      var jsonUser = this.factory.user(),
          user = new this.User(jsonUser);

      this.updateEntity.call(this,
                             user,
                             'updateEmail',
                             user.email,
                             updateUri('email', user.id),
                             { email: user.email },
                             jsonUser,
                             this.expectUser,
                             failTest);
    });

    it('can update a users avatar url', function() {
      var newAvatarUrl = this.factory.stringNext(),
          jsonUser = this.factory.user({ avatarUrl: newAvatarUrl }),
          user = new this.User(jsonUser);

      this.updateEntity.call(this,
                             user,
                             'updateAvatarUrl',
                             user.avatarUrl,
                             updateUri('avatarurl', user.id),
                             { avatarUrl: user.avatarUrl },
                             jsonUser,
                             this.expectUser,
                             failTest);
    });

    it('can update a users password', function() {
      var currentPassword = this.factory.stringNext(),
          newPassword = this.factory.stringNext(),
          jsonUser = this.factory.user(),
          user = new this.User(jsonUser);

      this.updateEntity.call(this,
                             user,
                             'updatePassword',
                             [ currentPassword, newPassword ],
                             updateUri('password', user.id),
                             {
                               currentPassword: currentPassword,
                               newPassword:     newPassword
                             },
                             jsonUser,
                             this.expectUser,
                             failTest);
    });

    it('can activate a registered user', function() {
      var user = new this.User(this.factory.user());
      this.statusChangeShared(user, 'activate', 'activate', this.UserState.ACTIVE);
    });

    it('can lock an active user', function() {
      var user = new this.User(_.extend(this.factory.user(), { state: this.UserState.ACTIVE }));
      this.statusChangeShared(user, 'lock', 'lock', this.UserState.LOCKED);
    });

    it('can unlock a locked user', function() {
      var user = new this.User(_.extend(this.factory.user(), { state: this.UserState.LOCKED }));
      this.statusChangeShared(user, 'unlock', 'unlock', this.UserState.ACTIVE);
    });

    it('fails when calling activate and the user is not registered', function() {
      var self = this,
          statuses = [ self.UserState.ACTIVE, self.UserState.LOCKED ];

      _.each(statuses, function (state) {
        var user = new self.User(_.extend(self.factory.user(), { state: state }));

        expect(function () { user.activate(); })
          .toThrow(new Error('user state is not registered: ' + state));
      });
    });

    it('fails when calling lock and the user is not active', function() {
      var self = this,
          statuses = [ self.UserState.REGISTERED, self.UserState.LOCKED ];

      _.each(statuses, function (state) {
        var user = new self.User(_.extend(self.factory.user(), { state: state }));

        expect(function () { user.lock(); })
          .toThrowError(/user state is not active/);
      });
    });

    it('fails when calling unlock and the user is not locked', function() {
      var self = this,
          statuses = [ self.UserState.REGISTERED, self.UserState.ACTIVE ];

      _.each(statuses, function (state) {
        var user = new self.User(_.extend(self.factory.user(), { state: state }));

        expect(function () { user.unlock(); })
          .toThrow(new Error('user state is not locked: ' + state));
      });
    });

    it('state predicates are valid valid', function() {
      var self = this;
      _.each(_.values(self.UserState), function (state) {
        var jsonUser = self.factory.user({ state: state }),
            user     = new self.User(jsonUser);

        expect(user.isRegistered()).toBe(state === self.UserState.REGISTERED);
        expect(user.isActive()).toBe(state === self.UserState.ACTIVE);
        expect(user.isLocked()).toBe(state === self.UserState.LOCKED);
      });
    });

    function uri(userId) {
      var result = '/users/';
      if (arguments.length > 0) {
        result += userId;
      }
      return result;
    }

    function updateUri(/* path, userId */) {
      var result = '/users/',
          args = _.toArray(arguments),
          path,
          userId;

      if (args.length > 0) {
        path = args.shift();
        result += path;
      }

      if (args.length > 0) {
        userId = args.shift();
        result += '/' + userId;
      }
      return result;
    }

    function registerCommand(user, password) {
      return _.extend(_.pick(user, 'name', 'email', 'avatarUrl'), { password: password || '' });
    }

    // used by promise tests
    function failTest(error) {
      expect(error).toBeUndefined();
    }

  });

});
