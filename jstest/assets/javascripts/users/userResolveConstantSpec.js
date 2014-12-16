// Jasmine test suite
//
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  ddescribe('Constant: userResolve', function() {

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

    beforeEach(inject(function ($cookies, $q, $httpBackend, _usersService_, _biobankXhrReqService_) {
      cookies = $cookies;
      q = $q;
      $cookies['XSRF-TOKEN'] = fakeToken;
      httpBackend = $httpBackend;
      usersService = _usersService_;
      biobankXhrReqService = _biobankXhrReqService_;
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
      httpBackend.whenGET('/authenticate').respond({
        status: 'error',
        message: 'auth failure'
      });

      userResolve.user(cookies, q, usersService, biobankXhrReqService)
        .then(function (actualUser) {
          fail();
        })
        .catch(function () {
        });
      httpBackend.flush();
    }));

  });

});

