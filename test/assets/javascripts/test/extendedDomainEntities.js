/**
 * This service extends the domain classes to add extra functions that can be run in the test environment.
 */
define(['angular', 'underscore'], function(angular, _) {
  'use strict';

  extendedDomainEntities.$inject = [
    'ConcurrencySafeEntity',
    'AnnotationType',
    'Study',
    'SpecimenGroup',
    'StudyAnnotationType',
    'ParticipantAnnotationType',
    'CollectionEventType',
    'ProcessingType',
    'SpecimenLinkType',
    'Participant',
    'Annotation',
    'AnnotationValueType',
    'Centre',
    'Location'
  ];

  /**
   * Extends domain entities by adding a 'compareToServerEntity' that can be called from test code to compare
   * a javascript domain object and a response from the server (for the same domain entity).
   */
  function extendedDomainEntities(ConcurrencySafeEntity,
                                  AnnotationType,
                                  Study,
                                  SpecimenGroup,
                                  StudyAnnotationType,
                                  ParticipantAnnotationType,
                                  CollectionEventType,
                                  ProcessingType,
                                  SpecimenLinkType,
                                  Participant,
                                  Annotation,
                                  AnnotationValueType,
                                  Centre,
                                  Location) {

    /**
     * @param obj the JS domain entity.
     * @param entity the server side entity to compare against.
     * @param attrs* the attributes to compare agains. May be more than one.
     */
    function validateAttrs(/* obj, entity, attrs */) {
      var args = _.toArray(arguments);
      var obj = args.shift();
      var entity = args.shift();

      _.each(args, function(arg) {
        expect(obj[arg]).toEqual(entity[arg]);
      });
    };

    /**
     * @param obj the JS domain entity.
     * @param entity the server side entity to compare against.
     * @param attrs* the attributes to compare agains. May be more than one.
     */
    function validateOptional(/* obj, entity, attrs */) {
      var args = _.toArray(arguments);
      var obj = args.shift();
      var entity = args.shift();

      _.each(args, function(attr) {
        if (obj[attr] !== null) {
          expect(obj[attr]).toEqual(entity[attr]);
        } else {
          expect(entity[attr]).toBeUndefined();
        }
      });
    };

    ConcurrencySafeEntity.prototype.compareToServerEntity = function (serverEntity) {
      expect(this.isNew()).toBe(false);
      validateAttrs(this, serverEntity, 'version');
    };

    AnnotationType.prototype.compareToServerEntity = function (serverEntity) {
      ConcurrencySafeEntity.prototype.compareToServerEntity.call(this, serverEntity);
      validateAttrs(this, serverEntity, 'name', 'valueType', 'options');
      validateOptional(this, serverEntity, 'description', 'maxValueCount');
    };

    StudyAnnotationType.prototype.compareToServerEntity = function (serverEntity) {
      AnnotationType.prototype.compareToServerEntity.call(this, serverEntity);
      validateAttrs(this, serverEntity, 'studyId');
    };

    ParticipantAnnotationType.prototype.compareToServerEntity = function (serverEntity) {
      StudyAnnotationType.prototype.compareToServerEntity.call(this, serverEntity);
      validateAttrs(this, serverEntity, 'required');
    };

    CollectionEventType.compareToServerEntity = function (serverEntity) {
      ConcurrencySafeEntity.prototype.compareToServerEntity.call(this, serverEntity);
      validateAttrs(this, serverEntity, 'name', 'recurring');
      validateOptional(this, serverEntity, 'description');

      expect(this.specimenGroupData).toBeArrayOfSize(serverEntity.specimenGroupData.length);
      expect(this.annotationTypeData).toBeArrayOfSize(serverEntity.annotationTypeData.length);

      expect(this.specimenGroupData).toBeContainAll(serverEntity.specimenGroupData);
      expect(this.annotationTypeData).toBeContainAll(serverEntity.annotationTypeData);
    };

    ProcessingType.compareToServerEntity = function (serverEntity) {
      ConcurrencySafeEntity.prototype.compareToServerEntity.call(this, serverEntity);
      validateAttrs(this, serverEntity, 'name', 'enabled');
      validateOptional(this, serverEntity, 'description');
    };

    SpecimenLinkType.compareToServerEntity = function (serverEntity) {
      ConcurrencySafeEntity.prototype.compareToServerEntity.call(this, serverEntity);
      validateAttrs(this,
                    serverEntity,
                    'processingTypeId',
                    'expectedInputChange',
                    'expectedOutputChange',
                    'inputCount',
                    'outputCount',
                    'inputGroupId',
                    'outputGroupId');
      expect(this.annotationTypeData).toBeArrayOfSize(serverEntity.annotationTypeData.length);
      expect(this.annotationTypeData).toBeContainAll(serverEntity.annotationTypeData);
    };

    SpecimenGroup.prototype.compareToServerEntity = function (serverEntity) {
      ConcurrencySafeEntity.prototype.compareToServerEntity.call(this, serverEntity);
      validateAttrs(this,
                    serverEntity,
                    'name',
                    'units',
                    'anatomicalSourceType',
                    'preservationType',
                    'preservationTemperatureType',
                    'specimenType');
      validateOptional(this, serverEntity, 'description');
    };

    Study.prototype.compareToServerEntity = function (serverEntity) {
      ConcurrencySafeEntity.prototype.compareToServerEntity.call(this, serverEntity);
      validateAttrs(this, serverEntity, 'name', 'Status');
      validateOptional(this, serverEntity, 'description');
    };

    Participant.prototype.compareToServerEntity = function (serverEntity) {
      ConcurrencySafeEntity.prototype.compareToServerEntity.call(this, serverEntity);
      validateAttrs(this, serverEntity, 'studyId', 'uniqueId');

      _.each(this.annotations, function (annotation) {
        var serverAnnotation = _.findWhere(serverEntity.annotations,
                                           { annotationTypeId: annotation.getAnnotationTypeId() });
        Annotation.prototype.compareToServerEntity.call(annotation, serverEntity);
      });
    };

    Annotation.prototype.compareToServerEntity = function (serverEntity) {
      switch (this.getValueType) {

      case AnnotationValueType.DATE_TIME():
      case AnnotationValueType.TEXT():
        expect(this.stringValue).toEqual(serverEntity.stringValue);
        expect(this.numberValue).toBeNull();
        expect(this.selectedValues).toBeEmptyArray();
        break;

      case AnnotationValueType.NUMBER():
        expect(this.numberValue).toEqual(serverEntity.numberValue);
        expect(this.stringValue).toBeNull();
        expect(this.selectedValues).toBeEmptyArray();
        break;

      case AnnotationValueType.SELECT():
        expect(this.stringValue).toBeNull();
        expect(this.numberValue).toBeNull();
        expect(this.selectedValues).toBeArrayOfSize(serverEntity.selectedValues.length);
        expect(this.selectedValues).toContainAll(serverEntity.selectedValues);
        break;
      }
    };

    Centre.prototype.compareToServerEntity = function (serverEntity) {
      ConcurrencySafeEntity.prototype.compareToServerEntity.call(this, serverEntity);
      validateAttrs(this, serverEntity, 'name', 'Status');
      validateOptional(this, serverEntity, 'description');
    };

    Location.prototype.compareToServerEntity = function (serverEntity) {
      validateAttrs(this,
                    serverEntity,
                    'name',
                    'street',
                    'city',
                    'province',
                    'postalCode',
                    'countryIsoCode');
      validateOptional(this, serverEntity, 'id', 'poBoxNumber');
    };
  }

  return extendedDomainEntities;
});
