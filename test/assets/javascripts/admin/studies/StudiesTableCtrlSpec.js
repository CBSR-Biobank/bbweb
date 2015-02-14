// Jasmine test suite
//
define(['angular', 'angularMocks', 'biobankApp'], function(angular, mocks) {
  'use strict';

  describe('Controller: StudiesTableCtrl', function() {
    var scope, tableService;

    var studies = [
      {name: 'ST1'},
      {name: 'ST2'}
    ];

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
        studyCount: 1
      });
      scope.$digest();
    }));

    // FIXME - this test no longer valid
    xit('should contain all studies on startup', function() {
      expect(scope.vm.studies).toEqual(studies);
      expect(tableService.getTableParams).toHaveBeenCalled();
    });

  });

});
