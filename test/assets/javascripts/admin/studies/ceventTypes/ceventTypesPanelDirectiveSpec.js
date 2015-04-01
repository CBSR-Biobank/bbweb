/**
 * Jasmine test suite
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  'biobank.testUtils',
  'biobankApp'
], function(angular, mocks, _, testUtils) {
  'use strict';

  describe('Controller: CeventTypesPanelCtrl', function() {
    var q,
        rootScope,
        controller,
        state,
        modalService,
        tableService,
        EntityViewer,
        Study,
        StudyStatus,
        CollectionEventType,
        CollectionEventAnnotationType,
        Panel,
        CeventTypeViewer,
        AnnotationTypeViewer,
        SpecimenGroupViewer,
        domainEntityService,
        fakeEntities;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function ($q,
                                $rootScope,
                                $controller,
                                $state,
                                _modalService_,
                                _tableService_,
                                _EntityViewer_,
                                _Study_,
                                _StudyStatus_,
                                _CollectionEventType_,
                                _CollectionEventAnnotationType_,
                                _Panel_,
                                _CeventTypeViewer_,
                                _AnnotationTypeViewer_,
                                _SpecimenGroupViewer_,
                                _domainEntityService_,
                                fakeDomainEntities) {
      q                             = $q;
      rootScope                     = $rootScope;
      controller                    = $controller;
      state                         = $state;
      modalService                  = _modalService_;
      tableService                  = _tableService_;
      EntityViewer                  = _EntityViewer_;
      Study                         = _Study_;
      StudyStatus                   = _StudyStatus_;
      CollectionEventType           = _CollectionEventType_;
      CollectionEventAnnotationType = _CollectionEventAnnotationType_;
      Panel                         = _Panel_;
      CeventTypeViewer              = _CeventTypeViewer_;
      AnnotationTypeViewer          = _AnnotationTypeViewer_;
      SpecimenGroupViewer           = _SpecimenGroupViewer_;
      domainEntityService           = _domainEntityService_;
      fakeEntities                  = fakeDomainEntities;

      testUtils.addCustomMatchers();
    }));

    function createController(options) {
      var scope = rootScope.$new();

      options = options || {
        studyHasSpecimenGroups: true,
        studyHasAnnotationTypes: true
      };

      scope.study = new Study(fakeEntities.study());

      if (options.studyHasSpecimenGroups) {
        scope.specimenGroups = _.map(_.range(2), function () {
        return fakeEntities.specimenGroup(scope.study);
        });
      } else {
        scope.specimenGroups = [];
      }

      if (options.studyHasAnnotationTypes) {
        scope.annotationTypes = _.map(
          ['Text', 'Number', 'DateTime', 'Select'],
          function(valueType) {
            return new CollectionEventAnnotationType(
              fakeEntities.studyAnnotationType(
                scope.study, { valueType: valueType }));
          });
        scope.annotationTypeIdsInUse = [scope.annotationTypes[0]];
      } else {
        scope.annotationTypes = [];
      }

      scope.ceventTypes = _.map(_.range(2), function () {
        var serverObj =
          fakeEntities.collectionEventType(scope.study, {
            specimenGroups: scope.specimenGroups,
            annotationTypes: scope.annotationTypes
          });
        return new CollectionEventType(serverObj, {
          studySpecimenGroups: scope.specimenGroups,
          studyAnnotationTypes: scope.annotationTypes
        });
      });

      controller('CeventTypesPanelCtrl as vm', {
        $scope:               scope,
        $state:               state,
        modalService:         modalService,
        tableService:         tableService,
        CollectionEventType:  CollectionEventType,
        Panel:                Panel,
        CeventTypeViewer:     CeventTypeViewer,
        AnnotationTypeViewer: AnnotationTypeViewer,
        SpecimenGroupViewer:  SpecimenGroupViewer,
        domainEntityService:  domainEntityService
      });
      scope.$digest();
      return scope;
    }

    it('has valid scope', function () {
      var scope = createController();

      expect(scope.vm.study).toBe(scope.study);
      _.each(scope.specimenGroups, function (sg) {
        expect(scope.vm.specimenGroupsById[sg.id]).toBe(sg);
      });
      _.each(scope.annotationTypes, function (at) {
        expect(scope.vm.annotationTypesById[at.id]).toBe(at);
      });
      expect(scope.vm.ceventTypes).toBeArrayOfSize(scope.ceventTypes.length);
      expect(scope.vm.ceventTypes).toContainAll(scope.ceventTypes);
      expect(scope.vm.tableParams).toBeDefined();
    });

    it('cannot add a collection event type if study has no specimen groups', function() {
      var scope = createController({
        studyHasSpecimenGroups: false,
        studyHasAnnotationTypes: false
      });

      spyOn(modalService, 'modalOk').and.callFake(function () {});

      scope.vm.add();
      scope.$digest();

      expect(modalService.modalOk).toHaveBeenCalled();
    });

    it('can add collection event', function() {
      var scope = createController();

      spyOn(state, 'go').and.callFake(function () {});

      scope.vm.add();
      scope.$digest();

      expect(state.go).toHaveBeenCalledWith(
        'home.admin.studies.study.collection.ceventTypeAdd');
    });

    it('can view information for a collection event type', function() {
      var scope = createController();

      spyOn(EntityViewer.prototype, 'showModal').and.callFake(function () { });

      scope.vm.information(scope.vm.ceventTypes[0]);
      scope.$digest();

      expect(EntityViewer.prototype.showModal).toHaveBeenCalled();
    });

    it('can view information for a specimen group', function() {
      var scope = createController();

      spyOn(EntityViewer.prototype, 'showModal').and.callFake(function () { });

      scope.vm.viewSpecimenGroup(scope.specimenGroups[0].id);
      scope.$digest();

      expect(EntityViewer.prototype.showModal).toHaveBeenCalled();
    });

    it('can view information for an annotation type', function() {
      var scope = createController();

      spyOn(EntityViewer.prototype, 'showModal').and.callFake(function () { });

      scope.vm.viewAnnotationType(scope.annotationTypes[0].id);
      scope.$digest();

      expect(EntityViewer.prototype.showModal).toHaveBeenCalled();
    });

    it('cannot update a collection event type if study is disabled', function() {
      var scope = createController();
      scope.study.status = StudyStatus.ENABLED();

      expect(function () { scope.vm.update(scope.annotationTypes[0].id); }).
        toThrow(new Error('study is not disabled'));
    });

    it('can update a collection event type', function() {
      var scope = createController();

      spyOn(state, 'go').and.callFake(function () {});

      scope.vm.update(scope.ceventTypes[0]);
      scope.$digest();

      expect(state.go).toHaveBeenCalledWith(
        'home.admin.studies.study.collection.ceventTypeUpdate',
        { ceventTypeId: scope.ceventTypes[0].id });
    });

    it('cannot remove a collection event type if study is disabled', function() {
      var scope = createController();
      scope.study.status = StudyStatus.ENABLED();

      expect(function () { scope.vm.remove(scope.ceventTypes[0]); }).
        toThrow(new Error('study is not disabled'));
    });

    /**
     * Fails right now due to a bug in ng-table.
     */
    xit('can remove a collection event type', function() {
      var scope = createController();

      spyOn(domainEntityService, 'removeEntity').and.callFake(function () {
        return q.when('xxx');
      });

      scope.vm.remove(scope.ceventTypes[0]);
      scope.$digest();

      expect(domainEntityService.removeEntity).toHaveBeenCalled();
    });

  });

});
