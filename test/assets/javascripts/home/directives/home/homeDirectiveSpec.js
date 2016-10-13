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
                              '$httpBackend');

      self.putHtmlTemplates('/assets/javascripts/home/directives/home/home.html');
    }));

    it('has valid scope', function() {
      createDirective.call(this);
      expect(this.$rootScope.pageTitle).toBeDefined('Biobank');
    });
  });

});
