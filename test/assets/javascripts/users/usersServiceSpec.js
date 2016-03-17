/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
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

      it('should allow changing a password', function() {
        var postResult = {status: 'success', data: 'success'};
        httpBackend.expectPOST('/passreset', {email: user.email}).respond(201, postResult);
        usersService.passwordReset(user.email).then(function(data) {
          expect(data).toBe('success');
        });
        httpBackend.flush();
      });

    });

  });

});
