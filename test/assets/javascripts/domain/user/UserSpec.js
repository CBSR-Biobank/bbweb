/* global define */
define([
  'angular',
  'angularMocks',
  'underscore',
  'biobank.testUtils',
  'biobankApp'
], function(angular, mocks, _, testUtils) {
  'use strict';

  describe('User', function() {

    var httpBackend, User, UserStatus, funutils, fakeEntities;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($httpBackend,
                               _User_,
                               _UserStatus_,
                               _funutils_,
                               fakeDomainEntities,
                               extendedDomainEntities) {
      httpBackend  = $httpBackend;
      User         = _User_;
      UserStatus   = _UserStatus_;
      funutils     = _funutils_;
      fakeEntities = fakeDomainEntities;
    }));

    it('creating a user with no parameters has default values', function() {
      var user = new User();
      expect(user.id).toBeNull();
      expect(user.version).toBe(0);
      expect(user.timeAdded).toBeNull();
      expect(user.timeModified).toBeNull();
      expect(user.name).toBeEmptyString();
      expect(user.email).toBeEmptyString();
      expect(user.avatarUrl).toBeNull();
      expect(user.status).toBe(UserStatus.REGISTERED());
    });

    it('creating a user with an object does not modify object', function() {
      var obj = null, user = new User(obj);
      expect(user).toBeObject();
      expect(obj).toBeNull();
    });

    it('fails when creating from object with a non object', function() {
      var nonObj = 1;
      expect(User.create(nonObj)).toEqual(
        new Error('invalid object: must be a map, has the correct keys'));
    });

    it('fails when creating from object with missing required keys', function() {
      var obj = fakeEntities.user();
      var requiredKeys = ['id', 'name', 'email', 'status'];

      _.each(requiredKeys, function (key) {
        var badObj = _.omit(obj, key);

        expect(User.create(badObj)).toEqual(
          new Error('invalid object: has the correct keys'));
      });
    });

    it('can retrieve users', function(done) {
      var users = [ fakeEntities.user() ];
      httpBackend.whenGET(uri()).respond(serverReply(fakeEntities.pagedResult(users)));

      User.list().then(function (pagedResult) {
        expect(pagedResult.items).toBeArrayOfSize(users.length);
        expect(pagedResult.items[0]).toEqual(jasmine.any(User));
        pagedResult.items[0].compareToServerEntity(users[0]);
        done();
      });
      httpBackend.flush();
    });

    it('can retrieve a single user', function(done) {
      var user = fakeEntities.user();
      httpBackend.whenGET(uri(user.id)).respond(serverReply(user));

      User.get(user.id).then(function (reply) {
        expect(reply).toEqual(jasmine.any(User));
        reply.compareToServerEntity(user);
        done();
      });
      httpBackend.flush();
    });

    it('can register a user', function(done) {
      var password = fakeEntities.stringNext();
      var user = new User(_.omit(fakeEntities.user(), 'id'));
      var cmd = registerCommand(user, password);

      httpBackend.expectPOST(uri(), cmd).respond(201, serverReply());

      user.register(password).then(function(reply) {
        expect(reply).toEqual({});
        done();
      });
      httpBackend.flush();
    });

    it('can update a users name', function(done) {
      var newName = fakeEntities.stringNext();
      var baseUser = fakeEntities.user();
      var user = new User(baseUser);
      var reply = replyUser(baseUser, { name: newName });
      var cmd = updateNameCommand(user, newName);

      httpBackend.expectPUT(uri(user.id) + '/name', cmd).respond(201, serverReply(reply));

      user.updateName(newName).then(function(updatedUser) {
        expect(updatedUser.id).toEqual(user.id);
        expect(updatedUser.version).toEqual(user.version + 1);
        expect(updatedUser.name).toEqual(newName);
        done();
      });
      httpBackend.flush();
    });

    it('can update a users email', function(done) {
      var newEmail = fakeEntities.stringNext();
      var baseUser = fakeEntities.user();
      var user = new User(baseUser);
      var reply = replyUser(baseUser, { email: newEmail });
      var cmd = updateEmailCommand(user, newEmail);

      httpBackend.expectPUT(uri(user.id) + '/email', cmd).respond(201, serverReply(reply));

      user.updateEmail(newEmail).then(function(updatedUser) {
        expect(updatedUser.id).toEqual(user.id);
        expect(updatedUser.version).toEqual(user.version + 1);
        expect(updatedUser.email).toEqual(newEmail);
        done();
      });
      httpBackend.flush();
    });

    it('can update a users avatar url', function(done) {
      var newAvatarUrl = fakeEntities.stringNext();
      var baseUser = fakeEntities.user();
      var user = new User(baseUser);
      var reply = replyUser(baseUser, { avatarUrl: newAvatarUrl });
      var cmd = updateAvatarUrlCommand(user, newAvatarUrl);

      httpBackend.expectPUT(uri(user.id) + '/avatarurl', cmd).respond(201, serverReply(reply));

      user.updateAvatarUrl(newAvatarUrl).then(function(updatedUser) {
        expect(updatedUser.id).toEqual(user.id);
        expect(updatedUser.version).toEqual(user.version + 1);
        expect(updatedUser.avatarUrl).toEqual(newAvatarUrl);
        done();
      });
      httpBackend.flush();
    });

    it('can update a users password', function(done) {
      var currentPassword = fakeEntities.stringNext();
      var newPassword = fakeEntities.stringNext();
      var baseUser = fakeEntities.user();
      var user = new User(baseUser);
      var reply = replyUser(baseUser);
      var cmd = updatePasswordCommand(user, currentPassword, newPassword);

      httpBackend.expectPUT(uri(user.id) + '/password', cmd).respond(201, serverReply(reply));

      user.updatePassword(currentPassword, newPassword).then(function(replyUser) {
        expect(replyUser.id).toEqual(user.id);
        expect(replyUser.version).toEqual(user.version + 1);
        done();
      });
      httpBackend.flush();
    });

    it('can activate a registered user', function() {
      var user = new User(fakeEntities.user());
      statusChangeShared(user, '/activate', 'activate', UserStatus.ACTIVE());
    });

    it('can lock an active user', function() {
      var user = new User(_.extend(fakeEntities.user(), { status: UserStatus.ACTIVE() }));
      statusChangeShared(user, '/lock', 'lock', UserStatus.LOCKED());
    });

    it('can unlock a locked user', function() {
      var user = new User(_.extend(fakeEntities.user(), { status: UserStatus.LOCKED() }));
      statusChangeShared(user, '/unlock', 'unlock', UserStatus.ACTIVE());
    });

    it('fails when calling activate and the user is not registered', function() {
      var status = [ UserStatus.ACTIVE(), UserStatus.LOCKED() ];

      _.each(status, function () {
        var user = new User(_.extend(fakeEntities.user(), { status: status }));

        expect(function () { user.activate(); })
          .toThrow(new Error('user status is not registered: ' + status));
      });
    });

    it('fails when calling lock and the user is not active', function() {
      var status = [ UserStatus.REGISTERED(), UserStatus.LOCKED() ];

      _.each(status, function () {
        var user = new User(_.extend(fakeEntities.user(), { status: status }));

        expect(function () { user.lock(); })
          .toThrow(new Error('user status is not active: ' + status));
      });
    });

    it('fails when calling unlock and the user is not locked', function() {
      var status = [ UserStatus.REGISTERED(), UserStatus.ACTIVE() ];

      _.each(status, function () {
        var user = new User(_.extend(fakeEntities.user(), { status: status }));

        expect(function () { user.unlock(); })
          .toThrow(new Error('user status is not locked: ' + status));
      });
    });

    function statusChangeShared(user, uriPart, userMethod, newStatus) {
      var cmd = changeStatusCommand(user);
      var reply = replyUser(user, {status: newStatus});

      httpBackend.expectPOST(uri(user.id) + uriPart, cmd).respond(201, serverReply(reply));

      user[userMethod]().then(function (replyUser) {
        expect(replyUser.id).toEqual(user.id);
        expect(replyUser.version).toEqual(user.version + 1);
        expect(replyUser.status).toEqual(newStatus);
      });
      httpBackend.flush();
    }

    function uri(userId) {
      var result = '/users';
      if (arguments.length > 0) {
        result += '/' + userId;
      }
      return result;
    }

    function serverReply(obj) {
      obj = obj || {};
      return { status: '', data: obj };
    }

    function registerCommand(user, password) {
      return _.extend(_.pick(user, 'name', 'email', 'avatarUrl'), { password: password || '' });
    }

    function updateCommand(user) {
      return  { id: user.id, expectedVersion: user.version };
    }

    function updateNameCommand(user, newName) {
      return _.extend(updateCommand(user), { name: newName});
    }

    function updateEmailCommand(user, newEmail) {
      return _.extend(updateCommand(user), { email: newEmail });
    }

    function updateAvatarUrlCommand(user, newAvatarUrl) {
      return _.extend(updateCommand(user), { avatarUrl: newAvatarUrl });
    }

    function updatePasswordCommand(user, currentPassword, newPassword) {
      return _.extend(updateCommand(user), {
        currentPassword: currentPassword,
        newPassword:     newPassword
      });
    }

    function changeStatusCommand(user) {
      return  { id: user.id, expectedVersion: user.version };
    }

    function replyUser(user, newValues) {
      newValues = newValues || {};
      return new User(_.extend({}, user, newValues, {version: user.version + 1}));
    }

  });

});
