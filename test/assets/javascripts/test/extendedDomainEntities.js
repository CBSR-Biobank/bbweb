/**
 * This service extends the domain classes to add extra functions that can be run in the test environment.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['angular', 'underscore', 'moment'], function(angular, _, moment) {
  'use strict';

  extendedDomainEntities.$inject = [
    'bbwebConfig',
    'ConcurrencySafeEntity',
    'AnnotationType',
    'Annotation',
    'Study',
    'SpecimenGroup',
    'CollectionEventType',
    'ProcessingType',
    'SpecimenLinkType',
    'Participant',
    'CollectionEvent',
    'DateTimeAnnotation',
    'MultipleSelectAnnotation',
    'NumberAnnotation',
    'SingleSelectAnnotation',
    'TextAnnotation',
    'AnnotationValueType',
    'Centre',
    'Location'
  ];

  /**
   * Extends domain entities by adding a 'compareToServerEntity' that can be called from test code to compare
   * a javascript domain object and a response from the server (for the same domain entity).
   */
  function extendedDomainEntities(bbwebConfig,
                                  ConcurrencySafeEntity,
                                  AnnotationType,
                                  Annotation,
                                  Study,
                                  SpecimenGroup,
                                  CollectionEventType,
                                  ProcessingType,
                                  SpecimenLinkType,
                                  Participant,
                                  CollectionEvent,
                                  DateTimeAnnotation,
                                  MultipleSelectAnnotation,
                                  NumberAnnotation,
                                  SingleSelectAnnotation,
                                  TextAnnotation,
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
        //console.log('validateAttrs', arg, obj[arg], entity[arg]);
        expect(obj[arg]).toEqual(entity[arg]);
      });
    }

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
    }

    ConcurrencySafeEntity.prototype.compareToServerEntity = function (serverEntity) {
      validateAttrs(this, serverEntity, 'id', 'version');
    };

    AnnotationType.prototype.compareToServerEntity = function (serverEntity) {
      validateAttrs(this, serverEntity, 'name', 'valueType', 'options', 'required');
      validateOptional(this, serverEntity, 'description', 'maxValueCount');
    };

    CollectionEventType.prototype.compareToServerEntity = function (serverEntity) {
      ConcurrencySafeEntity.prototype.compareToServerEntity.call(this, serverEntity);
      validateAttrs(this, serverEntity, 'name', 'recurring');
      validateOptional(this, serverEntity, 'description');

      expect(this.specimenGroupData).toBeArrayOfSize(serverEntity.specimenGroupData.length);
      expect(this.annotationTypeData).toBeArrayOfSize(serverEntity.annotationTypeData.length);

      expect(this.specimenGroupData).toBeContainAll(serverEntity.specimenGroupData);
      expect(this.annotationTypeData).toBeContainAll(serverEntity.annotationTypeData);
    };

    ProcessingType.prototype.compareToServerEntity = function (serverEntity) {
      ConcurrencySafeEntity.prototype.compareToServerEntity.call(this, serverEntity);
      validateAttrs(this, serverEntity, 'name', 'enabled');
      validateOptional(this, serverEntity, 'description');
    };

    SpecimenLinkType.prototype.compareToServerEntity = function (serverEntity) {
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
        Annotation.prototype.compareToServerEntity.call(annotation, serverAnnotation);
      });
    };

    CollectionEvent.prototype.compareToServerEntity = function (serverEntity) {
      ConcurrencySafeEntity.prototype.compareToServerEntity.call(this, serverEntity);
      validateAttrs(this,
                    serverEntity,
                    'participantId',
                    'collectionEventTypeId',
                    'timeCompleted',
                    'visitNumber');

      expect(this.annotations).toBeArrayOfSize(serverEntity.annotations.length);
      _.each(this.annotations, function (annotation) {

        // only compare annotations if annotation is of type Annotation
        if (annotation.getAnnotationTypeId) {
          var serverAnnotation = _.findWhere(serverEntity.annotations,
                                             { annotationTypeId: annotation.getAnnotationTypeId() });
          expect(serverAnnotation).toBeDefined();
          annotation.compareToServerEntity.call(annotation, serverAnnotation);
        }
      });
    };

    DateTimeAnnotation.prototype.compareToServerEntity = function (serverEntity) {
      // has to be comparetd to UTC time
      expect(moment(this.date).local().format()).toEqual(serverEntity.stringValue);
      expect(moment(this.time).local().format()).toEqual(serverEntity.stringValue);
    };

    MultipleSelectAnnotation.prototype.compareToServerEntity = function (serverEntity) {
      expect(this.values).toBeArrayOfSize(serverEntity.selectedValues.length);
      expect(this.values).toContainAll(_.pluck(serverEntity.selectedValues, 'value'));
    };

    NumberAnnotation.prototype.compareToServerEntity = function (serverEntity) {
      expect(this.value.toString()).toEqual(serverEntity.numberValue.toString());
    };

    SingleSelectAnnotation.prototype.compareToServerEntity = function (serverEntity) {
      expect(serverEntity.selectedValues).toBeArrayOfSize(1);
      expect(this.value).toBe(_.pluck(serverEntity.selectedValues, 'value')[0]);
    };

    TextAnnotation.prototype.compareToServerEntity = function (serverEntity) {
      expect(this.value).toEqual(serverEntity.stringValue);
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
