/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([
  'angularMocks',
  'underscore',
  'biobankApp'
], function(mocks, _) {
  'use strict';

  describe('Controller: AdminCtrl', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($rootScope, $controller) {
      var self = this;

      self.$q               = self.$injector.get('$q');
      self.adminService     = self.$injector.get('adminService');
      self.createController = createController;

      //----

      function createController() {
        self.scope = $rootScope.$new();

        $controller('AdminCtrl as vm', {
          $scope:       self.scope,
          adminService: self.adminService
        });
        self.scope.$digest();
      }
    }));

    it('has valid scope', function() {
      var self = this,
          counts = { studies: 1,
                     centres: 2,
                     users: 3
                   };

      spyOn(self.adminService, 'aggregateCounts').and.callFake(function () {
        return self.$q.when(counts);
      });

      self.createController(counts);
      expect(self.scope.vm.counts).toEqual(counts);
    });

  });

});
