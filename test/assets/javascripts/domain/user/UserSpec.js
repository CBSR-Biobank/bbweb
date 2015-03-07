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

  fdescribe('User', function() {

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
      var password = fakeEntities.domainEntityNameNext();
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

    function uri(userId) {
      var result = '/users';
      if (arguments.length > 0) {
        result += '/' + userId;
      }
      return result;
    }

    function serverReply(obj) {
      return { status: 'success', data: obj };
    }

    function registerCommand(user, password) {
      return _.extend(_.pick(user, 'name', 'email', 'avatarUrl'), { password: password || '' });
    }

    function registeredEvent(user) {
      return _.extend(_.pick(user, 'name', 'email', 'avatarUrl'), { id: testUtils.uuid() });
    }

  });

});
