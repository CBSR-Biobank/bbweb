/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { ServerReplyMixin } from 'test/mixins/ServerReplyMixin';
import { TestSuiteMixin } from 'test/mixins/TestSuiteMixin';
import _ from 'lodash';
import ngModule from '../../index'

describe('Service: userService', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function () {
      Object.assign(this, TestSuiteMixin, ServerReplyMixin);

      this.injectDependencies('$q',
                              '$httpBackend',
                              '$cookies',
                              'biobankApi',
                              'User',
                              'Factory');

      this.jsonUser = this.Factory.user();
      this.user = this.User.create(this.jsonUser);
      this.$httpBackend.whenGET(this.url('users/authenticate')).respond(this.reply(this.jsonUser));

      this.doLogin = (token, user) => {
        const email = 'test@test.com';
        const password = 'test';

        this.$httpBackend.expectPOST(this.url('users/login'), { email, password })
          .respond(this.reply(this.jsonUser));

        this.userService.login(email, password).then(function(reply) {
          expect(_.isEqual(reply, user));
        });
        this.$httpBackend.flush();
      };
    });
  });

  afterEach(function() {
    this.$httpBackend.verifyNoOutstandingExpectation();
    this.$httpBackend.verifyNoOutstandingRequest();
    this.$cookies.remove('XSRF-TOKEN');
  });

  describe('service initialization', function () {

    /**
     * userService needs to be injected to the test so that the initialization code is executed
     * when the test starts.
     */
    it('should allow a user to re-connect', function() {
      this.$cookies.put('XSRF-TOKEN', this.Factory.stringNext());
      const userService = this.$injector.get('userService');
      this.$httpBackend.flush();
      expect(userService.getCurrentUser()).toEqual(jasmine.any(this.User));
    });

    it('should not allow a user to re-connect when authentication fails', function() {
      // 401 cause the http interceptor to intercept the request.
      //
      // the interceptor displays a modal to the user stating the session expired
      // this must be disabled for this test
      this.$injector.get('modalService').modalOk = jasmine.createSpy().and.returnValue(this.$q.when('OK'));

      this.$cookies.put('XSRF-TOKEN', this.Factory.stringNext());
      this.$httpBackend.expectGET(this.url('users/authenticate'))
        .respond(401, this.errorReply('simulated auth failure'));

      const userService = this.$injector.get('userService');
      this.$httpBackend.flush();
      expect(userService.getCurrentUser()).toBeUndefined();
    });
  });

  describe('service functions', function () {

    beforeEach(function() {
      this.injectDependencies('userService');
    });

    it('should return the user that is logged in after a session timeout', function() {
      this.userService.sessionTimeout();
      this.userService.requestCurrentUser().then(reply => {
        expect(reply).toEqual(jasmine.any(this.User));
      });
      this.$httpBackend.flush();
    });

    describe('logging in', function() {

      it('should allow a user to login', function () {
        var token = this.Factory.stringNext;
        this.doLogin(token, this.user);
        expect(this.userService.getCurrentUser()).not.toBeUndefined();
      });

      it('should return the user that is logged in', function() {
        const token = this.Factory.stringNext;

        this.doLogin(token, this.user);
        this.userService.requestCurrentUser().then(reply => {
          expect(reply).toEqual(jasmine.any(this.User));
        });
      });

    });

    it('show allow a user to logout', function() {
      this.$httpBackend.expectPOST(this.url('users/logout')).respond(this.reply('success'));
      this.userService.logout();
      this.$httpBackend.flush();
      expect(this.userService.getCurrentUser()).toBeUndefined();
    });

    it('should allow changing a password', function() {
      this.$httpBackend.expectPOST(this.url('users/passreset'), {email: this.user.email})
        .respond(this.reply('success'));
      this.userService.passwordReset(this.user.email).then(function(reply) {
        expect(reply).toBe('success');
      });
      this.$httpBackend.flush();
    });

  });

});
