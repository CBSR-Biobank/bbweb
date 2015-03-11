// Jasmine test suite
define(['angular', 'angularMocks', 'jquery', 'underscore', 'biobankApp'], function(angular, mocks, $, _) {
  'use strict';

  describe('Service: userService', function() {

    var usersService;
    var fakeToken = 'fake-token';
    var userNoId = {
      version:      1,
      timeAdded:    '2014-10-20T09:58:43-0600',
      name:         'testuser',
      email:        'testuser@test.com',
      avatarUrl:    'http://www.avatarsdb.com/avatars/duck_walking.gif',
      status:       'Active'
    };
    var user = angular.extend({id: 'dummy-id'}, userNoId);

    function uri(userId) {
      var result = '/users';
      if (arguments.length >= 1) {
        result += '/' + userId;
      }
      return result;
    }

    beforeEach(mocks.module('biobankApp'));

    describe('service initialization', function () {

      var authenticateDeferred;

      beforeEach(inject(function ($cookies, $q, biobankApi) {
        $cookies['XSRF-TOKEN'] = fakeToken;

        spyOn(biobankApi, 'get').and.callFake(function () {
          authenticateDeferred = $q.defer();
          return authenticateDeferred.promise;
        });
      }));

      /**
       * usersService needs to be injected to the test so that the initialization code is executed
       * when the test starts.
       */
      it('should allow a user to re-connect', inject(function($rootScope, usersService) {
        authenticateDeferred.resolve(user);
        $rootScope.$digest();
        expect(usersService.getCurrentUser()).toEqual(user);
      }));

      it('should not allow a user to re-connect', inject(function($rootScope, usersService) {
        authenticateDeferred.reject();
        $rootScope.$digest();
        expect(usersService.getCurrentUser()).toBeNull();
      }));
    });

    describe('service functions', function () {

      var httpBackend;

      function doLogin() {
        var credentials = {
          email: 'test@test.com',
          password: 'test'
        };
        httpBackend.expectPOST('/login', credentials).respond(201, fakeToken);

        httpBackend.whenGET('/authenticate').respond({
          status: 'success',
          data: [user]
        });

        usersService.login(credentials).then(function(data) {
          expect(_.isEqual(data, user));
        });
        httpBackend.flush();
      }

      beforeEach(inject(function (_usersService_, $httpBackend) {
        usersService = _usersService_;
        httpBackend = $httpBackend;
      }));

      afterEach(function() {
        httpBackend.verifyNoOutstandingExpectation();
        httpBackend.verifyNoOutstandingRequest();
      });

      it('should have the following functions', function () {
        expect(usersService.getUserCounts).toBeFunction();
        expect(usersService.getAllUsers).toBeFunction();
        expect(usersService.requestCurrentUser).toBeFunction();
        expect(usersService.query).toBeFunction();
        expect(usersService.getUsers).toBeFunction();
        expect(usersService.add).toBeFunction();
        expect(usersService.updateName).toBeFunction();
        expect(usersService.updateEmail).toBeFunction();
        expect(usersService.updatePassword).toBeFunction();
        expect(usersService.updateAvatarUrl).toBeFunction();
        expect(usersService.passwordReset).toBeFunction();
        expect(usersService.activate).toBeFunction();
        expect(usersService.lock).toBeFunction();
        expect(usersService.unlock).toBeFunction();
      });

      it('should allow a user to login', function () {
        doLogin();
      });

      it('show allow a user to logout', function() {
        httpBackend.expectPOST('/logout').respond(201, 'success');
        usersService.logout();
        httpBackend.flush();
      });

      it('should return the user that is logged in', function() {
        doLogin();
        expect(_.isEqual(usersService.requestCurrentUser(), user));
      });

      it('calling getUserCount has valid URL', function() {
        httpBackend.whenGET(uri() + '/counts').respond({
          status: 'success',
          data: [user]
        });

        usersService.getUserCounts().then(function(data) {
          expect(data.length).toEqual(1);
          expect(_.isEqual(user, data[0]));
        });

        httpBackend.flush();
      });

      it('should get a list of all users', function() {
        httpBackend.whenGET(uri()).respond({
          status: 'success',
          data: [user]
        });

        usersService.getAllUsers().then(function(data) {
          expect(_.isEqual(data, user));
        });
        httpBackend.flush();
      });


      it('should return a user given a valid user ID', function() {
        httpBackend.whenGET(uri(user.id)).respond({
          status: 'success',
          data: user
        });
        usersService.query(user.id).then(function(data) {
          expect(_.isEqual(data, user));
        });
        httpBackend.flush();
      });

      it('should query for multiple users - no parameters', function() {
        httpBackend.whenGET(uri()).respond({
          status: 'success',
          data: [user]
        });

        usersService.getUsers().then(function(data) {
          expect(data.length).toBe(1);
          expect(_.isEqual(data[0], user));
        });
        httpBackend.flush();
      });

      it('should query for multiple users - name filter parameter only', function() {
        var nameFilter = 'test';

        httpBackend.whenGET(uri() + '?' + $.param({nameFilter: nameFilter})).respond({
          status: 'success',
          data: [user]
        });

        usersService.getUsers({nameFilter: nameFilter}).then(function(data) {
          expect(data.length).toBe(1);
          expect(_.isEqual(data[0], user));
        });
        httpBackend.flush();
      });

      it('should query for multiple users - email filter parameter only', function() {
        var emailFilter = 'test';

        httpBackend.whenGET(uri() + '?' + $.param({emailFilter: emailFilter})).respond({
          status: 'success',
          data: [user]
        });

        usersService.getUsers({emailFilter: emailFilter}).then(function(data) {
          expect(data.length).toBe(1);
          expect(_.isEqual(data[0], user));
        });
        httpBackend.flush();
      });

      it('should query for multiple users - sort parameter only', function() {
        var sort = 'asc';

        httpBackend.whenGET(uri() + '?' + $.param({sort: sort})).respond({
          status: 'success',
          data: [user]
        });

        usersService.getUsers({sort: sort}).then(function(data) {
          expect(data.length).toBe(1);
          expect(_.isEqual(data[0], user));
        });
        httpBackend.flush();
      });

      it('should query for multiple users', function() {
        var emailFilter = 'test';
        var sort = 'email';
        var order = 'desc';

        httpBackend.whenGET(uri() + '?' + $.param({emailFilter: emailFilter, sort: sort, order: 'desc'})).respond({
          status: 'success',
          data: [user]
        });

        usersService.getUsers({emailFilter: emailFilter, sort: sort, order: order}).then(function(data) {
          expect(data.length).toBe(1);
          expect(_.isEqual(data[0], user));
        });
        httpBackend.flush();
      });

      it('should allow adding a user', function() {
        var postResult = {status: 'success', data: 'success'};
        var cmd = {
          name:      user.name,
          email:     user.email,
          password:  user.password,
          avatarUrl: user.avatarUrl
        };
        httpBackend.expectPOST(uri(), cmd).respond(201, postResult);
        usersService.add(userNoId).then(function(reply) {
          expect(reply).toEqual('success');
        });
        httpBackend.flush();
      });

      it('should allow adding a user - no avatar url', function() {
        var cmd = {
          name:      user.name,
          email:     user.email,
          password:  user.password
        };
        httpBackend.expectPOST(uri(), cmd).respond(201, {status: 'success', data: 'success'});

        userNoId.avatarUrl = undefined;
        usersService.add(userNoId).then(function(reply) {
          expect(reply).toEqual('success');
        });
        httpBackend.flush();
      });

      it('should allow changing a password', function() {
        var postResult = {status: 'success', data: 'success'};
        httpBackend.expectPOST('/passreset', {email: user.email}).respond(201, postResult);
        usersService.passwordReset(user.email).then(function(data) {
          expect(data).toBe('success');
        });
        httpBackend.flush();
      });

      it('should allow a users name to be changed', function() {
        var expectedCmd = {
          id:              user.id,
          expectedVersion: user.version,
          name:            user.name
        };
        var postResult = {status: 'success', data: 'success'};
        httpBackend.expectPUT(uri(user.id) + '/name', expectedCmd).respond(201, postResult);
        usersService.updateName(user, user.name).then(function(data) {
          expect(data).toBe('success');
        });
        httpBackend.flush();
      });

      it('should allow a users email to be changed', function() {
        var expectedCmd = {
          id:              user.id,
          expectedVersion: user.version,
          email:           user.email
        };
        var postResult = {status: 'success', data: 'success'};
        httpBackend.expectPUT(uri(user.id) + '/email', expectedCmd).respond(201, postResult);
        usersService.updateEmail(user, user.email).then(function(data) {
          expect(data).toBe('success');
        });
        httpBackend.flush();
      });

      it('should allow a users password to be changed', function() {
        var expectedCmd = {
          id:              user.id,
          expectedVersion: user.version,
          password:        user.password
        };
        var postResult = {status: 'success', data: 'success'};
        httpBackend.expectPUT(uri(user.id) + '/password', expectedCmd).respond(201, postResult);
        usersService.updatePassword(user, user.password).then(function(data) {
          expect(data).toBe('success');
        });
        httpBackend.flush();
      });

      it('should allow a users avatar URL to be changed', function() {
        var expectedCmd = {
          id:              user.id,
          expectedVersion: user.version,
          avatarUrl:       user.avatarUrl
        };
        var postResult = {status: 'success', data: 'success'};
        httpBackend.expectPUT(uri(user.id) + '/avatarurl', expectedCmd).respond(201, postResult);
        usersService.updateAvatarUrl(user, user.avatarUrl).then(function(data) {
          expect(data).toBe('success');
        });
        httpBackend.flush();
      });

      it('should allow a users avatar URL to be removed', function() {
        var expectedCmd = { id: user.id, expectedVersion: user.version };
        var postResult = {status: 'success', data: 'success'};
        httpBackend.expectPUT(uri(user.id) + '/avatarurl', expectedCmd).respond(201, postResult);
        usersService.updateAvatarUrl(user, null).then(function(data) {
          expect(data).toBe('success');
        });
        httpBackend.flush();
      });

      function userStatusChange(status, serviceFn) {
        var expectedCmd = { id: user.id, expectedVersion: user.version};
        var postResult = {status: 'success', data: 'success'};
        httpBackend.expectPOST(uri(user.id) + '/' + status, expectedCmd).respond(201, postResult);
        serviceFn(user).then(function(reply) {
          expect(reply).toEqual('success');
        });
        httpBackend.flush();
      }

      it('should allow activating a user', function() {
        userStatusChange('activate', usersService.activate);
      });

      it('should allow locking a user', function() {
        userStatusChange('lock', usersService.lock);
      });

      it('should allow unlocking a user', function() {
        userStatusChange('unlock', usersService.unlock);
      });


    });

  });

});
