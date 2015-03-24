// Jasmine test suite
//
define(['angular', 'angularMocks', 'biobankApp'], function(angular, mocks) {
  'use strict';

  describe('Controller: StudyEditCtrl', function() {

    var scope,
        state,
        studiesService,
        notificationsService,
        domainEntityUpdateError,
        Study,
        newStudy,
        studyWithId;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($state,
                               _studiesService_,
                               _notificationsService_,
                               _domainEntityUpdateError_,
                               _Study_,
                              fakeDomainEntities) {
      state                   = $state;
      studiesService          = _studiesService_;
      notificationsService    = _notificationsService_;
      domainEntityUpdateError = _domainEntityUpdateError_;
      Study                   = _Study_;

      newStudy = new Study();
      studyWithId = new Study(fakeDomainEntities.study());

      spyOn(state, 'go');
      spyOn(domainEntityUpdateError, 'handleErrorNoStateChange');
    }));

    describe('when adding a study', function() {

      beforeEach(inject(function($controller, $rootScope) {
        scope = $rootScope.$new();

        $controller('StudyEditCtrl as vm', {
          $scope:                  scope,
          $state:                  state,
          notificationsService:    notificationsService,
          domainEntityUpdateError: domainEntityUpdateError,
          study:                   newStudy
        });
        scope.$digest();
      }));

      it('should contain valid settings to add a study', function() {
        expect(scope.vm.study).toEqual(newStudy);
        expect(scope.vm.title).toContain('Add');
        expect(scope.vm.returnState.name).toBe('home.admin.studies');
        expect(scope.vm.returnState.params).toEqual({} );
      });

      it('should return to valid state on cancel', function() {
        scope.vm.cancel();
        expect(state.go).toHaveBeenCalledWith('home.admin.studies', {}, { reload: false });
      });

      it('should return to valid state on submit', inject(function($q) {
        var study = new Study();

        spyOn(studiesService, 'addOrUpdate').and.callFake(function () {
          var deferred = $q.defer();
          deferred.resolve('xxx');
          return deferred.promise;
        });

        scope.vm.submit(study);
        scope.$digest();
        expect(state.go).toHaveBeenCalledWith('home.admin.studies', {}, {reload: true});
      }));

      it('should return to valid state on invalid submit', inject(function($q) {
        var study = new Study();

        spyOn(studiesService, 'addOrUpdate').and.callFake(function () {
          var deferred = $q.defer();
          deferred.reject('xxx');
          return deferred.promise;
        });

        scope.vm.submit(study);
        scope.$digest();
        expect(domainEntityUpdateError.handleErrorNoStateChange).toHaveBeenCalledWith('xxx', 'study');
      }));

    });

    describe('when updating a study', function() {

      beforeEach(inject(function($controller,
                                 $rootScope) {
        scope = $rootScope.$new();

        $controller('StudyEditCtrl as vm', {
          $scope:                  scope,
          $state:                  state,
          notificationsService:    notificationsService,
          domainEntityUpdateError: domainEntityUpdateError,
          study:                   studyWithId
        });
        scope.$digest();
      }));


      it('should contain valid settings to update a study', function() {
        expect(scope.vm.study).toEqual(studyWithId);
        expect(scope.vm.title).toContain('Update');
        expect(scope.vm.returnState.name).toBe('home.admin.studies.study.summary');
        expect(scope.vm.returnState.params).toEqual({studyId: studyWithId.id});
      });

      it('should return to valid state on cancel', function() {
        scope.vm.cancel();
        expect(state.go).toHaveBeenCalledWith(
          'home.admin.studies.study.summary', {studyId: studyWithId.id}, {reload: false});
      });

      it('should return to valid state on submit', inject(function($q) {
        spyOn(studiesService, 'addOrUpdate').and.callFake(function () {
          var deferred = $q.defer();
          deferred.resolve('xxx');
          return deferred.promise;
        });

        scope.vm.submit(studyWithId);
        scope.$digest();
        expect(state.go).toHaveBeenCalledWith(
          'home.admin.studies.study.summary', {studyId: studyWithId.id}, {reload: true});
      }));

      it('should return to valid state on invalid submit', inject(function($q) {
        spyOn(studiesService, 'addOrUpdate').and.callFake(function () {
          var deferred = $q.defer();
          deferred.reject('xxx');
          return deferred.promise;
        });

        scope.vm.submit(studyWithId);
        scope.$digest();
        expect(domainEntityUpdateError.handleErrorNoStateChange)
          .toHaveBeenCalledWith('xxx', 'study');
      }));

    });

  });

});
