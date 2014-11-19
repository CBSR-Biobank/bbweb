// Jasmine test suite
//
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  describe('Controller: StudySummaryTabCtrl', function() {
    var studiesService, modalService, scope;
    var study = {name: 'ST1', description: 'some description'};

    beforeEach(mocks.module('biobankApp'));

    beforeEach(inject(function($q, $controller, $rootScope, $filter, _studiesService_, _modalService_) {
      scope = $rootScope.$new();
      studiesService = _studiesService_;
      modalService = _modalService_;

      spyOn(modalService, 'showModal').and.callFake(function () {
        var deferred = $q.defer();
        deferred.resolve('modalResult');
        return deferred.promise;
      });

      $controller('StudySummaryTabCtrl as vm', {
        $scope:  scope,
        $filter: $filter,
        study:   study
      });
      scope.$digest();
    }));

    it('should contain valid settings to display the study summary', function() {
      expect(scope.vm.study).toBe(study);
      expect(scope.vm.descriptionToggleLength).toBeDefined();
    });

    describe('change study status', function() {

      beforeEach(inject(function($q) {
        spyOn(studiesService, 'get').and.callFake(function () {
          var deferred = $q.defer();
          deferred.resolve(study);
          return deferred.promise;
        });
      }));

      function checkStatusChange($q, status) {
        spyOn(studiesService, status).and.callFake(function () {
          var deferred = $q.defer();
          deferred.resolve('status changed');
          return deferred.promise;
        });

        scope.vm.changeStatus(status);
        scope.$digest();
        expect(studiesService[status]).toHaveBeenCalledWith(study);
      };

      it('should enable a study', inject(function($q) {
        checkStatusChange($q, 'enable');
      }));

      it('should disable a study', inject(function($q) {
        checkStatusChange($q, 'disable');
      }));

      it('should retire a study', inject(function($q) {
        checkStatusChange($q, 'retire');
      }));

      it('should unretire a study', inject(function($q) {
        checkStatusChange($q, 'unretire');
      }));

    });

  });

});
