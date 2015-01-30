// Jasmine test suite
//
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  describe('Service: authorizationService', function() {

    var $rootScope, $q, authorization, usersService, resolved;
    var fakeToken = 'fake-token';
    var user = {
      id:           'dummy-id',
      version:      1,
      timeAdded:    '2014-10-20T09:58:43-0600',
      name:         'testuser',
      email:        'testuser@test.com',
      avatarUrl:    'http://www.avatarsdb.com/avatars/duck_walking.gif',
      status:       'Active'
    };

    beforeEach(mocks.module('biobankApp'));

    beforeEach(inject(function (_$rootScope_, _$q_, _usersService_, _authorization_) {
      $rootScope = _$rootScope_;
      $q = _$q_;
      usersService = _usersService_;
      authorization = _authorization_;
      resolved = false;

      spyOn(usersService, 'requestCurrentUser').and.callFake(function () {
        usersService.currentUser = usersService.currentUser || user;
        var promise = $q.when(usersService.currentUser);
        // Trigger a digest to resolve the promise;
        return promise;
      });
    }));

    fdescribe('requireAuthenticatedUser', function () {

      it('requests the user from the server', function (done) {
        var failTest = function(error) {
          expect(error).toBeUndefined();
        };

        expect(usersService.isAuthenticated()).toBe(false);
        authorization.requireAuthenticatedUser()
          .then(function (data) {
            console.log('*************', data);
            resolved = true;
            expect(authorization.isAuthenticated()).toBe(true);
            expect(authorization.getCurrentUser()).toBe(user);
          })
          .catch(failTest)
          .finally(done);
        $rootScope.$digest();
        expect(resolved).toBe(true);
        done();
      });

    });

    describe('requireAdminUser', function () {

      it('requests the user from the server', function () {
        expect(usersService.isAuthenticated()).toBe(false);
        authorization.requireAdminUser().then(function(data) {
          resolved = true;
          expect(authorization.isAdmin()).toBe(true);
          expect(authorization.getCurrentUser()).toBe(user);
        });
        $rootScope.$digest();
        expect(resolved).toBe(true);
      });

    });

  });

});
