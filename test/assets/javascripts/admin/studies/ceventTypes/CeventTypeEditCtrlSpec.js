/**
 * Jasmine test suite
 */
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  fdescribe('Controller: CeventTypeEditCtrl', function() {
    var q,
        rootScope,
        controller,
        state,
        CollectionEventType,
        CollectionEventAnnotationType,
        domainEntityService,
        notificationsService,
        fakeEntities;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($q,
                               $rootScope,
                               $controller,
                               $state,
                               _CollectionEventType_,
                               _CollectionEventAnnotationType_,
                               _domainEntityService_,
                               _notificationsService_,
                               fakeDomainEntities) {
      q                             = $q;
      rootScope                     = $rootScope;
      controller                    = $controller;
      state                         = $state;
      CollectionEventType           = _CollectionEventType_;
      CollectionEventAnnotationType = _CollectionEventAnnotationType_;
      domainEntityService           = _domainEntityService_;
      notificationsService          = _notificationsService_;
      fakeEntities                  = fakeDomainEntities;
    }));

    function createEntities(options) {
      var study, specimenGroups, annotationTypes, ceventType;

      options = options || {};

      study = fakeEntities.study();
      specimenGroups = [ fakeEntities.specimenGroup(study) ];
      annotationTypes = _.map(
          ['Text', 'Number', 'DateTime', 'Select'],
          function(valueType) {
            return new CollectionEventAnnotationType(
              fakeEntities.studyAnnotationType(
                study, { valueType: valueType }));
          });

      if (options.noCetId) {
        ceventType = new CollectionEventType(_.omit(fakeEntities.collectionEventType(study), 'id'));
      } else {
        ceventType = new CollectionEventType(fakeEntities.collectionEventType(study));
      }
      ceventType.studySpecimenGroups(specimenGroups);
      ceventType.studyAnnotationTypes(annotationTypes);

      return {
        study:           study,
        specimenGroups:  specimenGroups,
        annotationTypes: annotationTypes,
        ceventType:      ceventType
      };
    }

    /**
     *
     */
    function createController(entities) {
      var scope = rootScope.$new();

      controller('CeventTypeEditCtrl as vm', {
        $scope:               scope,
        CollectionEventType:  CollectionEventType,
        domainEntityService:  domainEntityService,
        notificationsService: notificationsService,
        ceventType:           entities.ceventType,
        studySpecimenGroups:  entities.specimenGroups
      });

      scope.$digest();
      return scope;
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
        var entities = createEntities(),
            scope = createController(entities);

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
      var entities = createEntities(),
          scope = createController(entities);

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
      var entities = createEntities(),
          scope = createController(entities);

      spyOn(state, 'go').and.callFake(function () {});
      scope.vm.cancel();
      scope.$digest();
      expect(state.go).toHaveBeenCalledWith(
        'home.admin.studies.study.collection', {}, {reload: true});
    });

    it('can add specimen group data', function() {
      var entities = createEntities({ noCetId: true }),
          scope = createController(entities);

      expect(scope.vm.ceventType.specimenGroupData).toBeArrayOfSize(0);
      scope.vm.addSpecimenGroupData();
      expect(scope.vm.ceventType.specimenGroupData).toBeArrayOfSize(1);
    });

    it('can remove specimen group data', function() {
      var entities = createEntities({ noCetId: true }),
          scope = createController(entities);

      scope.vm.addSpecimenGroupData();
      scope.vm.addSpecimenGroupData();
      expect(scope.vm.ceventType.specimenGroupData).toBeArrayOfSize(2);

      scope.vm.removeSpecimenGroupData(0);
      expect(scope.vm.ceventType.specimenGroupData).toBeArrayOfSize(1);
    });

    it('removing a specimen group data with invalid index throws an error', function() {
      var entities = createEntities({ noCetId: true }),
          scope = createController(entities);

      expect(function () { scope.vm.removeSpecimenGroupData(-1); })
        .toThrow(new Error('index is invalid: -1'));

      expect(function () { scope.vm.removeSpecimenGroupData(1); })
        .toThrow(new Error('index is invalid: 1'));
    });

    it('can add annotation type data', function() {
      var entities = createEntities({ noCetId: true }),
          scope = createController(entities);

      expect(scope.vm.ceventType.annotationTypeData).toBeArrayOfSize(0);
      scope.vm.addAnnotationTypeData();
      expect(scope.vm.ceventType.annotationTypeData).toBeArrayOfSize(1);
    });

    it('can remove annotation type data', function() {
      var entities = createEntities({ noCetId: true }),
          scope = createController(entities);

      scope.vm.addAnnotationTypeData();
      scope.vm.addAnnotationTypeData();
      expect(scope.vm.ceventType.annotationTypeData).toBeArrayOfSize(2);

      scope.vm.removeAnnotationTypeData(0);
      expect(scope.vm.ceventType.annotationTypeData).toBeArrayOfSize(1);
    });

    it('removing an annotation type data with invalid index throws an error', function() {
      var entities = createEntities({ noCetId: true }),
          scope = createController(entities);


      expect(function () { scope.vm.removeAnnotationTypeData(-1); })
        .toThrow(new Error('index is invalid: -1'));

      expect(function () { scope.vm.removeAnnotationTypeData(1); })
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
