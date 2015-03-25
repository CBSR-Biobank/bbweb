// Jasmine test suite
//
define(['angular', 'angularMocks', 'biobankApp'], function(angular, mocks) {
  'use strict';

  describe('Controller: StudySummaryTabCtrl', function() {
    var q, Study, modalService, scope, study;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($q,
                               $controller,
                               $rootScope,
                               $state,
                               _Study_,
                               _modalService_,
                               fakeDomainEntities) {
      q            = $q;
      scope        = $rootScope.$new();
      Study        = _Study_;
      modalService = _modalService_;

      spyOn(modalService, 'showModal').and.callFake(function () {
        var deferred = $q.defer();
        deferred.resolve('modalResult');
        return deferred.promise;
      });

      study = new Study(fakeDomainEntities.study());

      $controller('StudySummaryTabCtrl as vm', {
        $scope:       scope,
        $state:       $state,
        modalService: modalService,
        study:        study
      });
      scope.$digest();
    }));

    it('should contain valid settings to display the study summary', function() {
      expect(scope.vm.study).toBe(study);
      expect(scope.vm.descriptionToggleLength).toBeDefined();
    });

    describe('change study status', function() {

      beforeEach(function () {
        spyOn(Study, 'get').and.callFake(function () {
          var deferred = q.defer();
          deferred.resolve(study);
          return deferred.promise;
        });
      });

      function spyOnStudyStatusChangeAndResolve(status) {
        spyOn(Study.prototype, status).and.callFake(function () {
          var deferred = q.defer();
          deferred.resolve('status changed');
          return deferred.promise;
        });
      }

      function checkStatusChange(status) {
        spyOnStudyStatusChangeAndResolve(status);

        scope.vm.changeStatus(status);
        scope.$digest();
        expect(Study.prototype[status]).toHaveBeenCalled();
      }

      it('should enable a study', function() {
        checkStatusChange('enable');
      });

      it('should disable a study', function () {
        checkStatusChange('disable');
      });

      it('should retire a study', function () {
        checkStatusChange('retire');
      });

      it('should unretire a study', function () {
        checkStatusChange('unretire');
      });

      it('when changing status, should throw error for invalid status', function () {
        var badStatus = 'xxx';
        expect(function () {
          scope.vm.changeStatus(badStatus);
        }).toThrow(new Error('invalid status: ' + badStatus));
      });

    });

  });

});
