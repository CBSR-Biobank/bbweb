/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
// Jasmine test suite
//
define(['angular', 'angularMocks', 'biobankApp'], function(angular, mocks) {
  'use strict';

  describe('Controller: CentreSummaryTabCtrl', function() {
    var q, Centre, CentreStatus, jsonEntities, createController;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($q,
                               _Centre_,
                               _CentreStatus_,
                               modalService,
                               jsonEntities) {
      q            = $q;
      Centre       = _Centre_;
      CentreStatus = _CentreStatus_;
      jsonEntities = jsonEntities;

      spyOn(modalService, 'showModal').and.callFake(function () {
        return $q.when('modalResult');
      });

      createController = setupController(this.$injector);
    }));

    function setupController(injector) {
      var $rootScope  = injector.get('$rootScope'),
          $controller = injector.get('$controller'),
          $filter     = injector.get('$filter');

      return create;

      //--

      function create(centre) {
        var scope = $rootScope.$new();
        $controller('CentreSummaryTabCtrl as vm', {
          $scope:  scope,
          $filter: $filter,
          centre:   centre
        });
        scope.$digest();
        return scope;
      }
    }

    it('should contain valid settings to display the centre summary', function() {
      var centre = new Centre(jsonEntities.centre()),
          scope = createController(centre);

      expect(scope.vm.centre).toBe(centre);
      expect(scope.vm.descriptionToggleLength).toBeDefined();
    });

    describe('change centre status', function() {

      function checkStatusChange(centre, status, newStatus) {
        var scope = createController(centre);

        spyOn(Centre.prototype, status).and.callFake(function () {
          centre.status = (centre.status === CentreStatus.ENABLED()) ?
            CentreStatus.DISABLED() : CentreStatus.ENABLED();
          return q.when(centre);
        });

        scope.vm.changeStatus(status);
        scope.$digest();
        expect(Centre.prototype[status]).toHaveBeenCalled();
        expect(scope.vm.centre.status).toBe(newStatus);
      }

      it('should enable a centre', function() {
        var centre = new Centre(jsonEntities.centre());
        checkStatusChange(centre, 'enable', CentreStatus.ENABLED());
      });

      it('should disable a centre', function() {
        var centre = new Centre(jsonEntities.centre());
        centre.status = CentreStatus.ENABLED();
        checkStatusChange(centre, 'disable', CentreStatus.DISABLED());
      });

    });

  });

});
