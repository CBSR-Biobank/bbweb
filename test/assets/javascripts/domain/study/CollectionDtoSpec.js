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
  'biobankApp'
], function(angular, mocks, _) {
  'use strict';

  describe('CollectionDto', function() {
    var httpBackend,
        CollectionDto,
        createEntities,
        fakeEntities;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(testUtils) {
      httpBackend   = this.$injector.get('$httpBackend');
      CollectionDto = this.$injector.get('CollectionDto');
      fakeEntities  = this.$injector.get('fakeDomainEntities');
      testUtils.addCustomMatchers();

      createEntities = setupEntities(this.$injector);
    }));

    function setupEntities(injector) {
      var AnnotationValueType = injector.get('AnnotationValueType');

      return create;

      //--
      function create() {
        var entities = {};
        entities.study = fakeEntities.study();
        entities.collectionEventAnnotationTypes = _.map(
          AnnotationValueType.values(),
          function (valueType) {
            return fakeEntities.studyAnnotationType(entities.study, {
              valueType: valueType
            });
        });
        entities.specimenGroups = _.map(_.range(2), function () {
          return fakeEntities.specimenGroup(entities.study);
        });
        entities.collectionEventAnnotationTypeIdsInUse = [ entities.collectionEventAnnotationTypes[0].id ];
        entities.collectionEventTypes = _.map(_.range(2), function () {
          return fakeEntities.collectionEventType(entities.study, {
            specimenGroups: entities.specimenGroups,
            annotationTypes: entities.collectionEventAnnotationTypes
          });
        });
        return entities;
      }
    }

    it('constructor with no parameters has default values', function() {
      var dto = new CollectionDto();

      expect(dto.collectionEventTypes).toBeArrayOfSize(0);
      expect(dto.collectionEventAnnotationTypes).toBeArrayOfSize(0);
      expect(dto.collectionEventAnnotationTypeIdsInUse).toBeArrayOfSize(0);
      expect(dto.specimenGroups).toBeArrayOfSize(0);
    });

    it('fails when creating from a non object', function() {
      expect(CollectionDto.create(1))
        .toEqual(new Error('invalid object from server: must be a map, has the correct keys'));
    });

    it('can retrieve a collection dto', function(done) {
      var CollectionEventType = this.$injector.get('CollectionEventType'),
          CollectionEventAnnotationType = this.$injector.get('CollectionEventAnnotationType'),
          SpecimenGroup = this.$injector.get('SpecimenGroup'),
          entities = createEntities();

      httpBackend.whenGET('/studies/' + entities.study.id + '/dto/collection')
        .respond(serverReply(entities));

      CollectionDto.get(entities.study.id).then(function(dto) {
        compareToServerObj(dto, entities);

        _.each(dto.collectionEventTypes, function(cet) {
          expect(cet).toEqual(jasmine.any(CollectionEventType));
        });

        _.each(dto.collectionEventAnnotationTypes, function(ceat) {
          expect(ceat).toEqual(jasmine.any(CollectionEventAnnotationType));
        });

        _.each(dto.specimenGroups, function(sg) {
          expect(sg).toEqual(jasmine.any(SpecimenGroup));
        });

        done();
      });
      httpBackend.flush();
    });

    function serverReply(obj) {
      return { status: 'success', data: obj };
    }

    function compareToServerObj(dto, serverObj) {
      expect(dto.collectionEventTypes).toBeArrayOfSize(serverObj.collectionEventTypes.length);
      expect(dto.collectionEventAnnotationTypes).toBeArrayOfSize(serverObj.collectionEventAnnotationTypes.length);
      expect(dto.collectionEventAnnotationTypeIdsInUse)
        .toBeArrayOfSize(serverObj.collectionEventAnnotationTypeIdsInUse.length);
      expect(dto.specimenGroups).toBeArrayOfSize(serverObj.specimenGroups.length);

      expect(dto.collectionEventTypes).toContainAll(serverObj.collectionEventTypes);
      expect(dto.collectionEventAnnotationTypes).toContainAll(serverObj.collectionEventAnnotationTypes);
      expect(dto.collectionEventAnnotationTypeIdsInUse)
        .toContainAll(serverObj.collectionEventAnnotationTypeIdsInUse);
      expect(dto.specimenGroups).toContainAll(serverObj.specimenGroups);

      _.each(dto.collectionEventTypes, function (cet) {
        expect(cet.specimenGroupDataIds()).toBeArrayOfSize(serverObj.specimenGroups.length);
        expect(cet.annotationTypeDataIds()).toBeArrayOfSize(serverObj.collectionEventAnnotationTypes.length);

        expect(cet.specimenGroupDataIds()).toContainAll(
          _.pluck(serverObj.specimenGroups, 'id'));
        expect(cet.annotationTypeDataIds()).toContainAll(
          _.pluck(serverObj.collectionEventAnnotationTypes, 'id'));
      });
    }

  });

});
