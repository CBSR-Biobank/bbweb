/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var mocks   = require('angularMocks'),
      _       = require('lodash');

  describe('Component: biobankAdmin', function() {

    function SuiteMixinFactory(ComponentTestSuiteMixin) {

      function SuiteMixin() {
      }

      SuiteMixin.prototype = Object.create(ComponentTestSuiteMixin.prototype);
      SuiteMixin.prototype.constructor = SuiteMixin;

      SuiteMixin.prototype.createController = function () {
        ComponentTestSuiteMixin.prototype.createController.call(
          this,
          '<biobank-admin></biobank-admin>',
          undefined,
          'biobankAdmin');
      };

      return SuiteMixin;
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ComponentTestSuiteMixin) {
      _.extend(this, new SuiteMixinFactory(ComponentTestSuiteMixin).prototype);
      this.injectDependencies('$rootScope',
                              '$compile',
                              '$q',
                              'User',
                              'adminService',
                              'usersService',
                              'factory');
      this.putHtmlTemplates(
        '/assets/javascripts/admin/components/biobankAdmin/biobankAdmin.html',
        '/assets/javascripts/common/components/breadcrumbs/breadcrumbs.html');

    }));

    it('has valid scope', function() {
      var user = this.User.create(this.factory.user()),
          counts = {
            studies: 1,
            centres: 2,
            users: 3
          };

      this.usersService.requestCurrentUser =
        jasmine.createSpy().and.returnValue(this.$q.when(user));
      spyOn(this.adminService, 'aggregateCounts').and.returnValue(this.$q.when(counts));

      this.createController();
      expect(this.controller.user).toEqual(jasmine.any(this.User));
      expect(this.controller.counts).toEqual(counts);
    });

  });

});
