/**
 * Jasmine test suite
 */
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  describe('Controller: CeventTypeEditCtrl', function() {
    var createEntities,
        createController,
        fakeEntities;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($injector,
                               fakeDomainEntities) {
      fakeEntities = fakeDomainEntities;
      createEntities = setupEntities($injector);
      createController = setupController($injector);
    }));

    function setupEntities(injector) {
      var Study                         = injector.get('Study'),
          CollectionEventType           = injector.get('CollectionEventType'),
          CollectionEventAnnotationType = injector.get('CollectionEventAnnotationType'),
          AnnotationValueType           = injector.get('AnnotationValueType');

      return create;

      //--

      function create(options) {
        var study, specimenGroups, annotationTypes, ceventType, serverCet;

        options = options || {};

        study = fakeEntities.study();
        specimenGroups = _.map(_.range(2), function () {
          return fakeEntities.specimenGroup(study);
        });
        annotationTypes = _.map(
          AnnotationValueType.values(),
          function(valueType) {
            return new CollectionEventAnnotationType(
              fakeEntities.studyAnnotationType(
                study, { valueType: valueType }));
          });

        serverCet = fakeEntities.collectionEventType(study, {
          specimenGroups: specimenGroups,
          annotationTypes: annotationTypes
        });

        if (options.noCetId) {
          ceventType = new CollectionEventType(_.omit(serverCet, 'id'));
        } else {
          ceventType = new CollectionEventType(serverCet);
        }
        ceventType.studySpecimenGroups(specimenGroups);
        ceventType.studyAnnotationTypes(annotationTypes);

        return {
          study:           new Study(study),
          specimenGroups:  specimenGroups,
          annotationTypes: annotationTypes,
          ceventType:      ceventType
        };
      }
    }

    function setupController(injector) {
      var rootScope            = injector.get('$rootScope'),
          controller           = injector.get('$controller'),
          state                = injector.get('$state'),
          CollectionEventType  = injector.get('CollectionEventType'),
          domainEntityService  = injector.get('domainEntityService'),
          notificationsService = injector.get('notificationsService');

      return create;

      //--

      function create(entities) {
        var scope = rootScope.$new();

        controller('CeventTypeEditCtrl as vm', {
          $scope:               scope,
          CollectionEventType:  CollectionEventType,
          domainEntityService:  domainEntityService,
          notificationsService: notificationsService,
          study:                entities.study,
          ceventType:           entities.ceventType,
          studySpecimenGroups:  entities.specimenGroups,
          studyAnnotationTypes: entities.annotationTypes
        });

        scope.$digest();
        return scope;
      }
    }

    describe('has valid scope when created', function () {

      it('for new collection event type', function() {
        var entities = createEntities({ noCetId: true }),
            scope = createController(entities);

        expect(scope.vm.ceventType).toBe(entities.ceventType);
        expect(scope.vm.studySpecimenGroups).toBe(entities.specimenGroups);
        expect(scope.vm.title).toBe('Add Collection Event Type');
      });

      it('for existing collection event type', function() {
        var entities = createEntities(),
            scope = createController(entities);

        expect(scope.vm.ceventType).toBe(entities.ceventType);
        expect(scope.vm.studySpecimenGroups).toBe(entities.specimenGroups);
        expect(scope.vm.title).toBe('Update Collection Event Type');
      });

    });

    it('can submit a collection event type', function() {
      var q                    = this.$injector.get('$q'),
          notificationsService = this.$injector.get('notificationsService'),
          state                = this.$injector.get('$state'),
          entities             = createEntities(),
          scope                = createController(entities);

      spyOn(entities.ceventType, 'addOrUpdate').and.callFake(function () {
        return q.when(fakeEntities.collectionEventType(
          entities.study, {
            specimenGroups: entities.specimenGroups,
            annotationTypes: entities.annotationTypes
          }));
      });
      spyOn(notificationsService, 'submitSuccess').and.callFake(function () {});
      spyOn(state, 'go').and.callFake(function () {});

      scope.vm.submit(entities.ceventType);
      scope.$digest();

      expect(notificationsService.submitSuccess).toHaveBeenCalled();
      expect(state.go).toHaveBeenCalledWith(
        'home.admin.studies.study.collection', {}, {reload: true});
    });

    it('on submit error, displays an error modal', function() {
      var q                   = this.$injector.get('$q'),
          domainEntityService = this.$injector.get('domainEntityService'),
          state               = this.$injector.get('$state'),
          entities            = createEntities(),
          scope               = createController(entities);

      spyOn(entities.ceventType, 'addOrUpdate').and.callFake(function () {
        var deferred = q.defer();
        deferred.reject('xxx');
        return deferred.promise;
      });
      spyOn(domainEntityService, 'updateErrorModal').and.callFake(function () {});

      scope.vm.submit(entities.ceventType);
      scope.$digest();

      expect(domainEntityService.updateErrorModal).toHaveBeenCalledWith('xxx', 'collection event type');
    });

    it('when user presses the cancel button, goes to correct state', function() {
      var state    = this.$injector.get('$state'),
          entities = createEntities(),
          scope    = createController(entities);

      spyOn(state, 'go').and.callFake(function () {});
      scope.vm.cancel();
      scope.$digest();
      expect(state.go).toHaveBeenCalledWith(
        'home.admin.studies.study.collection', {}, {reload: true});
    });

    it('can add specimen group data', function() {
      var CollectionEventType = this.$injector.get('CollectionEventType'),
          entities = createEntities(),
          scope;

      entities.ceventType = new CollectionEventType(
        fakeEntities.collectionEventType(entities.study));

      scope = createController(entities);

      expect(scope.vm.ceventType.specimenGroupData).toBeArrayOfSize(0);
      scope.vm.addSpecimenGroup();
      expect(scope.vm.ceventType.specimenGroupData).toBeArrayOfSize(1);
    });

    it('can remove specimen group data', function() {
      var CollectionEventType = this.$injector.get('CollectionEventType'),
          entities = createEntities(), scope;

      entities.ceventType = new CollectionEventType(
        fakeEntities.collectionEventType(entities.study));

      scope = createController(entities);

      scope.vm.addSpecimenGroup();
      scope.vm.addSpecimenGroup();
      expect(scope.vm.ceventType.specimenGroupData).toBeArrayOfSize(2);

      scope.vm.removeSpecimenGroup(0);
      expect(scope.vm.ceventType.specimenGroupData).toBeArrayOfSize(1);
    });

    it('removing a specimen group data with invalid index throws an error', function() {
      var CollectionEventType = this.$injector.get('CollectionEventType'),
          entities = createEntities(), scope;

      entities.ceventType = new CollectionEventType(
        fakeEntities.collectionEventType(entities.study));

      scope = createController(entities);

      expect(function () { scope.vm.removeSpecimenGroup(-1); })
        .toThrow(new Error('index is invalid: -1'));

      expect(function () { scope.vm.removeSpecimenGroup(1); })
        .toThrow(new Error('index is invalid: 1'));
    });

    it('can add an annotation type', function() {
      var CollectionEventType = this.$injector.get('CollectionEventType'),
          entities = createEntities({ noCetId: true }), scope;

      entities.ceventType = new CollectionEventType(
        fakeEntities.collectionEventType(entities.study));
      scope = createController(entities);

      expect(scope.vm.ceventType.annotationTypeData).toBeArrayOfSize(0);
      scope.vm.addAnnotationType();
      expect(scope.vm.ceventType.annotationTypeData).toBeArrayOfSize(1);
    });

    it('can remove an annotation type', function() {
      var CollectionEventType = this.$injector.get('CollectionEventType'),
          entities = createEntities({ noCetId: true }), scope;

      entities.ceventType = new CollectionEventType(
        fakeEntities.collectionEventType(entities.study));
      scope = createController(entities);

      scope.vm.addAnnotationType();
      scope.vm.addAnnotationType();
      expect(scope.vm.ceventType.annotationTypeData).toBeArrayOfSize(2);

      scope.vm.removeAnnotationType(0);
      expect(scope.vm.ceventType.annotationTypeData).toBeArrayOfSize(1);
    });

    it('removing an annotation type data with invalid index throws an error', function() {
      var CollectionEventType = this.$injector.get('CollectionEventType'),
          entities = createEntities({ noCetId: true }), scope;

      entities.ceventType = new CollectionEventType(
        fakeEntities.collectionEventType(entities.study));
      scope = createController(entities);

      expect(function () { scope.vm.removeAnnotationType(-1); })
        .toThrow(new Error('index is invalid: -1'));

      expect(function () { scope.vm.removeAnnotationType(1); })
        .toThrow(new Error('index is invalid: 1'));
    });

    it('getSpecimenGroupUnits returns valid results', function() {
      var entities = createEntities({ noCetId: true }),
          scope = createController(entities),
          badSgId = fakeEntities.stringNext();

      expect(function () {
        scope.vm.getSpecimenGroupUnits(badSgId);
      }).toThrow(new Error('specimen group ID not found: ' + badSgId));

      expect(scope.vm.getSpecimenGroupUnits(entities.specimenGroups[0].id))
        .toBe(entities.specimenGroups[0].units);

      expect(scope.vm.getSpecimenGroupUnits()).toBe('Amount');
    });

  });

});
