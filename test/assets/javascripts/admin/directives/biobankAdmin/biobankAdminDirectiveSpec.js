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

  describe('biobankAdminDirective', function() {

    var createController = function () {
      this.element = angular.element('<biobank-admin></biobank-admin>');
      this.scope = this.$rootScope.$new();

      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('biobankAdmin');
    };

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($rootScope, $compile, TestSuiteMixin) {
      var self = this;

      _.extend(self, TestSuiteMixin.prototype);
      self.injectDependencies('$rootScope',
                              '$compile',
                              '$q',
                              'User',
                              'adminService',
                              'usersService',
                              'factory');
      self.putHtmlTemplates(
        '/assets/javascripts/admin/directives/biobankAdmin/biobankAdmin.html');

    }));

    it('has valid scope', function() {
      var user = this.factory.user(),
          counts = {
            studies: 1,
            centres: 2,
            users: 3
          };

      this.usersService.requestCurrentUser =
        jasmine.createSpy().and.returnValue(this.$q.when(user));
      spyOn(this.adminService, 'aggregateCounts').and.returnValue(this.$q.when(counts));

      createController.call(this);
      expect(this.controller.user).toEqual(jasmine.any(this.User));
      expect(this.controller.counts).toEqual(counts);
    });

  });

});
