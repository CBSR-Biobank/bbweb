/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var mocks = require('angularMocks'),
      _     = require('lodash');

  describe('homeDirective', function() {

    function SuiteMixinFactory(ComponentTestSuiteMixin) {

      function SuiteMixin() {
        ComponentTestSuiteMixin.call(this);
      }

      SuiteMixin.prototype = Object.create(ComponentTestSuiteMixin.prototype);
      SuiteMixin.prototype.constructor = SuiteMixin;

      SuiteMixin.prototype.createController = function () {
        ComponentTestSuiteMixin.prototype.createController.call(
          this,
          '<home></home>',
          undefined,
          'home');
      };

      return SuiteMixin;
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ComponentTestSuiteMixin) {
      _.extend(this, new SuiteMixinFactory(ComponentTestSuiteMixin).prototype);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$httpBackend',
                              'User',
                              'usersService',
                              'factory');

      this.putHtmlTemplates('/assets/javascripts/home/components/home/home.html',
                            '/assets/javascripts/common/components/breadcrumbs/breadcrumbs.html');
    }));

    it('has valid scope', function() {
      var user = this.factory.user();
      this.usersService.requestCurrentUser =
        jasmine.createSpy().and.returnValue(this.$q.when(user));
      this.createController();
      expect(this.controller.user).toEqual(jasmine.any(this.User));
      expect(this.$rootScope.pageTitle).toBeDefined('Biobank');
    });
  });

});
