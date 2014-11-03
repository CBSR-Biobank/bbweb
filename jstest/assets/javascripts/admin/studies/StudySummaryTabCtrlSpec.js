// Jasmine test suite
//
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  describe('Controller: StudySummaryTabCtrl', function() {
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

    it('should contain valid settings to display the study summary', function() {
      expect(scope.vm.study).toBe(study);
      expect(scope.vm.descriptionToggleLength).toBeDefined();
    });

    xit('should change the status for a study', function() {

    });


  });

});
