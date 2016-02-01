/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
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
    var createEntities,
        createController;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function ($injector) {
      createEntities   = setupEntities($injector);
      createController = setupController($injector);

      testUtils.addCustomMatchers();
    }));

    function setupEntities(injector) {
      var Study                         = injector.get('Study'),
          CollectionEventType           = injector.get('CollectionEventType'),
          SpecimenGroup                 = injector.get('SpecimenGroup'),
          CollectionEventAnnotationType = injector.get('CollectionEventAnnotationType'),
          AnnotationValueType           = injector.get('AnnotationValueType'),
          fakeEntities                  = injector.get('fakeDomainEntities');

      return create;

      //--

      function create(options) {
        var entities = {};

        options = options || {
          studyHasSpecimenGroups: true,
          studyHasAnnotationTypes: true
        };

        entities.study = new Study(fakeEntities.study());

        if (options.studyHasSpecimenGroups) {
          entities.specimenGroups = _.map(_.range(2), function () {
            return new SpecimenGroup(fakeEntities.specimenGroup(entities.study));
          });
        } else {
          entities.specimenGroups = [];
        }

        if (options.studyHasAnnotationTypes) {
          entities.annotationTypes = _.map(
            AnnotationValueType.values(),
            function(valueType) {
              return new CollectionEventAnnotationType(
                fakeEntities.studyAnnotationType(
                  entities.study, { valueType: valueType }));
            });
          entities.annotationTypeIdsInUse = [entities.annotationTypes[0]];
        } else {
          entities.annotationTypes = [];
        }

        entities.ceventTypes = _.map(_.range(2), function () {
          var serverObj =
              fakeEntities.collectionEventType(entities.study, {
                specimenGroups: entities.specimenGroups,
                annotationTypes: entities.annotationTypes
              });
          return new CollectionEventType(serverObj, {
            studySpecimenGroups: entities.specimenGroups,
            studyAnnotationTypes: entities.annotationTypes
          });
        });

        return entities;
      }
    }

    function setupController(injector) {
      var rootScope            = injector.get('$rootScope'),
          controller           = injector.get('$controller'),
          state                = injector.get('$state'),
          modalService         = injector.get('modalService'),
          Panel                = injector.get('Panel'),
          CollectionEventType  = injector.get('CollectionEventType'),
          CeventTypeViewer     = injector.get('CeventTypeViewer'),
          AnnotationTypeViewer = injector.get('AnnotationTypeViewer'),
          SpecimenGroupViewer  = injector.get('SpecimenGroupViewer'),
          domainEntityService  = injector.get('domainEntityService');

      return create;

      //--
      function create(entities) {
        var scope = rootScope.$new();

        scope.study           = entities.study;
        scope.specimenGroups  = entities.specimenGroups;
        scope.annotationTypes = entities.annotationTypes;
        scope.ceventTypes     = entities.ceventTypes;

        controller('CeventTypesPanelCtrl as vm', {
          $scope:               scope,
          $state:               state,
          modalService:         modalService,
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
    }

    it('has valid scope', function () {
      var entities = createEntities(),
          scope = createController(entities);

      expect(scope.vm.study).toBe(scope.study);
      _.each(scope.specimenGroups, function (sg) {
        expect(scope.vm.specimenGroupsById[sg.id]).toBe(sg);
      });
      _.each(scope.annotationTypes, function (at) {
        expect(scope.vm.annotationTypesById[at.id]).toBe(at);
      });
      expect(scope.vm.ceventTypes).toBeArrayOfSize(scope.ceventTypes.length);
      expect(scope.vm.ceventTypes).toContainAll(scope.ceventTypes);
    });

    it('cannot add a collection event type if study has no specimen groups', function() {
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

    it('can add collection event type', function() {
      var state = this.$injector.get('$state'),
          entities = createEntities(),
          scope = createController(entities);

      spyOn(state, 'go').and.callFake(function () {});

      scope.vm.add();
      scope.$digest();

      expect(state.go).toHaveBeenCalledWith(
        'home.admin.studies.study.collection.ceventTypeAdd');
    });

    it('can view information for a collection event type', function() {
      var EntityViewer = this.$injector.get('EntityViewer'),
          entities = createEntities(),
          scope = createController(entities);

      spyOn(EntityViewer.prototype, 'showModal').and.callFake(function () { });
      scope.vm.information(scope.vm.ceventTypes[0]);
      scope.$digest();
      expect(EntityViewer.prototype.showModal).toHaveBeenCalled();
    });

    it('can view information for a specimen group', function() {
      var EntityViewer = this.$injector.get('EntityViewer'),
          entities = createEntities(),
          scope = createController(entities);

      spyOn(EntityViewer.prototype, 'showModal').and.callFake(function () { });
      scope.vm.viewSpecimenGroup(scope.specimenGroups[0].id);
      scope.$digest();
      expect(EntityViewer.prototype.showModal).toHaveBeenCalled();
    });

    it('can view information for an annotation type', function() {
      var EntityViewer = this.$injector.get('EntityViewer'),
          entities = createEntities(),
          scope = createController(entities);

      spyOn(EntityViewer.prototype, 'showModal').and.callFake(function () { });
      scope.vm.viewAnnotationType(scope.annotationTypes[0].id);
      scope.$digest();
      expect(EntityViewer.prototype.showModal).toHaveBeenCalled();
    });

    it('cannot update a collection event type if study is not disabled', function() {
      var StudyStatus = this.$injector.get('StudyStatus'),
          entities = createEntities(),
          scope = createController(entities),
          statuses = [StudyStatus.ENABLED(), StudyStatus.RETIRED()];

      _.each(statuses, function (status) {
        scope.study.status = status;

        expect(function () { scope.vm.update(scope.ceventTypes[0]); }).
          toThrow(new Error('study is not disabled'));
      });
    });

    it('can update a collection event type', function() {
      var state = this.$injector.get('$state'),
          entities = createEntities(),
          scope = createController(entities);

      spyOn(state, 'go').and.callFake(function () {});

      scope.vm.update(scope.ceventTypes[0]);
      scope.$digest();

      expect(state.go).toHaveBeenCalledWith(
        'home.admin.studies.study.collection.ceventTypeUpdate',
        { ceventTypeId: scope.ceventTypes[0].id });
    });

    it('cannot remove a collection event type if study is not disabled', function() {
      var StudyStatus = this.$injector.get('StudyStatus'),
          entities = createEntities(),
          scope = createController(entities),
          statuses = [StudyStatus.ENABLED(), StudyStatus.RETIRED()];

      _.each(statuses, function (status) {
        scope.study.status = status;

        expect(function () { scope.vm.remove(scope.ceventTypes[0]); }).
          toThrow(new Error('study is not disabled'));
      });
    });

    it('can remove a collection event type', function() {
      var q                   = this.$injector.get('$q'),
          domainEntityService = this.$injector.get('domainEntityService'),
          entities            = createEntities(),
          scope               = createController(entities),
          cetToRemove         = entities.ceventTypes[1];

      spyOn(domainEntityService, 'removeEntity').and.callFake(function () {
        return q.when('OK');
      });
      scope.vm.remove(cetToRemove);
      scope.$digest();
      expect(domainEntityService.removeEntity).toHaveBeenCalled();
      expect(scope.vm.ceventTypes).toBeArrayOfSize(entities.ceventTypes.length - 1);
    });

    it('displays a modal if removal of a collection event type fails', function() {
      var q                   = this.$injector.get('$q'),
          modalService        = this.$injector.get('modalService'),
          entities            = createEntities(),
          scope               = createController(entities),
          cetToRemove         = entities.ceventTypes[1];

      spyOn(cetToRemove, 'remove').and.callFake(function () {
        var deferred = q.defer();
        deferred.reject('error');
        return deferred.promise;
      });
      spyOn(modalService, 'showModal').and.callFake(function () {
        return q.when('OK');
      });

      scope.vm.remove(cetToRemove);
      scope.$digest();
      expect(modalService.showModal.calls.count()).toBe(2);
    });

  });

});
