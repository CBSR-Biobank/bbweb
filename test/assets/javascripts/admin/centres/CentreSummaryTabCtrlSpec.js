// Jasmine test suite
//
define(['angular', 'angularMocks', 'biobankApp'], function(angular, mocks) {
  'use strict';

  describe('Controller: CentreSummaryTabCtrl', function() {
    var q, Centre, CentreStatus, modalService, scope, fakeEntities, centre;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($q,
                               $controller,
                               $rootScope,
                               $filter,
                               _Centre_,
                               _CentreStatus_,
                               _modalService_,
                               fakeDomainEntities) {
      q = $q;
      scope = $rootScope.$new();
      Centre = _Centre_;
      CentreStatus = _CentreStatus_;
      modalService = _modalService_;
      fakeEntities = fakeDomainEntities;

      centre = new Centre(fakeEntities.centre());

      spyOn(modalService, 'showModal').and.callFake(function () {
        var deferred = $q.defer();
        deferred.resolve('modalResult');
        return deferred.promise;
      });

      $controller('CentreSummaryTabCtrl as vm', {
        $scope:  scope,
        $filter: $filter,
        centre:   centre
      });
      scope.$digest();
    }));

    it('should contain valid settings to display the centre summary', function() {
      expect(scope.vm.centre).toBe(centre);
      expect(scope.vm.descriptionToggleLength).toBeDefined();
    });

    describe('change centre status', function() {

      function checkStatusChange(status, newStatus) {
        spyOn(Centre.prototype, status).and.callFake(function () {
          var deferred = q.defer();
          centre.status = (centre.status === CentreStatus.ENABLED()) ?
            CentreStatus.DISABLED() : CentreStatus.ENABLED();
          deferred.resolve(centre);
          return deferred.promise;
        });

        scope.vm.changeStatus(status);
        scope.$digest();
        expect(Centre.prototype[status]).toHaveBeenCalled();
        expect(scope.vm.centre.status).toBe(newStatus);
      }

      it('should enable a centre', function() {
        checkStatusChange('enable', CentreStatus.ENABLED());
      });

      it('should disable a centre', function() {
        centre.status = CentreStatus.ENABLED();
        checkStatusChange('disable', CentreStatus.DISABLED());
      });

    });

  });

});
