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

  describe('Controller: SpcLinkTypesPanelCtrl', function() {
    var createEntities,
        createController;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function ($injector) {
      createEntities = setupEntities($injector);
      createController = setupController($injector);
      testUtils.addCustomMatchers();
    }));

    function setupEntities(injector) {
      var Study                      = injector.get('Study'),
          ProcessingType             = injector.get('ProcessingType'),
          SpecimenLinkAnnotationType = injector.get('SpecimenLinkAnnotationType'),
          AnnotationValueType        = injector.get('AnnotationValueType'),
          SpecimenLinkType           = injector.get('SpecimenLinkType'),
          fakeEntities               = injector.get('fakeDomainEntities');

      return create;

      //--

      function create(options) {
        var entities = {};

        options = options || {
          studyHasSpecimenGroups: true,
          studyHasAnnotationTypes: true
        };

        entities.study = new Study(fakeEntities.study());
        entities.processingTypes = _.map(_.range(2), function () {
          return new ProcessingType(fakeEntities.processingType(entities.study));
        });

        if (options.studyHasSpecimenGroups) {
          entities.specimenGroups = _.map(_.range(2), function () {
            return fakeEntities.specimenGroup(entities.study);
          });
        } else {
          entities.specimenGroups = [];
        }

        if (options.studyHasAnnotationTypes) {
          entities.annotationTypes = _.map(
            AnnotationValueType.values(),
            function(valueType) {
              return new SpecimenLinkAnnotationType(
                fakeEntities.studyAnnotationType(
                  entities.study, { valueType: valueType }));
            });
          entities.annotationTypeIdsInUse = [entities.annotationTypes[0]];
        } else {
          entities.annotationTypes = [];
        }

        entities.specimenLinkTypes = _.map(_.range(2), function () {
          var slt = new SpecimenLinkType(fakeEntities.processingType(entities.study));
          if (options.studyHasSpecimenGroups) {
            slt.studySpecimenGroups(entities.specimenGroups);
          }
          if (options.studyHasAnnotationTypes) {
            slt.studyAnnotationTypes(entities.annotationTypes);
          }
          return slt;
        });

        return entities;
      }
    }

    function setupController(injector) {
      var rootScope                  = injector.get('$rootScope'),
          controller                 = injector.get('$controller'),
          state                      = injector.get('$state'),
          modalService               = injector.get('modalService'),
          tableService               = injector.get('tableService'),
          domainEntityService        = injector.get('domainEntityService'),
          Panel                      = injector.get('Panel'),
          SpecimenLinkType           = injector.get('SpecimenLinkType'),
          SpecimenLinkAnnotationType = injector.get('SpecimenLinkAnnotationType'),
          SpcLinkTypeViewer          = injector.get('SpcLinkTypeViewer'),
          ProcessingTypeViewer       = injector.get('ProcessingTypeViewer'),
          SpecimenGroupViewer        = injector.get('SpecimenGroupViewer'),
          AnnotationTypeViewer       = injector.get('AnnotationTypeViewer');

      return create;

      //--

      function create(entities) {
        var scope = rootScope.$new();

        scope.study             = entities.study;
        scope.processingDto = {
          processingTypes:             entities.processingTypes,
          specimenGroups:              entities.specimenGroups,
          specimenLinkAnnotationTypes: entities.annotationTypes,
          specimenLinkTypes:           entities.specimenLinkTypes
        };

        controller('SpcLinkTypesPanelCtrl as vm', {
          $scope:                     scope,
          $state:                     state,
          modalService:               modalService,
          tableService:               tableService,
          domainEntityService:        domainEntityService,
          Panel:                      Panel,
          SpecimenLinkType:           SpecimenLinkType,
          SpecimenLinkAnnotationType: SpecimenLinkAnnotationType,
          SpcLinkTypeViewer:          SpcLinkTypeViewer,
          ProcessingTypeViewer:       ProcessingTypeViewer,
          SpecimenGroupViewer:        SpecimenGroupViewer,
          AnnotationTypeViewer:       AnnotationTypeViewer
        });
        scope.$digest();
        return scope;
      }
    }

    it('has valid scope', function () {
      var entities = createEntities(),
          scope = createController(entities);

      expect(scope.vm.study).toBe(entities.study);
      _.each(entities.processingTypes, function (pt) {
        expect(scope.vm.processingTypesById[pt.id]).toBe(pt);
      });
      _.each(entities.specimenGroups, function (sg) {
        expect(scope.vm.specimenGroupsById[sg.id]).toBe(sg);
      });
      _.each(entities.annotationTypes, function (at) {
        expect(scope.vm.annotationTypesById[at.id]).toBe(at);
      });
      expect(scope.vm.specimenLinkTypes).toBeArrayOfSize(entities.specimenLinkTypes.length);
      expect(scope.vm.specimenLinkTypes).toContainAll(entities.specimenLinkTypes);
      expect(scope.vm.tableParams).toBeDefined();
    });

    it('cannot add a specimen link type if study has no specimen groups', function() {
      var modalService = this.$injector.get('modalService'),
          entities = createEntities({ studyHasSpecimenGroups: false,
                                      studyHasAnnotationTypes: false
                                    }),
          scope = createController(entities);

      spyOn(modalService, 'modalOk').and.callFake(function () {});
      scope.vm.add();
      scope.$digest();
      expect(modalService.modalOk).toHaveBeenCalled();
    });

    it('can add specimen link type', function() {
      var state = this.$injector.get('$state'),
          entities = createEntities(),
          scope = createController(entities);

      spyOn(state, 'go').and.callFake(function () {});
      scope.vm.add();
      scope.$digest();
      expect(state.go).toHaveBeenCalledWith(
        'home.admin.studies.study.processing.spcLinkTypeAdd');
    });

    it('can view information for a processing type', function() {
      var EntityViewer = this.$injector.get('EntityViewer'),
          entities = createEntities(),
          scope = createController(entities);

      spyOn(EntityViewer.prototype, 'showModal').and.callFake(function () { });
      scope.vm.viewProcessingType(entities.processingTypes[0].id);
      scope.$digest();
      expect(EntityViewer.prototype.showModal).toHaveBeenCalled();
    });

    it('can view information for a specimen group', function() {
      var EntityViewer = this.$injector.get('EntityViewer'),
          entities = createEntities(),
          scope = createController(entities);

      spyOn(EntityViewer.prototype, 'showModal').and.callFake(function () { });
      scope.vm.viewSpecimenGroup(entities.specimenGroups[0].id);
      scope.$digest();
      expect(EntityViewer.prototype.showModal).toHaveBeenCalled();
    });

    it('can view information for an annotation type', function() {
      var EntityViewer = this.$injector.get('EntityViewer'),
          entities = createEntities(),
          scope = createController(entities);

      spyOn(EntityViewer.prototype, 'showModal').and.callFake(function () { });
      scope.vm.viewAnnotationType(entities.annotationTypes[0].id);
      scope.$digest();
      expect(EntityViewer.prototype.showModal).toHaveBeenCalled();
    });

    it('cannot update a specimen link type if study is not disabled', function() {
      var StudyStatus = this.$injector.get('StudyStatus'),
          entities    = createEntities(),
          scope       = createController(entities),
          statuses    = [StudyStatus.ENABLED(), StudyStatus.RETIRED()];

      _.each(statuses, function (status) {
        scope.study.status = status;

        expect(function () { scope.vm.update(entities.specimenLinkTypes[0]); }).
          toThrow(new Error('study is not disabled'));
      });
    });

    it('can update a specimen link type', function() {
      var state = this.$injector.get('$state'),
          entities = createEntities(),
          scope = createController(entities);

      spyOn(state, 'go').and.callFake(function () {});

      scope.vm.update(entities.specimenLinkTypes[0]);
      scope.$digest();

      expect(state.go).toHaveBeenCalledWith(
        'home.admin.studies.study.processing.spcLinkTypeUpdate', {
          procTypeId: entities.specimenLinkTypes[0].processingTypeId,
          spcLinkTypeId: entities.specimenLinkTypes[0].id });
    });

    it('cannot remove a specimen link type if study is not disabled', function() {
      var StudyStatus = this.$injector.get('StudyStatus'),
          entities    = createEntities(),
          scope       = createController(entities),
          statuses    = [StudyStatus.ENABLED(), StudyStatus.RETIRED()];

      _.each(statuses, function (status) {
        scope.study.status = status;

        expect(function () { scope.vm.remove(entities.specimenLinkTypes[0]); }).
          toThrow(new Error('study is not disabled'));
      });
    });

    /**
     * A spy is needed on scope.vm.tableParams due to a bug in ng-table.
     */
    it('can remove a specimen link type', function() {
      var q                   = this.$injector.get('$q'),
          domainEntityService = this.$injector.get('domainEntityService'),
          entities            = createEntities(),
          scope               = createController(entities),
          sltToRemove         = entities.specimenLinkTypes[0];

      spyOn(domainEntityService, 'removeEntity').and.callFake(function () {
        return q.when('OK');
      });
      spyOn(scope.vm.tableParams, 'reload').and.callFake(function () {});
      scope.vm.remove(sltToRemove);
      scope.$digest();
      expect(domainEntityService.removeEntity).toHaveBeenCalled();
      expect(scope.vm.specimenLinkTypes).toBeArrayOfSize(entities.specimenLinkTypes.length - 1);
    });

    /**
     * A spy is needed on scope.vm.tableParams due to a bug in ng-table.
     */
    it('displays a modal if removal of a specimen link type fails', function() {
      var q                   = this.$injector.get('$q'),
          modalService        = this.$injector.get('modalService'),
          entities            = createEntities(),
          scope               = createController(entities),
          sltToRemove         = entities.specimenLinkTypes[0];

      spyOn(sltToRemove, 'remove').and.callFake(function () {
        var deferred = q.defer();
        deferred.reject('error');
        return deferred.promise;
      });
      spyOn(modalService, 'showModal').and.callFake(function () {
        return q.when('OK');
      });
      spyOn(scope.vm.tableParams, 'reload').and.callFake(function () {});

      scope.vm.remove(sltToRemove);
      scope.$digest();
      expect(modalService.showModal.calls.count()).toBe(2);
    });

  });

});
