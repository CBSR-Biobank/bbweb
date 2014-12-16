// Jasmine test suite
//
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  describe('Constant: userResolve', function() {

    var cookies, q, userResolve, httpBackend, usersService, biobankXhrReqService;
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

    beforeEach(inject(function ($q, $httpBackend, _usersService_, _biobankXhrReqService_) {
      q = $q;
      httpBackend = $httpBackend;
      usersService = _usersService_;
      biobankXhrReqService = _biobankXhrReqService_;
    }));

    describe('when token is present', function () {

      beforeEach(inject(function ($cookies) {
        cookies = $cookies;
        $cookies['XSRF-TOKEN'] = fakeToken;
      }));

      it('should return a valid user on authentication success', inject(function(userResolve) {
        httpBackend.whenGET('/authenticate').respond({
          status: 'success',
          data: user
        });

        userResolve.user(cookies, q, usersService, biobankXhrReqService)
          .then(function (actualUser) {
            expect(actualUser).toEqual(user);
          });
        httpBackend.flush();
      }));

      it('should return a invalid user on authentication failure', inject(function(userResolve) {
        httpBackend.whenGET('/authenticate').respond(401, {
          status: 'error',
          message: 'auth failure'
        });

        userResolve.user(cookies, q, usersService, biobankXhrReqService)
          .then(function (user) {
            // if this code runs, the test has failed
            expect(true).toEqual(false);
          })
          .catch(function (resp) {
            expect(resp.data.message).toBe('auth failure');
          });
        httpBackend.flush();
      }));

    });

    describe('when no token is present', function () {

      beforeEach(inject(function ($cookies, $q, $httpBackend, _usersService_, _biobankXhrReqService_) {
        cookies = $cookies;
        delete $cookies['XSRF-TOKEN'];
      }));

      it('should return a invalid user', inject(function($rootScope, userResolve) {
        userResolve.user(cookies, q, usersService, biobankXhrReqService)
          .then(function (user) {
            // if this code runs, the test has failed
            expect(true).toEqual(false);
          })
          .catch(function (err) {
            expect(err).toBe('no token present');
          });
        $rootScope.$digest();
      }));

    });

    describe('when user is defined in usersService', function () {

      beforeEach(inject(function ($cookies, $q, $httpBackend, _biobankXhrReqService_, _usersService_) {
        cookies = $cookies;
        delete $cookies['XSRF-TOKEN'];

        spyOn(usersService, 'getUser').and.callFake(function () {
          return user;
        });
      }));

      it('should return a invalid user on authentication failure', inject(function(userResolve) {
        userResolve.user(cookies, q, usersService, biobankXhrReqService)
          .then(function (actualUser) {
            expect(actualUser).toEqual(user);
          })
          .catch(function (resp) {
            // if this code runs, the test has failed
            expect(true).toEqual(false);
          });
      }));

    });

  });

});

