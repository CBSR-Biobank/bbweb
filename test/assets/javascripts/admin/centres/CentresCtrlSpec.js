/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
// Jasmine test suite
//
define([
  'angular',
  'underscore',
  'angularMocks',
  'biobankApp'
], function(angular, _, mocks) {
  'use strict';

  describe('Controller: CentresCtrl', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($rootScope, $controller, testUtils) {
      var self = this;

      self.$q           = self.$injector.get('$q');
      self.CentreCounts = self.$injector.get('CentreCounts');
      self.Centre       = self.$injector.get('Centre');
      self.CentreStatus = self.$injector.get('CentreStatus');
      self.jsonEntities = self.$injector.get('jsonEntities');

      self.createController = createController;
      self.createCentreCounts = createCentreCounts;

      testUtils.addCustomMatchers();

      function createCentreCounts(disabled, enabled, retired) {
        return new self.CentreCounts({
          total:    disabled + enabled + retired,
          disabled: disabled,
          enabled:  enabled,
          retired:  retired
        });
      }

      function createController(centreCounts) {
        self.scope = $rootScope.$new();

        $controller('CentresCtrl as vm', {
          $scope:       self.scope,
          Centre:       self.Centre,
          CentreStatus: self.CentreStatus,
          CentreCounts: self.CentreCounts
        });

          self.scope.$digest();
      }
    }));

    it('scope is valid on startup', function() {
      var self = this,
          CentreStatus = self.$injector.get('CentreStatus'),
          allStatuses = CentreStatus.values(),
          counts = self.createCentreCounts(1, 2, 3);

      spyOn(self.CentreCounts, 'get').and.callFake(function () {
        return self.$q.when(counts);
      });

      self.createController(counts);

      expect(self.scope.vm.centreCounts).toEqual(counts);
      expect(self.scope.vm.pageSize).toBeDefined();

      _.each(allStatuses, function(status) {
        expect(self.scope.vm.possibleStatuses).toContain({ id: status, label: CentreStatus.label(status)});
      });
      expect(self.scope.vm.possibleStatuses).toContain({ id: 'all', label: 'All'});
    });

    it('updateCentres retrieves new list of centres', function() {
      var self = this,
          Centre = this.$injector.get('Centre'),
          counts = this.createCentreCounts(1, 2, 3),
          listOptions = {};

      spyOn(self.CentreCounts, 'get').and.callFake(function () {
        return self.$q.when(counts);
      });

      spyOn(Centre, 'list').and.callFake(function () {});

      self.createController(counts);
      self.scope.vm.updateCentres(listOptions);
      self.scope.$digest();

      expect(Centre.list).toHaveBeenCalledWith(listOptions);
    });


  });

});
