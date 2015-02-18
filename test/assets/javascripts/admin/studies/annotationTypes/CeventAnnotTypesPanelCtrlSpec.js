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

    describe('Controller: CeventAnnotTypesPanelCtrl', function() {
      var scope, state, modal, modalService, ceventAnnotTypesService;
      var annotationTypeRemoveService, AnnotationTypeViewer, Panel;
      var study = fakeEntities.study();
      var annotTypes = _.map(['Text', 'Number', 'DateTime', 'Select'], function(valueType) {
        return fakeEntities.studyAnnotationType(study, {valueType: valueType});
      });
      var annotTypesInUse = [annotTypes[0].id];

      beforeEach(mocks.module('biobankApp'));

      beforeEach(inject(function($modal,
                                 _modalService_,
                                 _ceventAnnotTypesService_,
                                 _annotationTypeRemoveService_,
                                 _AnnotationTypeViewer_,
                                 _Panel_) {
        state = jasmine.createSpyObj('state', ['go']);

        modal =                        $modal;
        modalService =                 _modalService_;
        ceventAnnotTypesService =      _ceventAnnotTypesService_;
        annotationTypeRemoveService =  _annotationTypeRemoveService_;
        AnnotationTypeViewer          = _AnnotationTypeViewer_;
        Panel                         = _Panel_;

        spyOn(Panel.prototype, 'getTableParams').and.callThrough();
      }));

      beforeEach(inject(function($controller, $rootScope, $window) {
        // initialize the local storage
        $window.localStorage.setItem('study.panel.collectionEventAnnottionTypes', '');

        scope = $rootScope.$new();
        scope.study = study;
        scope.annotTypes = annotTypes;
        scope.annotTypesInUse = annotTypesInUse;

        $controller('CeventAnnotTypesPanelCtrl as vm', {
          $scope:                      scope,
          $state:                      state,
          modalService:                modalService,
          ceventAnnotTypesService:     ceventAnnotTypesService,
          annotationTypeRemoveService: annotationTypeRemoveService,
          Panel:                       Panel,
          AnnotationTypeViewer:        AnnotationTypeViewer
        });
        scope.$digest();
      }));

      it('has valid scope', function() {
        expect(scope.vm.annotTypes).toBe(annotTypes);
        expect(scope.vm.annotTypesInUse).toBe(annotTypesInUse);
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
          'home.admin.studies.study.collection.ceventAnnotTypeUpdate',
          { annotTypeId: annotTypes[1].id });
      });

      it('on remove opens a modal to display annotation type in use modal', function() {
        spyOn(modalService, 'modalOk');
        scope.vm.remove(annotTypes[0]);
        expect(modalService.modalOk).toHaveBeenCalled();
      });

      it('on update should change state to update an annotation type', function() {
        spyOn(annotationTypeRemoveService, 'remove');
        scope.vm.remove(annotTypes[1]);
        expect(annotationTypeRemoveService.remove).toHaveBeenCalled();
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
