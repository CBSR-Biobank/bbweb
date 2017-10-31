/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import ngModule from '../../index'

describe('Service: userService', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function (TestSuiteMixin, ServerReplyMixin) {
      _.extend(this, TestSuiteMixin, ServerReplyMixin);

      this.injectDependencies('$q',
                              '$httpBackend',
                              '$cookies',
                              'biobankApi',
                              'User',
                              'Factory');

      this.jsonUser = this.Factory.user();
      this.user = this.User.create(this.jsonUser);
    });
  });

  afterEach(function() {
    this.$httpBackend.verifyNoOutstandingExpectation();
    this.$httpBackend.verifyNoOutstandingRequest();
  });

  describe('service initialization', function () {

    /**
     * usersService needs to be injected to the test so that the initialization code is executed
     * when the test starts.
     */
    it('should allow a user to re-connect', function() {
      var usersService;

      this.$cookies.put('XSRF-TOKEN', this.Factory.stringNext());
      this.$httpBackend.expectGET(this.url('users/authenticate')).respond(this.reply(this.jsonUser));

      usersService = this.$injector.get('usersService');
      this.$httpBackend.flush();
      expect(usersService.getCurrentUser()).toEqual(jasmine.any(this.User));
    });

    it('should not allow a user to re-connect when authentication fails', function() {
      var usersService;

      // 401 cause the http interceptor to intercept the request.
      //
      // the interceptor displays a modal to the user stating the session expired
      // this must be disabled for this test
      this.$injector.get('modalService').modalOk = jasmine.createSpy().and.returnValue(this.$q.when('OK'));

      this.$cookies.put('XSRF-TOKEN', this.Factory.stringNext());
      this.$httpBackend.expectGET(this.url('users/authenticate'))
        .respond(401, this.errorReply('simulated auth failure'));

      usersService = this.$injector.get('usersService');
      this.$httpBackend.flush();
      expect(usersService.getCurrentUser()).toBeUndefined();
    });
  });

  describe('service functions', function () {

    beforeEach(function() {
      this.injectDependencies('usersService');
    });

    it('should return the user that is logged in after a session timeout', function() {
      var self = this;

      this.$httpBackend.expectGET(this.url('users/authenticate')).respond(this.reply(this.jsonUser));
      self.usersService.sessionTimeout();
      self.usersService.requestCurrentUser().then(function (reply) {
        expect(reply).toEqual(jasmine.any(self.User));
      });
      this.$httpBackend.flush();
    });

    describe('logging in', function() {

      var doLogin = function (token, user) {
        var credentials = {
          email: 'test@test.com',
          password: 'test'
        };
        this.$httpBackend.expectPOST(this.url('users/login'), credentials).respond(this.reply(this.jsonUser));

        this.usersService.login(credentials).then(function(reply) {
          expect(_.isEqual(reply, user));
        });
        this.$httpBackend.flush();
      };

      it('should allow a user to login', function () {
        var token = this.Factory.stringNext;
        doLogin.call(this, token, this.user);
      });

      it('should return the user that is logged in', function() {
        var self = this,
            token = this.Factory.stringNext;

        doLogin.call(self, token, self.user);
        self.usersService.requestCurrentUser().then(function (reply) {
          expect(reply).toEqual(jasmine.any(self.User));
        });
      });

    });

    it('show allow a user to logout', function() {
      this.$httpBackend.expectPOST(this.url('users/logout')).respond(this.reply('success'));
      this.usersService.logout();
      this.$httpBackend.flush();
    });

    it('should allow changing a password', function() {
      this.$httpBackend.expectPOST(this.url('users/passreset'), {email: this.user.email})
        .respond(this.reply('success'));
      this.usersService.passwordReset(this.user.email).then(function(reply) {
        expect(reply).toBe('success');
      });
      this.$httpBackend.flush();
    });

  });

});
