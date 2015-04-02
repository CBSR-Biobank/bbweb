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

  describe('Controller: SpcLinkTypeEditCtrl', function() {
    var q,
        rootScope,
        controller,
        state,
        SpecimenLinkType,
        SpecimenLinkAnnotationType,
        domainEntityService,
        notificationsService,
        fakeEntities;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($q,
                               $rootScope,
                               $controller,
                               $state,
                               _SpecimenLinkType_,
                               _SpecimenLinkAnnotationType_,
                               _domainEntityService_,
                               _notificationsService_,
                               fakeDomainEntities) {
      q                          = $q;
      rootScope                  = $rootScope;
      controller                 = $controller;
      state                      = $state;
      SpecimenLinkType           = _SpecimenLinkType_;
      SpecimenLinkAnnotationType = _SpecimenLinkAnnotationType_;
      domainEntityService        = _domainEntityService_;
      notificationsService       = _notificationsService_;
      fakeEntities               = fakeDomainEntities;

      testUtils.addCustomMatchers();
    }));

    function createEntities() {
      var study, processingType, specimenGroups, annotationTypes, specimenLinkType;

      study = fakeEntities.study();
      processingType = fakeEntities.processingType(study);
      specimenGroups = _.map(_.range(2), function () {
        return fakeEntities.specimenGroup(study);
      });
      annotationTypes = _.map(
          ['Text', 'Number', 'DateTime', 'Select'],
          function(valueType) {
            return new SpecimenLinkAnnotationType(
              fakeEntities.studyAnnotationType(
                study, { valueType: valueType }));
          });

      specimenLinkType = new SpecimenLinkType(
        fakeEntities.specimenLinkType(processingType, {
          inputGroup: specimenGroups[0],
          outputGroup: specimenGroups[1],
          annotationTypes: annotationTypes
        }));
      specimenLinkType.studySpecimenGroups(specimenGroups);
      specimenLinkType.studyAnnotationTypes(annotationTypes);

      return {
        study:            study,
        processingType:   processingType,
        specimenGroups:   specimenGroups,
        annotationTypes:  annotationTypes,
        specimenLinkType: specimenLinkType
      };
    }

    function createController(entities) {
      var scope = rootScope.$new();

      controller('SpcLinkTypeEditCtrl as vm', {
        $scope:               scope,
        $state:               state,
        SpecimenLinkType:     SpecimenLinkType,
        domainEntityService:  domainEntityService,
        notificationsService: notificationsService,
        study:                entities.study,
        spcLinkType:          entities.specimenLinkType,
        processingDto:        {
          processingTypes:             [ entities.processingType ],
          specimenLinkTypes:           [ entities.specimenLinkType ],
          specimenLinkAnnotationTypes: entities.annotationTypes,
          specimenGroups:              entities.specimenGroups
        }
      });

      scope.$digest();
      return scope;
    }

    describe('has valid scope when created', function () {

      it('for new specimen link type', function() {
        var entities = createEntities({ noCetId: true }), scope;

        entities.specimenLinkType = new SpecimenLinkType(
          _.omit(fakeEntities.specimenLinkType(entities.processingType), 'id'));

        scope = createController(entities);

        expect(scope.vm.title).toBe('Add Specimen Link Type');
        initScopeCommon(entities, scope);
      });

      it('for existing specimen link type', function() {
        var entities = createEntities(),
            scope = createController(entities);
        expect(scope.vm.title).toBe('Update Specimen Link Type');
        initScopeCommon(entities, scope);
      });

      function initScopeCommon(entities, scope) {
        expect(scope.vm.study).toBe(entities.study);

        expect(scope.vm.processingTypes).toBeArrayOfSize(1);
        expect(scope.vm.processingTypes).toContain(entities.processingType);
        expect(scope.vm.processingTypesById[entities.processingType.id]).toBe(entities.processingType);

        expect(scope.vm.studySpecimenGroups).toBeArrayOfSize(entities.specimenGroups.length);
        expect(scope.vm.studySpecimenGroups).toContainAll(entities.specimenGroups);

        expect(scope.vm.studyAnnotationTypes).toBeArrayOfSize(entities.annotationTypes.length);
        expect(scope.vm.studyAnnotationTypes).toContainAll(entities.annotationTypes);
      }

    });

    it('can submit a specimen link type', function() {
      var entities = createEntities(),
          scope = createController(entities);

      spyOn(entities.specimenLinkType, 'addOrUpdate').and.callFake(function () {
        return q.when(fakeEntities.specimenLinkType(entities.processingType, {
          inputGroup: entities.specimenGroups[0],
          outputGroup: entities.specimenGroups[1],
          annotationTypes: entities.annotationTypes
        }));
      });
      spyOn(state, 'go').and.callFake(function () {});
      scope.vm.submit(entities.specimenLinkType);
      scope.$digest();

      expect(state.go).toHaveBeenCalledWith(
        'home.admin.studies.study.processing', {}, {reload: true});
    });

    it('on submit error, displays an error modal', function() {
      var entities = createEntities(),
          scope = createController(entities);

      spyOn(entities.specimenLinkType, 'addOrUpdate').and.callFake(function () {
        var deferred = q.defer();
        deferred.reject('xxx');
        return deferred.promise;
      });
      spyOn(domainEntityService, 'updateErrorModal').and.callFake(function () {});

      scope.vm.submit(entities.specimenLinkType);
      scope.$digest();

      expect(domainEntityService.updateErrorModal)
        .toHaveBeenCalledWith('xxx', 'collection event type');
    });

    it('when user presses the cancel button, goes to correct state', function() {
      var entities = createEntities(),
          scope = createController(entities);

      spyOn(state, 'go').and.callFake(function () {});
      scope.vm.cancel();
      scope.$digest();
      expect(state.go).toHaveBeenCalledWith(
        'home.admin.studies.study.processing', {}, {reload: true});
    });

    it('can add an annotation type', function() {
      var entities = createEntities(), scope;

     entities.specimenLinkType = new SpecimenLinkType(
        fakeEntities.specimenLinkType(entities.processingType, {
          inputGroup: entities.specimenGroups[0],
          outputGroup: entities.specimenGroups[1]
        }));

      scope = createController(entities);

      expect(scope.vm.specimenLinkType.annotationTypeData).toBeArrayOfSize(0);
      scope.vm.addAnnotationType();
      expect(scope.vm.specimenLinkType.annotationTypeData).toBeArrayOfSize(1);
    });

    it('can remove annotation type', function() {
      var entities = createEntities(), scope;

      entities.specimenLinkType = new SpecimenLinkType(
        fakeEntities.specimenLinkType(entities.processingType, {
          inputGroup: entities.specimenGroups[0],
          outputGroup: entities.specimenGroups[1]
        }));

      scope = createController(entities);

      scope.vm.addAnnotationType();
      scope.vm.addAnnotationType();
      expect(scope.vm.specimenLinkType.annotationTypeData).toBeArrayOfSize(2);

      scope.vm.removeAnnotationType(0);
      expect(scope.vm.specimenLinkType.annotationTypeData).toBeArrayOfSize(1);
    });

    it('removing an annotation type with invalid index throws an error', function() {
      var entities = createEntities(), scope;

     entities.specimenLinkType = new SpecimenLinkType(
        fakeEntities.specimenLinkType(entities.processingType, {
          inputGroup: entities.specimenGroups[0],
          outputGroup: entities.specimenGroups[1]
        }));

      scope = createController(entities);

      expect(function () { scope.vm.removeAnnotationType(-1); })
        .toThrow(new Error('index is invalid: -1'));

      expect(function () { scope.vm.removeAnnotationType(1); })
        .toThrow(new Error('index is invalid: 1'));
    });

    it('getSpecimenGroupUnits returns valid results', function() {
      var entities = createEntities(),
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
