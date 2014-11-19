// Jasmine test suite
//
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  describe('Controller: SpcLinkAnnotTypesPanelCtrl', function() {
    var scope, state, modalService, spcLinkAnnotTypesService;
    var spcLinkAnnotTypeRemoveService, annotTypeModalService, panelService;
    var panelFns;
    var study = {id: 'study-id', name: 'ST1'};
    var annotTypes = [
      {id: 'dummy-annot-type-id-1', name: 'CEAT1', valueType: 'DateTime'},
      {id: 'dummy-annot-type-id-2', name: 'CEAT2', valueType: 'Name'},
    ];
    var spcLinkTypes = [
      {
        id: 'dummy-spc-link-type-id',
        name: 'SLT1',
        annotationTypeData: [
          {annotationTypeId: annotTypes[0].id}
        ]
      }
    ];

    beforeEach(mocks.module('biobankApp'));

    beforeEach(inject(function(_modalService_,
                               _spcLinkAnnotTypesService_,
                               _spcLinkAnnotTypeRemoveService_,
                               _annotTypeModalService_,
                               _panelService_) {
      state = jasmine.createSpyObj('state', ['go']);

      modalService =                  _modalService_;
      spcLinkAnnotTypesService =      _spcLinkAnnotTypesService_;
      spcLinkAnnotTypeRemoveService = _spcLinkAnnotTypeRemoveService_;
      annotTypeModalService =         _annotTypeModalService_;
      panelService =                  _panelService_;

      panelFns = jasmine.createSpyObj('panelFns', [
        'information',
        'add',
        'panelToggle',
        'getTableParams'
      ]);

      spyOn(panelService, 'panel').and.callFake(function () {
        return {
          information: panelFns.information,
          add: panelFns.add,
          panelOpen: true,
          panelToggle: panelFns.panelToggle,
          getTableParams: panelFns.getTableParams
        };
      });

    }));

    beforeEach(inject(function($controller, $rootScope, $filter) {
      scope = $rootScope.$new();
      scope.study = study;
      scope.annotTypes = annotTypes;
      scope.spcLinkTypes = spcLinkTypes;

      $controller('SpcLinkAnnotTypesPanelCtrl as vm', {
        $scope:                        scope,
        $state:                        state,
        modalService:                  modalService,
        spcLinkAnnotTypesService:      spcLinkAnnotTypesService,
        spcLinkAnnotTypeRemoveService: spcLinkAnnotTypeRemoveService,
        annotTypeModalService:         annotTypeModalService,
        panelService:                  panelService
      });
      scope.$digest();
    }));

    it('has valid scope', function() {
      expect(scope.vm.annotTypes).toBe(annotTypes);
      expect(scope.vm.annotTypesInUse).toEqual([annotTypes[0].id]);
      expect(scope.vm.columns).toBeArrayOfSize(3);
      expect(scope.vm.columns[0].title).toBe('Name');
      expect(scope.vm.columns[1].title).toBe('Type');
      expect(scope.vm.columns[2].title).toBe('Description');
      expect(scope.vm.tableParams).not.toBeNull();
    });

    it('on update should open a modal to display annotation type in use modal', function() {
      spyOn(modalService, 'modalOk');
      scope.vm.update(annotTypes[0]);
      expect(modalService.modalOk).toHaveBeenCalled();
    });

    it('on update should change state to update an annotation type', function() {
      spyOn(modalService, 'modalOk');
      scope.vm.update(annotTypes[1]);
      expect(state.go).toHaveBeenCalledWith(
        'admin.studies.study.processing.spcLinkAnnotTypeUpdate',
        { annotTypeId: annotTypes[1].id });
    });

    it('on remove opens a modal to display annotation type in use modal', function() {
      spyOn(modalService, 'modalOk');
      scope.vm.remove(annotTypes[0]);
      expect(modalService.modalOk).toHaveBeenCalled();
    });

    it('on update should change state to update an annotation type', function() {
      spyOn(spcLinkAnnotTypeRemoveService, 'remove');
      scope.vm.remove(annotTypes[1]);
      expect(spcLinkAnnotTypeRemoveService.remove).toHaveBeenCalled();
    });

    describe('for the panel service', function() {

      it('should invoke information function', function() {
        scope.vm.information();
        expect(panelFns.information).toHaveBeenCalled();
      });

      it('should invoke add function', function() {
        scope.vm.add();
        expect(panelFns.add).toHaveBeenCalled();
      });

      it('should invoke panelToggle function', function() {
        scope.vm.panelToggle();
        expect(panelFns.panelToggle).toHaveBeenCalled();
      });

      it('should invoke getTableParams function', function() {
        expect(panelFns.getTableParams).toHaveBeenCalled();
      });

      it('should return the panels open state', function() {
        var panelState = scope.vm.panelOpen;
        expect(panelState).toEqual(true);
      });

    });


  });

});
