// Jasmine test suite
//
define([
  'angular',
  'angularMocks',
  'underscore',
  'biobank.panelTests',
  'biobankApp'
], function(angular, mocks, _, commonTests) {
  'use strict';

  describe('Controller: ParticipantAnnotTypesPanelCtrl', function() {
    var scope, state, modal, modalService, participantAnnotTypesService;
    var annotationTypeRemoveService, AnnotationTypeViewer, Panel, fakeEntities;
    var study, annotTypes, annotTypesInUse;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($modal,
                               _modalService_,
                               _participantAnnotTypesService_,
                               _annotationTypeRemoveService_,
                               _AnnotationTypeViewer_,
                               _Panel_,
                               fakeDomainEntities) {
      state = jasmine.createSpyObj('state', ['go']);

      modal                        = $modal;
      modalService                 = _modalService_;
      participantAnnotTypesService = _participantAnnotTypesService_;
      annotationTypeRemoveService  = _annotationTypeRemoveService_;
      AnnotationTypeViewer         = _AnnotationTypeViewer_;
      Panel                        = _Panel_;
      fakeEntities                 = fakeDomainEntities;

      study = fakeEntities.study();
      annotTypes = _.map(['Text', 'Number', 'DateTime', 'Select'], function(valueType) {
        return fakeEntities.studyAnnotationType(study, {valueType: valueType, required: true});
      });
      annotTypesInUse = [annotTypes[0]];

      spyOn(Panel.prototype, 'getTableParams').and.callThrough();
    }));

    beforeEach(inject(function($window, $controller, $rootScope) {
      $window.localStorage.setItem('study.panel.participantAnnotationTypes', '');

      scope = $rootScope.$new();
      scope.study = study;
      scope.annotTypes = annotTypes;
      scope.annotTypesInUse = annotTypesInUse;

      $controller('ParticipantAnnotTypesPanelCtrl as vm', {
        $scope:                       scope,
        $state:                       state,
        modalService:                 modalService,
        participantAnnotTypesService: participantAnnotTypesService,
        annotationTypeRemoveService:  annotationTypeRemoveService,
        Panel:                        Panel,
        AnnotationTypeViewer:         AnnotationTypeViewer
      });
      scope.$digest();
    }));

    it('has valid scope', function() {
      expect(scope.vm.annotTypes).toBe(annotTypes);
      // Uncomment line below once 'participant annotation types in use' API is enabled.
      //expect(scope.vm.annotTypesInUse).toBe(annotTypesInUse);
      expect(scope.vm.columns).toBeArrayOfSize(4);
      expect(scope.vm.columns[0].title).toBe('Name');
      expect(scope.vm.columns[1].title).toBe('Type');
      expect(scope.vm.columns[2].title).toBe('Required');
      expect(scope.vm.columns[3].title).toBe('Description');
      expect(scope.vm.tableParams).not.toBeNull();
    });

    /**
     * Enable this test once 'participant annotation types in use' API is enabled.
     */
    xit('on update should open a modal to display annotation type in use modal', function() {
      spyOn(modalService, 'modalOk');
      scope.vm.update(annotTypes[0]);
      expect(modalService.modalOk).toHaveBeenCalled();
    });

    it('on update should change state to update an annotation type', function() {
      spyOn(modalService, 'modalOk');
      scope.vm.update(annotTypes[1]);
      expect(state.go).toHaveBeenCalledWith(
        'home.admin.studies.study.participants.annotTypeUpdate',
        { annotTypeId: annotTypes[1].id });
    });

    /**
     * Enable this test once 'participant annotation types in use' API is enabled.
     */
    xit('on remove opens a modal to display annotation type in use modal', function() {
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

    });

  });

});
