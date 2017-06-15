/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'lodash'
], function(angular, mocks, _) {
  'use strict';

  describe('homeDirective', function() {

    var createDirective = function () {
      this.scope = this.$rootScope.$new();
      this.element = angular.element('<home></home>');
      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('home');
    };

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(TestSuiteMixin, ServerReplyMixin) {
      var self = this;

      _.extend(self, TestSuiteMixin.prototype, ServerReplyMixin.prototype);

      self.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$httpBackend',
                              'User',
                              'usersService',
                              'factory');

      self.putHtmlTemplates('/assets/javascripts/home/directives/home/home.html',
                            '/assets/javascripts/common/components/breadcrumbs/breadcrumbs.html');
    }));

    it('has valid scope', function() {
      var user = this.factory.user();
      this.usersService.requestCurrentUser =
        jasmine.createSpy().and.returnValue(this.$q.when(user));
      createDirective.call(this);
      expect(this.controller.user).toEqual(jasmine.any(this.User));
      expect(this.$rootScope.pageTitle).toBeDefined('Biobank');
    });
  });

});
