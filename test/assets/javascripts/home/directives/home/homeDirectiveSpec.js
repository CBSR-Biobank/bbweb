/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'underscore'
], function(angular, mocks, _) {
  'use strict';

  describe('homeDirective', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($rootScope, $compile, templateMixin, serverReplyMixin) {
      var self = this;

      _.extend(self, templateMixin, serverReplyMixin);

      self.$rootScope = self.$injector.get('$rootScope');
      self.$httpBackend = self.$injector.get('$httpBackend');
      self.putHtmlTemplates('/assets/javascripts/home/directives/home/home.html');
      self.createDirective = createDirective;

      //--

      function createDirective(test) {
        self.scope = $rootScope.$new();
        self.element = angular.element('<home></home>');
        $compile(self.element)(self.scope);
        self.scope.$digest();
        self.controller = self.element.controller('home');
      }
    }));

    it('has valid scope', function() {
      this.$httpBackend.whenGET('/authenticate').respond(this.reply());

      this.createDirective(this);
      expect(this.$rootScope.pageTitle).toBeDefined('Biobank');
    });
  });

});
