/**
 * Jasmine test suite
 */
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

    it('fails when creating from object with missing required keys', function() {
      var obj = fakeEntities.user();
      var requiredKeys = ['id', 'name', 'email', 'status'];

      _.each(requiredKeys, function (key) {
        var badObj = _.omit(obj, key);

        expect(function () { User.create(badObj); })
          .toThrow(new Error('invalid object: has the correct keys'));
      });
    });

    it('fails when creating from event with missing required keys', function() {
      var user = fakeEntities.user();
      var event = registeredEvent(user);
      var requiredKeys = ['id', 'name', 'email'];

      _.each(requiredKeys, function (key) {
        var badEvent = _.omit(event, key);

        var result = User.createFromEvent(badEvent);
        expect(result).toEqual(new Error('invalid event: has the correct keys'));
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
      var event = registeredEvent(user);

      httpBackend.expectPOST(uri(), cmd).respond(201, serverReply(event));

      user.register(password).then(function(replyUser) {
        expect(replyUser.id).toEqual(event.id);
        expect(replyUser.version).toEqual(0);
        expect(replyUser.name).toEqual(user.name);
        expect(replyUser.description).toEqual(user.description);
        done();
      });
      httpBackend.flush();
    });

    it('can update a users name', function(done) {
      var newName = fakeEntities.stringNext();
      var user = new User(fakeEntities.user());
      var cmd = updateNameCommand(user, newName);
      var event = nameUpdatedEvent(user, newName);

      httpBackend.expectPUT(uri(user.id) + '/name', cmd).respond(201, serverReply(event));

      user.updateName(newName).then(function(replyUser) {
        expect(replyUser.timeAdded).toBeNull();
        expect(replyUser.timeModified).toBeNull();
        expect(replyUser.id).toEqual(event.id);
        expect(replyUser.version).toEqual(event.version);
        expect(replyUser.name).toEqual(newName);
        done();
      });
      httpBackend.flush();
    });

    it('can update a users email', function(done) {
      var newEmail = fakeEntities.stringNext();
      var user = new User(fakeEntities.user());
      var cmd = updateEmailCommand(user, newEmail);
      var event = emailUpdatedEvent(user, newEmail);

      httpBackend.expectPUT(uri(user.id) + '/email', cmd).respond(201, serverReply(event));

      user.updateEmail(newEmail).then(function(replyUser) {
        expect(replyUser.timeAdded).toBeNull();
        expect(replyUser.timeModified).toBeNull();
        expect(replyUser.id).toEqual(event.id);
        expect(replyUser.version).toEqual(event.version);
        expect(replyUser.email).toEqual(newEmail);
        done();
      });
      httpBackend.flush();
    });

    it('can update a users avatar url', function(done) {
      var newAvatarUrl = fakeEntities.stringNext();
      var user = new User(fakeEntities.user());
      var cmd = updateAvatarUrlCommand(user, newAvatarUrl);
      var event = avatarUrlUpdatedEvent(user, newAvatarUrl);

      httpBackend.expectPUT(uri(user.id) + '/avatarurl', cmd).respond(201, serverReply(event));

      user.updateAvatarUrl(newAvatarUrl).then(function(replyUser) {
        expect(replyUser.timeAdded).toBeNull();
        expect(replyUser.timeModified).toBeNull();
        expect(replyUser.id).toEqual(event.id);
        expect(replyUser.version).toEqual(event.version);
        expect(replyUser.avatarUrl).toEqual(newAvatarUrl);
        done();
      });
      httpBackend.flush();
    });

    it('can update a users password', function(done) {
      var currentPassword = fakeEntities.stringNext();
      var newPassword = fakeEntities.stringNext();
      var user = new User(fakeEntities.user());
      var cmd = updatePasswordCommand(user, currentPassword, newPassword);
      var event = passwordUpdatedEvent(user, currentPassword, newPassword);

      httpBackend.expectPUT(uri(user.id) + '/password', cmd).respond(201, serverReply(event));

      user.updatePassword(currentPassword, newPassword).then(function(replyUser) {
        expect(replyUser.timeAdded).toBeNull();
        expect(replyUser.timeModified).toBeNull();
        expect(replyUser.id).toEqual(event.id);
        expect(replyUser.version).toEqual(event.version);
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
      var event = statusChangedEvent(user);

      httpBackend.expectPOST(uri(user.id) + uriPart, cmd).respond(201, serverReply(event));

      user[userMethod]().then(function (replyUser) {
        expect(replyUser.timeAdded).toBeNull();
        expect(replyUser.timeModified).toBeNull();
        expect(replyUser.id).toEqual(event.id);
        expect(replyUser.version).toEqual(event.version);
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
      return { status: '', data: obj };
    }

    function registerCommand(user, password) {
      return _.extend(_.pick(user, 'name', 'email', 'avatarUrl'), { password: password || '' });
    }

    function registeredEvent(user) {
      return _.extend(_.pick(user, 'name', 'email', 'avatarUrl'), { id: testUtils.uuid() });
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

    function updateEvent(user) {
      return  { id: user.id, version: user.version };
    }

    function nameUpdatedEvent(user, newName) {
      return _.extend(updateEvent(user), { name: newName});
    }

    function emailUpdatedEvent(user, newEmail) {
      return _.extend(updateEvent(user), { email: newEmail });
    }

    function avatarUrlUpdatedEvent(user, newAvatarUrl) {
      return _.extend(updateEvent(user), { avatarUrl: newAvatarUrl });
    }

    function passwordUpdatedEvent(user) {
      return _.extend(_.pick(user, 'id', 'version'), {
        password: fakeEntities.stringNext(),
        salt:     fakeEntities.stringNext()
      });
    }

    function changeStatusCommand(user) {
      return  { id: user.id, expectedVersion: user.version };
    }

    function statusChangedEvent(user) {
      return  _.pick(user, 'id', 'version');
    }

  });

});
