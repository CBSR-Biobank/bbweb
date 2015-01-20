// Jasmine test suite
//
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  describe('Controller: StudiesTableCtrl', function() {
    var scope, tableService;

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

    beforeEach(inject(function (_tableService_) {
      tableService = _tableService_;
      spyOn(tableService, 'getTableParams').and.callFake(function () {
        return {
          settings: function () {
            return {};
          }
        };
      });
    }));

    beforeEach(inject(function($controller, $rootScope, $q, studiesService, tableService) {
      scope = $rootScope.$new();
      $controller('StudiesTableCtrl as vm', {
        $scope: scope,
        $q: $q,
        studiesService: studiesService,
        tableService: tableService,
        paginatedStudies: generatePagedResults()
      });
      scope.$digest();
    }));

    it('should contain all studies on startup', function() {
      expect(scope.vm.studies).toEqual(studies);
      expect(tableService.getTableParams).toHaveBeenCalled();
    });

  });

});
