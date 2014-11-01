// Jasmine test suite
//
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  ddescribe('Controller: StudyEditCtrl', function() {
    var scope;
    var study = {name: 'ST1'};

    beforeEach(mocks.module('biobankApp'));

    beforeEach(inject(function($q, stateHelper, studiesService, domainEntityUpdateError) {
      spyOn(stateHelper, 'reloadStateAndReinit');
      spyOn(domainEntityUpdateError, 'handleError');

      spyOn(studiesService, 'addOrUpdate').and.callFake(function () {
        var deferred = $q.defer();
        deferred.resolve('xxx');
        return deferred.promise;
      });

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
      //expect(tableService.getTableParams).toHaveBeenCalledWith(studies);
    });

    it('should contain valid settings to update a study',
       inject(function($controller,
                       $rootScope,
                       stateHelper,
                       studiesService,
                       domainEntityUpdateError) {
         var study = {id: 'dummy-id', name: 'ST1'};
         scope = $rootScope.$new();

         $controller('StudyEditCtrl as vm', {
           $scope:                  scope,
           stateHelper:             stateHelper,
           studiesService:          studiesService,
           domainEntityUpdateError: domainEntityUpdateError,
           study:                   study
         });
         scope.$digest();

         expect(scope.vm.study).toBe(study);
         expect(scope.vm.title).toContain('Update');
         expect(scope.vm.returnState).toBe('admin.studies.study.summary');
         expect(scope.vm.stateParams).toEqual({studyId: study.id});
       }));


  });

});
