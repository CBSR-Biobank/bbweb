// Jasmine test suite
//
define(['angular', 'angularMocks', 'biobankApp'], function(angular, mocks) {
  'use strict';

  describe('Controller: CentreSummaryTabCtrl', function() {
    var centresService, modalService, scope;
    var centre = {name: 'CTR1', description: 'some description'};

    beforeEach(mocks.module('biobankApp'));

    beforeEach(inject(function($q, $controller, $rootScope, $filter, _centresService_, _modalService_) {
      scope = $rootScope.$new();
      centresService = _centresService_;
      modalService = _modalService_;

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

      beforeEach(inject(function($q) {
        spyOn(centresService, 'get').and.callFake(function () {
          var deferred = $q.defer();
          deferred.resolve(centre);
          return deferred.promise;
        });
      }));

      function checkStatusChange($q, status) {
        spyOn(centresService, status).and.callFake(function () {
          var deferred = $q.defer();
          deferred.resolve('status changed');
          return deferred.promise;
        });

        scope.vm.changeStatus(status);
        scope.$digest();
        expect(centresService[status]).toHaveBeenCalledWith(centre);
      }

      it('should enable a centre', inject(function($q) {
        checkStatusChange($q, 'enable');
      }));

      it('should disable a centre', inject(function($q) {
        checkStatusChange($q, 'disable');
      }));

    });

  });

});
