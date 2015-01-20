// Jasmine test suite
//
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  describe('Controller: StudiesCtrl', function() {
    var scope;

    var studies = [
      {name: 'ST1'},
      {name: 'ST2'}
    ];

    function generatePagedResults() {
      return {
        items: studies,
        page: 1,
        pageSize: 5,
        maxPages: 1,
        prev: null,
        next: 2,
        offset: 0,
        total: 10
      };
    }

    beforeEach(mocks.module('biobankApp'));

    beforeEach(inject(function($controller, $rootScope) {
      scope = $rootScope.$new();

      $controller('StudiesCtrl as vm', {
        $scope: scope,
        paginatedStudies: generatePagedResults()
      });

      scope.$digest();
    }));

    it('should contain all studies on startup', function() {
      //expect(_.difference(studies, scope.studies)).toEqual([]);

      expect(scope.vm.studies).toEqual(studies);
    });

  });

});
