/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'lodash',
  'biobankApp'
], function(angular, mocks, _) {
  'use strict';

  describe('Component: registerUser', function() {

    var createController = function () {
      this.element = angular.element('<register-user></register-user>');
      this.scope = this.$rootScope.$new();
      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('registerUser');
    };

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(TestSuiteMixin, ServerReplyMixin) {
      var self = this;
      _.extend(self, TestSuiteMixin.prototype, ServerReplyMixin.prototype);
      self.injectDependencies('$rootScope',
                              '$compile',
                              '$q',
                              '$state',
                              'User',
                              'notificationsService');
      self.putHtmlTemplates(
        '/assets/javascripts/users/components/registerUser/registerUser.html');
      spyOn(this.$state, 'go').and.returnValue(null);
    }));

    it('has valid scope', function() {
      createController.call(this);
      expect(this.controller.user).toEqual(new this.User());
      expect(this.controller.password).toBeEmptyString();
      expect(this.controller.confirmPassword).toBeEmptyString();
    });

    it('displays login page after successful registration', function() {
      spyOn(this.User.prototype, 'register').and.returnValue(this.$q.when('ok'));
      createController.call(this);
      this.controller.submit({});
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith('home.users.login');
    });

    it('displays a notification after registering an already registered email address', function() {
      spyOn(this.User.prototype, 'register').and.returnValue(
        this.$q.reject({ status: 403, data: { message: 'already registered' } }));
      spyOn(this.notificationsService, 'error').and.returnValue(null);
      createController.call(this);
      this.controller.submit({});
      this.scope.$digest();
      expect(this.notificationsService.error).toHaveBeenCalled();
    });

    it('displays a notification after registration failure', function() {
      spyOn(this.User.prototype, 'register').and.returnValue(
        this.$q.reject({ status: 401, data: { message: 'xxx' } }));
      spyOn(this.notificationsService, 'error').and.returnValue(null);
      createController.call(this);
      this.controller.submit({});
      this.scope.$digest();
      expect(this.notificationsService.error).toHaveBeenCalled();
    });

    it('goes to home state when cancel button is pressed', function() {
      createController.call(this);
      this.controller.cancel();
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith('home');
    });

  });

});
