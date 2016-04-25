/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
// Jasmine test suite
//
define(['angular', 'angularMocks', 'biobankApp'], function(angular, mocks) {
  'use strict';

  describe('Controller: CentreSummaryTabCtrl', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($rootScope, $controller, $filter, modalService) {
      var self = this;

      self.$q           = self.$injector.get('$q');
      self.Centre       = self.$injector.get('Centre');
      self.CentreStatus = self.$injector.get('CentreStatus');
      self.jsonEntities = self.$injector.get('jsonEntities');

      self.centre = new self.Centre(self.jsonEntities.centre());

      spyOn(modalService, 'showModal').and.callFake(function () {
        return self.$q.when('modalResult');
      });

      self.createController = createController;

      //--

      function createController(centre) {
        self.scope = $rootScope.$new();
        $controller('CentreSummaryTabCtrl as vm', {
          $scope:  self.scope,
          $filter: $filter,
          centre:  centre
        });
        self.scope.$digest();
      }
    }));

    it('should contain valid settings to display the centre summary', function() {
      this.createController(this.centre);
      expect(this.scope.vm.centre).toBe(this.centre);
      expect(this.scope.vm.descriptionToggleLength).toBeDefined();
    });

    describe('change centre status', function() {

      function checkStatusChange(centre, status, newStatus) {
        /* jshint validthis: true */
        var self = this;

        self.createController(self.centre);

        spyOn(self.Centre.prototype, status).and.callFake(function () {
          centre.status = (centre.status === self.CentreStatus.ENABLED) ?
            self.CentreStatus.DISABLED : self.CentreStatus.ENABLED;
          return self.$q.when(centre);
        });

        self.scope.vm.changeStatus(status);
        self.scope.$digest();
        expect(self.Centre.prototype[status]).toHaveBeenCalled();
        expect(self.scope.vm.centre.status).toBe(newStatus);
      }

      it('should enable a centre', function() {
        checkStatusChange.call(this, this.centre, 'enable', this.CentreStatus.ENABLED);
      });

      it('should disable a centre', function() {
        this.centre.status = this.CentreStatus.ENABLED;
        checkStatusChange.call(this, this.centre, 'disable', this.CentreStatus.DISABLED);
      });

    });

  });

});
