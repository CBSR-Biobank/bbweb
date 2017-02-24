/**
 * This service extends the domain classes to add extra functions that can be run in the test environment.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['angular', 'lodash', 'moment'], function(angular, _, moment) {
  'use strict';

  testDomainEntities.$inject = [
    'AppConfig',
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

  /*
   * Extends domain entities by adding a 'compareToJsonEntity' function that can be called from test code to
   * compare a javascript domain object and a response from the server (for the same domain entity).
   */
  function testDomainEntities(AppConfig,
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

    var service = {
      extend: extend
    };

    return service;

    //--

    /**
     * Validates that an object and a server side entity have the same values for the given attributes.
     *
     * Meant to be called from a Jasmine test suite.
     *
     * @param {object} obj the JS domain entity.
     *
     * @param {object} entity the server side entity to compare against.
     *
     * @param {...string} attrs the attributes to compare. May be more than one.
     *
     * @return {undefined}
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
     * Validates that an object and a server side entity have the same values for the given optional
     * attributes.
     *
     * Meant to be called from a Jasmine test suite.
     *
     * @param {object} obj the JS domain entity.
     *
     * @param {object} entity the server side entity to compare against.
     *
     * @param {...string} attrs the attributes to compare. May be more than one.
     *
     * @return {undefined}
     */
    function validateOptional(/* obj, entity, attrs */) {
      var args = _.toArray(arguments);
      var obj = args.shift();
      var entity = args.shift();

      _.each(args, function(attr) {
        if (obj[attr] !== null) {
          expect(obj[attr]).toEqual(entity[attr]);
        } else {
          expect(entity[attr]).toBeFalsy();
        }
      });
    }

    function extend() {
      ConcurrencySafeEntity.prototype.compareToJsonEntity = function (serverEntity) {
        validateAttrs(this, serverEntity, 'id', 'version');
      };

      AnnotationType.prototype.compareToJsonEntity = function (serverEntity) {
        validateAttrs(this, serverEntity, 'name', 'valueType', 'options', 'required');
        validateOptional(this, serverEntity, 'description', 'maxValueCount');
      };

      Study.prototype.compareToJsonEntity = function (serverEntity) {
        ConcurrencySafeEntity.prototype.compareToJsonEntity.call(this, serverEntity);
        validateAttrs(this, serverEntity, 'name', 'Status');
        validateOptional(this, serverEntity, 'description');

        expect(this.annotationTypes).toBeArrayOfSize(serverEntity.annotationTypes.length);
        expect(this.annotationTypes).toContainAll(serverEntity.annotationTypes);
      };

      CollectionEventType.prototype.compareToJsonEntity = function (serverEntity) {
        ConcurrencySafeEntity.prototype.compareToJsonEntity.call(this, serverEntity);
        validateAttrs(this, serverEntity, 'name', 'recurring');
        validateOptional(this, serverEntity, 'description');

        if (this.specimenSpecs && serverEntity.specimenSpecs) {
          expect(this.specimenSpecs).toBeArrayOfSize(serverEntity.specimenSpecs.length);
          expect(this.specimenSpecs).toContainAll(serverEntity.specimenSpecs);
        } else {
          fail('specimen specs mismatch');
        }

        if (this.annotationTypes && serverEntity.annotationTypes) {
          expect(this.annotationTypes).toBeArrayOfSize(serverEntity.annotationTypes.length);
          expect(this.annotationTypes).toContainAll(serverEntity.annotationTypes);
        } else {
          fail('annotation types mismatch');
        }
      };

      ProcessingType.prototype.compareToJsonEntity = function (serverEntity) {
        ConcurrencySafeEntity.prototype.compareToJsonEntity.call(this, serverEntity);
        validateAttrs(this, serverEntity, 'name', 'enabled');
        validateOptional(this, serverEntity, 'description');
      };

      SpecimenLinkType.prototype.compareToJsonEntity = function (serverEntity) {
        ConcurrencySafeEntity.prototype.compareToJsonEntity.call(this, serverEntity);
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

      SpecimenGroup.prototype.compareToJsonEntity = function (serverEntity) {
        ConcurrencySafeEntity.prototype.compareToJsonEntity.call(this, serverEntity);
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

      Participant.prototype.compareToJsonEntity = function (serverEntity) {
        ConcurrencySafeEntity.prototype.compareToJsonEntity.call(this, serverEntity);
        validateAttrs(this, serverEntity, 'studyId', 'uniqueId');

        _.each(this.annotations, function (annotation) {
          var serverAnnotation = _.find(serverEntity.annotations,
                                        { annotationTypeId: annotation.getAnnotationTypeId() });
          Annotation.prototype.compareToJsonEntity.call(annotation, serverAnnotation);
        });
      };

      CollectionEvent.prototype.compareToJsonEntity = function (serverEntity) {
        ConcurrencySafeEntity.prototype.compareToJsonEntity.call(this, serverEntity);
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
            var serverAnnotation = _.find(serverEntity.annotations,
                                          { annotationTypeId: annotation.getAnnotationTypeId() });
            expect(serverAnnotation).toBeDefined();
            annotation.compareToJsonEntity.call(annotation, serverAnnotation);
          }
        });
      };

      DateTimeAnnotation.prototype.compareToJsonEntity = function (serverEntity) {
        if (_.isNull(serverEntity.stringValue)) {
          expect(this.value).toBeNull();
        } else {
          // has to be comparetd to UTC time
          expect(moment(this.value).utc().format()).toEqual(serverEntity.stringValue);
        }
      };

      MultipleSelectAnnotation.prototype.compareToJsonEntity = function (serverEntity) {
        if (serverEntity.selectedValues.length > 0) {
          expect(this.values).toBeArrayOfSize(serverEntity.selectedValues.length);
          expect(this.values).toContainAll(_.map(serverEntity.selectedValues, 'value'));
        }
      };

      NumberAnnotation.prototype.compareToJsonEntity = function (serverEntity) {
        if (serverEntity.numberValue) {
          expect(this.value.toString()).toEqual(serverEntity.numberValue.toString());
        }
      };

      SingleSelectAnnotation.prototype.compareToJsonEntity = function (serverEntity) {
        if (serverEntity.selectedValues.length === 0) {
          expect(this.value).toBeNull();
          expect(this.selectedValues).toBeEmptyArray();
        } else {
          expect(serverEntity.selectedValues).toBeArrayOfSize(1);
          expect(this.value).toBe(serverEntity.selectedValues[0]);
        }
      };

      TextAnnotation.prototype.compareToJsonEntity = function (serverEntity) {
        if (_.isNull(serverEntity.stringValue)) {
          expect(this.value).toBeUndefined();
        } else {
          expect(this.value).toEqual(serverEntity.stringValue);
        }
      };

      Centre.prototype.compareToJsonEntity = function (serverEntity) {
        ConcurrencySafeEntity.prototype.compareToJsonEntity.call(this, serverEntity);
        validateAttrs(this, serverEntity, 'name', 'Status');
        validateOptional(this, serverEntity, 'description');
      };

      Location.prototype.compareToJsonEntity = function (serverEntity) {
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
  }

  return testDomainEntities;
});
