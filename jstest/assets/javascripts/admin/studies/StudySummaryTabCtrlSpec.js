// Jasmine test suite
//
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  ddescribe('Controller: StudySummaryTabCtrl', function() {
    var scope;
    var study = {name: 'ST1', description: 'some description'};

    beforeEach(mocks.module('biobankApp'));

    beforeEach(inject(function($controller, $rootScope, $filter) {
      scope = $rootScope.$new();

      $controller('StudySummaryTabCtrl as vm', {
        $scope:  scope,
        $filter: $filter,
        study:   study
      });
      scope.$digest();
    }));

    it('should contain valid settings to display a study', function() {
      expect(scope.vm.study).toBe(study);
      expect(scope.vm.description).toContain(study.description);
      expect(scope.vm.descriptionToggle).toBe(true);
    });

    xit('should allow toggling length of description', function() {
    });

  });

});
