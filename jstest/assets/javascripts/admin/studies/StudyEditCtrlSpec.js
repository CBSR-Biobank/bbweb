// Jasmine test suite
//
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  describe('Controller: StudyEditCtrl when adding a study', function() {
    var scope, stateHelper, studiesService, domainEntityUpdateError;
    var study = {name: 'ST1'};

    beforeEach(mocks.module('biobankApp'));

    beforeEach(inject(function(_stateHelper_, _studiesService_, _domainEntityUpdateError_) {
      stateHelper = _stateHelper_;
      studiesService = _studiesService_;
      domainEntityUpdateError = _domainEntityUpdateError_;

      spyOn(stateHelper, 'reloadStateAndReinit');
      spyOn(domainEntityUpdateError, 'handleError');
    }));

    beforeEach(inject(function($controller, $rootScope, stateHelper, studiesService, domainEntityUpdateError) {
      scope = $rootScope.$new();

      $controller('StudyEditCtrl as vm', {
        $scope:                  scope,
        stateHelper:             stateHelper,
        studiesService:          studiesService,
        domainEntityUpdateError: domainEntityUpdateError,
        study:                   study
      });
      scope.$digest();
    }));

    it('should contain valid settings to add a study', function() {
      expect(scope.vm.study).toBe(study);
      expect(scope.vm.title).toContain('Add');
      expect(scope.vm.returnState).toBe('admin.studies');
      expect(scope.vm.stateParams).toEqual({});
    });

    it('should return to valid state on cancel', function() {
      scope.vm.cancel();
      expect(stateHelper.reloadStateAndReinit).toHaveBeenCalledWith(
        'admin.studies', {}, {reload: true});
    });

    it('should return to valid state on submit', inject(function($q) {
      spyOn(studiesService, 'addOrUpdate').and.callFake(function () {
        var deferred = $q.defer();
        deferred.resolve('xxx');
        return deferred.promise;
      });

      scope.vm.submit(study);
      scope.$digest();
      expect(stateHelper.reloadStateAndReinit).toHaveBeenCalledWith(
        'admin.studies', {}, {reload: true});
    }));

    it('should return to valid state on invalid submit', inject(function($q) {
      spyOn(studiesService, 'addOrUpdate').and.callFake(function () {
        var deferred = $q.defer();
        deferred.reject('xxx');
        return deferred.promise;
      });

      scope.vm.submit(study);
      scope.$digest();
      expect(domainEntityUpdateError.handleError).toHaveBeenCalledWith(
        'xxx', 'study', 'admin.studies', {}, {reload: true});
    }));

  });

  describe('Controller: StudyEditCtrl when updating a study', function() {
    var scope, stateHelper, studiesService, domainEntityUpdateError;
    var study = {id: 'dummy-id', name: 'ST1'};

    beforeEach(mocks.module('biobankApp'));

    beforeEach(inject(function(_stateHelper_, _studiesService_, _domainEntityUpdateError_) {
      stateHelper = _stateHelper_;
      studiesService = _studiesService_;
      domainEntityUpdateError = _domainEntityUpdateError_;

      spyOn(stateHelper, 'reloadStateAndReinit');
      spyOn(domainEntityUpdateError, 'handleError');
    }));

    beforeEach(inject(function($controller, $rootScope, stateHelper, studiesService, domainEntityUpdateError) {
      scope = $rootScope.$new();

      $controller('StudyEditCtrl as vm', {
        $scope:                  scope,
        stateHelper:             stateHelper,
        studiesService:          studiesService,
        domainEntityUpdateError: domainEntityUpdateError,
        study:                   study
      });
      scope.$digest();
    }));


    it('should contain valid settings to update a study', function() {
      expect(scope.vm.study).toBe(study);
      expect(scope.vm.title).toContain('Update');
      expect(scope.vm.returnState).toBe('admin.studies.study.summary');
      expect(scope.vm.stateParams).toEqual({studyId: study.id});
    });

    it('should return to valid state on cancel', function() {
      scope.vm.cancel();
      expect(stateHelper.reloadStateAndReinit).toHaveBeenCalledWith(
        'admin.studies.study.summary', {studyId: study.id}, {reload: true});
    });

    it('should return to valid state on submit', inject(function($q) {
      spyOn(studiesService, 'addOrUpdate').and.callFake(function () {
        var deferred = $q.defer();
        deferred.resolve('xxx');
        return deferred.promise;
      });

      scope.vm.submit(study);
      scope.$digest();
      expect(stateHelper.reloadStateAndReinit).toHaveBeenCalledWith(
        'admin.studies.study.summary', {studyId: study.id}, {reload: true});
    }));

    it('should return to valid state on invalid submit', inject(function($q) {
      spyOn(studiesService, 'addOrUpdate').and.callFake(function () {
        var deferred = $q.defer();
        deferred.reject('xxx');
        return deferred.promise;
      });

      scope.vm.submit(study);
      scope.$digest();
      expect(domainEntityUpdateError.handleError).toHaveBeenCalledWith(
        'xxx', 'study', 'admin.studies.study.summary', {studyId: study.id}, {reload: true});
    }));

  });

});
