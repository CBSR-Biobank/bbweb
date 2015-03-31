/**
 * Jasmine test suite
 */
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  describe('Controller: CeventTypeEditCtrl', function() {
    var q,
        rootScope,
        controller,
        state,
        CollectionEventType,
        domainEntityService,
        notificationsService,
        fakeEntities,
        study,
        ceventType,
        specimenGroups,
        annotationTypes;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($q,
                               $rootScope,
                               $controller,
                               $state,
                               _CollectionEventType_,
                               _domainEntityService_,
                               _notificationsService_,
                               fakeDomainEntities) {
      q                    = $q;
      rootScope            = $rootScope;
      controller           = $controller;
      state                = $state;
      CollectionEventType  = _CollectionEventType_;
      domainEntityService  = _domainEntityService_;
      notificationsService = _notificationsService_;
      fakeEntities         = fakeDomainEntities;
    }));

    function createDependantObjs() {
      study = fakeEntities.study();
      specimenGroups = [ fakeEntities.specimenGroup(study) ];
      annotationTypes = [ fakeEntities.studyAnnotationType(study) ];
    }

    function createCollectionEventType(options) {
      var cet;
      options = options || {};

      createDependantObjs();
      if (options.noId) {
        cet = new CollectionEventType(_.omit(fakeEntities.collectionEventType(study), 'id'));
      } else {
        cet = new CollectionEventType(fakeEntities.collectionEventType(study));
      }
      cet.studySpecimenGroups(specimenGroups);
      cet.studyAnnotationTypes(annotationTypes);
      return cet;
    }

    /**
     * If 'cet' is undefined, a new collection event is created and assigned to global var 'ceventType'.
     */
    function createController(cet) {
      var scope = rootScope.$new();

      if (_.isUndefined(cet)) {
        cet = createCollectionEventType();
        ceventType = cet;
      }

      controller('CeventTypeEditCtrl as vm', {
        $scope:       scope,
        CollectionEventType:  CollectionEventType,
        domainEntityService:  domainEntityService,
        notificationsService: notificationsService,
        ceventType:           cet,
        studySpecimenGroups:  specimenGroups
      });

      scope.$digest();
      return scope;
    }

    describe('has valid scope when created', function () {

      it('for new collection event type', function() {
        var scope;

        ceventType = createCollectionEventType({ noId: true });
        scope = createController(ceventType);

        expect(scope.vm.ceventType).toBe(ceventType);
        expect(scope.vm.studySpecimenGroups).toBe(specimenGroups);
        expect(scope.vm.title).toBe('Add Collection Event Type');
      });

      it('for existing collection event type', function() {
        var scope = createController();

        expect(scope.vm.ceventType).toBe(ceventType);
        expect(scope.vm.studySpecimenGroups).toBe(specimenGroups);
        expect(scope.vm.title).toBe('Update Collection Event Type');
      });

    });

    it('can submit a collection event type', function() {
      var scope = createController();

      spyOn(ceventType, 'addOrUpdate').and.callFake(function () {
        return q.when(fakeEntities.collectionEventType(
          study, {
            specimenGroups: specimenGroups,
            annotationTypes: annotationTypes
          }));
      });
      spyOn(notificationsService, 'submitSuccess').and.callFake(function () {});
      spyOn(state, 'go').and.callFake(function () {});

      scope.vm.submit(ceventType);
      scope.$digest();

      expect(notificationsService.submitSuccess).toHaveBeenCalled();
      expect(state.go).toHaveBeenCalledWith(
        'home.admin.studies.study.collection', {}, {reload: true});
    });

    it('on submit error, displays an error modal', function() {
      var scope = createController();

      spyOn(ceventType, 'addOrUpdate').and.callFake(function () {
        var deferred = q.defer();
        deferred.reject('xxx');
        return deferred.promise;
      });
      spyOn(domainEntityService, 'updateErrorModal').and.callFake(function () {});

      scope.vm.submit(ceventType);
      scope.$digest();

      expect(domainEntityService.updateErrorModal).toHaveBeenCalledWith('xxx', 'collection event type');
    });

    it('when user presses the cancel button, goes to correct state', function() {
      var scope = createController();
      spyOn(state, 'go').and.callFake(function () {});
      scope.vm.cancel();
      scope.$digest();
      expect(state.go).toHaveBeenCalledWith(
        'home.admin.studies.study.collection', {}, {reload: true});
    });

    it('can add specimen group data', function() {
      var study = fakeEntities.study(),
          ceventType,
          scope;

      ceventType = new CollectionEventType(_.omit(fakeEntities.collectionEventType(study), 'id')),
      scope = createController(ceventType);

      expect(scope.vm.ceventType.specimenGroupData).toBeArrayOfSize(0);
      scope.vm.addSpecimenGroupData();
      expect(scope.vm.ceventType.specimenGroupData).toBeArrayOfSize(1);
    });

    it('can remove specimen group data', function() {
      var study = fakeEntities.study(),
          ceventType,
          scope;

      ceventType = new CollectionEventType(_.omit(fakeEntities.collectionEventType(study), 'id')),
      scope = createController(ceventType);

      scope.vm.addSpecimenGroupData();
      scope.vm.addSpecimenGroupData();
      expect(scope.vm.ceventType.specimenGroupData).toBeArrayOfSize(2);

      scope.vm.removeSpecimenGroupData(0);
      expect(scope.vm.ceventType.specimenGroupData).toBeArrayOfSize(1);
    });

    it('removing a specimen group data with invalid index throws an error', function() {
      var study = fakeEntities.study(),
          ceventType,
          scope;

      ceventType = new CollectionEventType(_.omit(fakeEntities.collectionEventType(study), 'id')),
      scope = createController(ceventType);

      expect(function () { scope.vm.removeSpecimenGroupData(-1); })
        .toThrow(new Error('index is invalid: -1'));

      expect(function () { scope.vm.removeSpecimenGroupData(1); })
        .toThrow(new Error('index is invalid: 1'));
    });

    it('can add annotation type data', function() {
      var study = fakeEntities.study(),
          ceventType,
          scope;

      ceventType = new CollectionEventType(_.omit(fakeEntities.collectionEventType(study), 'id')),
      scope = createController(ceventType);

      expect(scope.vm.ceventType.annotationTypeData).toBeArrayOfSize(0);
      scope.vm.addAnnotationTypeData();
      expect(scope.vm.ceventType.annotationTypeData).toBeArrayOfSize(1);
    });

    it('can remove annotation type data', function() {
      var study = fakeEntities.study(),
          ceventType,
          scope;

      ceventType = new CollectionEventType(_.omit(fakeEntities.collectionEventType(study), 'id')),
      scope = createController(ceventType);

      scope.vm.addAnnotationTypeData();
      scope.vm.addAnnotationTypeData();
      expect(scope.vm.ceventType.annotationTypeData).toBeArrayOfSize(2);

      scope.vm.removeAnnotationTypeData(0);
      expect(scope.vm.ceventType.annotationTypeData).toBeArrayOfSize(1);
    });

    it('removing an annotation type data with invalid index throws an error', function() {
      var study = fakeEntities.study(),
          ceventType,
          scope;

      ceventType = new CollectionEventType(_.omit(fakeEntities.collectionEventType(study), 'id')),
      scope = createController(ceventType);

      expect(function () { scope.vm.removeAnnotationTypeData(-1); })
        .toThrow(new Error('index is invalid: -1'));

      expect(function () { scope.vm.removeAnnotationTypeData(1); })
        .toThrow(new Error('index is invalid: 1'));
    });

    it('getSpecimenGroupUnits returns valid results', function() {
      var scope = createController(),
          badSgId = fakeEntities.stringNext();

      expect(function () {
        scope.vm.getSpecimenGroupUnits(badSgId);
      }).toThrow(new Error('specimen group ID not found: ' + badSgId));

      expect(scope.vm.getSpecimenGroupUnits(specimenGroups[0].id))
        .toBe(specimenGroups[0].units);

      expect(scope.vm.getSpecimenGroupUnits()).toBe('Amount');
    });


  });

});
