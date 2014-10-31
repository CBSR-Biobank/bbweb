// Jasmine test suite
//
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  ddescribe('Controller: StudiesTableCtrl', function() {
    var scope, tableService;

    var studies = [
      {name: 'ST1'},
      {name: 'ST2'}
    ];

    beforeEach(mocks.module('biobankApp', function($provide) {
      tableService = {
        settings: function () {
          return {};
        },
        getTableParams: function(studies) {
          return this.settings;
        }
      };

      spyOn(tableService, 'getTableParams').and.callThrough();
      $provide.value('tableService', tableService);
    }));

    beforeEach(inject(function($controller, $rootScope) {
      scope = $rootScope.$new();
      $controller('StudiesTableCtrl as vm', {$scope: scope, tableService: tableService, studies: studies });
      scope.$digest();
    }));

    it('should contain all studies on startup', function() {
      expect(scope.vm.studies).toEqual(studies);
      expect(tableService.getTableParams).toHaveBeenCalledWith(studies);
    });

  });

});
