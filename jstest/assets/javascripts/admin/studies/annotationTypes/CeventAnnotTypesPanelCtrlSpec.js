// Jasmine test suite
//
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  describe('Controller: CeventAnnotTypesPanelCtrl', function() {
    var scope;
    var annotTypes = [
      {id: 'dummy-annot-type-id-1', name: 'CEAT1', valueType: 'DateTime'},
      {id: 'dummy-annot-type-id-2', name: 'CEAT2', valueType: 'Name'},
    ];

    // this collection event type uses only the first annotation type defined above
    var ceventTypes = [
      {
        id: 'dummy-cevent-type',
        annotationTypeData: [
          {annotationTypeId: 'dummy-annot-type-id-1'}
        ]
      }
    ];

    beforeEach(mocks.module('biobankApp'));

    beforeEach(inject(function($controller, $rootScope, $filter) {
      scope = $rootScope.$new();
      scope.annotTypes = annotTypes;

      $controller('CeventAnnotTypesPanelCtrl as vm', {
        $scope:                      scope,
        $state:                      $state,
        modalService:                modalService,
        ceventAnnotTypesService:     ceventAnnotTypesService,
        annotationTypeRemoveService: annotationTypeRemoveService,
        annotTypeModalService:       annotTypeModalService,
        panelService:                panelService
      });
      scope.$digest();
    }));

  });

});
