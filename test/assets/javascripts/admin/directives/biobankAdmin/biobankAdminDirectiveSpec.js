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

  describe('biobankAdminDirective', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($rootScope, $compile, directiveTestSuite) {
      var self = this;

      _.extend(self, directiveTestSuite);

      self.$q               = self.$injector.get('$q');
      self.adminService     = self.$injector.get('adminService');

      self.putHtmlTemplates(
        '/assets/javascripts/admin/directives/biobankAdmin/biobankAdmin.html');

      self.createController = createController;

      //----

      function createController() {
        self.element = angular.element('<biobank-admin></biobank-admin>');
        self.scope = $rootScope.$new();

        $compile(self.element)(self.scope);
        self.scope.$digest();
        self.controller = self.element.controller('biobankAdmin');
      }
    }));

    it('has valid scope', function() {
      var counts = { studies: 1,
                     centres: 2,
                     users: 3
                   };

      spyOn(this.adminService, 'aggregateCounts').and.returnValue(this.$q.when(counts));

      this.createController();
      expect(this.controller.counts).toEqual(counts);
    });

  });

});
