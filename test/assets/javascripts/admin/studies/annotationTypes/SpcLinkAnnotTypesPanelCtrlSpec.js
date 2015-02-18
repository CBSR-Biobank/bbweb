// Jasmine test suite
//
define(
  [
    'angular',
    'angularMocks',
    'underscore',
    'biobank.fakeDomainEntities',
    'biobank.panelTests',
    'biobankApp'
  ],
  function(angular, mocks, _, fakeEntities, commonTests) {
    'use strict';

    describe('Controller: SpcLinkAnnotTypesPanelCtrl', function() {
      var scope, state, modal, modalService, spcLinkAnnotTypesService;
      var spcLinkAnnotTypeRemoveService, AnnotationTypeViewer, Panel;
      var study = fakeEntities.study();
      var annotTypes = _.map(['Text', 'Number', 'DateTime', 'Select'], function(valueType) {
        return fakeEntities.studyAnnotationType(study, {valueType: valueType});
      });
      var processingType = fakeEntities.processingType(study);
      var spcLinkTypes = _.map(_.range(2), function() {
        return fakeEntities.specimenLinkType(processingType, {annotationTypes: [annotTypes[0]]});
      });

      beforeEach(mocks.module('biobankApp'));

      beforeEach(inject(function($modal,
                                 _modalService_,
                                 _spcLinkAnnotTypesService_,
                                 _spcLinkAnnotTypeRemoveService_,
                                 _AnnotationTypeViewer_,
                                 _Panel_) {
        state = jasmine.createSpyObj('state', ['go']);

        modal                         = $modal;
        modalService                  = _modalService_;
        spcLinkAnnotTypesService      = _spcLinkAnnotTypesService_;
        spcLinkAnnotTypeRemoveService = _spcLinkAnnotTypeRemoveService_;
        AnnotationTypeViewer          = _AnnotationTypeViewer_;
        Panel                         = _Panel_;

        spyOn(Panel.prototype, 'getTableParams').and.callThrough();
      }));

      beforeEach(inject(function($window, $controller, $rootScope) {
        $window.localStorage.setItem('study.panel.specimenLinkAnnotationTypes', '');

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
          Panel:                         Panel,
          AnnotationTypeViewer:          AnnotationTypeViewer
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
          'home.admin.studies.study.processing.spcLinkAnnotTypeUpdate',
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

      describe('for the panel', function() {

        it('should invoke information function', function() {
          commonTests.information(scope, modal, annotTypes[0]);
        });

        it('should invoke add function', function() {
          commonTests.addItem(scope, Panel);
        });

        it('should invoke getTableParams function', function() {
          expect(Panel.prototype.getTableParams).toHaveBeenCalled();
        });

        it('panel should be open when created', function() {
          commonTests.panelInitialState(scope);
        });

        it('should allow toggling of the panel', function() {
          commonTests.panelToggle(scope, Panel);
        });

      });


    });

  });
