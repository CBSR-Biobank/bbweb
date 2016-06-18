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

    beforeEach(inject(function($rootScope, $compile, testSuiteMixin) {
      var self = this;

      _.extend(self, testSuiteMixin);

      self.injectDependencies('$rootScope',
                              '$compile',
                              '$q',
                              'adminService');

      self.putHtmlTemplates(
        '/assets/javascripts/admin/directives/biobankAdmin/biobankAdmin.html');
    }));

    it('has valid scope', function() {
      var counts = {
        studies: 1,
        centres: 2,
        users: 3
      };

      spyOn(this.adminService, 'aggregateCounts').and.returnValue(this.$q.when(counts));

      createController.call(this);
      expect(this.controller.counts).toEqual(counts);
    });

  });

});
