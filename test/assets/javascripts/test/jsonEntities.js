/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'underscore',
  'faker',
  'moment',
], function(angular, _, faker, moment) {
  'use strict';

  /*jshint camelcase: false */

  jsonEntities.$inject = [
    'AnnotationValueType',
    'AnatomicalSourceType',
    'PreservationTemperatureType',
    'PreservationType',
    'SpecimenType',
    'StudyStatus',
    'CentreStatus',
    'UserStatus',
    'bbwebConfig',
    'testUtils'
  ];

  /**
   * Generates fake domain entities as if returned by the server.
   *
   * This has to be an AngularJS service so that it's dependencies from the real application
   * can be injected (i.e. AnnotationValueType).
   */
  function jsonEntities(AnnotationValueType,
                        AnatomicalSourceType,
                        PreservationTemperatureType,
                        PreservationType,
                        SpecimenType,
                        StudyStatus,
                        CentreStatus,
                        UserStatus,
                        bbwebConfig,
                        testUtils) {
    var nameCountByEntity = {};

    var service = {
      domainEntityNameNext:              domainEntityNameNext,
      stringNext:                        stringNext,
      specimenLinkType:                  specimenLinkType,
      processingType:                    processingType,
      collectionEventType:               collectionEventType,
      collectionSpecimenSpec:            collectionSpecimenSpec,
      specimenGroup:                     specimenGroup,
      annotationType:                    annotationType,
      allAnnotationTypes:                allAnnotationTypes,
      study:                             study,
      annotation:                        annotation,
      valueForAnnotation:                valueForAnnotation,

      participant:                       participant,
      collectionEvent:                   collectionEvent,

      centre:                            centre,
      location:                          location,

      user:                              user,

      pagedResult:                       pagedResult,

      ENTITY_NAME_PROCESSING_TYPE:       ENTITY_NAME_PROCESSING_TYPE,
      ENTITY_NAME_SPECIMEN_LINK_TYPE:    ENTITY_NAME_SPECIMEN_LINK_TYPE,
      ENTITY_NAME_COLLECTION_EVENT_TYPE: ENTITY_NAME_COLLECTION_EVENT_TYPE,
      ENTITY_NAME_SPECIMEN_GROUP:        ENTITY_NAME_SPECIMEN_GROUP,
      ENTITY_NAME_ANNOTATION_TYPE:       ENTITY_NAME_ANNOTATION_TYPE,
      ENTITY_NAME_STUDY:                 ENTITY_NAME_STUDY,
      ENTITY_NAME_ANNOTATION:            ENTITY_NAME_ANNOTATION,

      ENTITY_NAME_PARTICIPANT:           ENTITY_NAME_PARTICIPANT,
      ENTITY_NAME_COLLECTION_EVENT:      ENTITY_NAME_COLLECTION_EVENT,

      ENTITY_NAME_CENTRE:                ENTITY_NAME_CENTRE,
      ENTITY_NAME_LOCATION:              ENTITY_NAME_LOCATION
    };
    return service;

    function ENTITY_NAME_PROCESSING_TYPE()       { return 'processingType'; }
    function ENTITY_NAME_SPECIMEN_LINK_TYPE()    { return 'specimenLinkType'; }
    function ENTITY_NAME_COLLECTION_EVENT_TYPE() { return 'collectionEventType'; }
    function ENTITY_NAME_SPECIMEN_GROUP()        { return 'specimenGroup'; }
    function ENTITY_NAME_ANNOTATION_TYPE()       { return 'annotationType'; }
    function ENTITY_NAME_STUDY()                 { return 'study'; }
    function ENTITY_NAME_ANNOTATION()            { return 'annotation'; }

    function ENTITY_NAME_PARTICIPANT()           { return 'participant'; }
    function ENTITY_NAME_COLLECTION_EVENT()      { return 'collectionEvent'; }

    function ENTITY_NAME_CENTRE()                { return 'centre'; }
    function ENTITY_NAME_LOCATION()              { return 'location'; }

    function ENTITY_NAME_USER()                  { return 'user'; }

    function commonFields(obj) {
      return {
        version:      0,
        timeAdded:    moment(faker.date.recent(10)).format(),
        timeModified: moment(faker.date.recent(5)).format()
      };
    }

    /**
     * Alternate way to generate a random word.
     *
     * Due to a bug in faker's Helpers.shuffle, faker.lorem.words() sometimes returns undefined.
     */
    function randomFakerLoremWord() {
      return faker.address.streetAddress();
    }

    /**
     * Generates a unique name for a domain entity type. If domain entity type is undefined, then a unique
     * string is generated.
     *
     * @param domainEntityType the name of the domain entity type. Eg: 'study', 'centre', 'user', etc.
     */
    function domainEntityNameNext(domainEntityType) {
      domainEntityType = domainEntityType || 'string';

      if (_.isUndefined(nameCountByEntity[domainEntityType])) {
        nameCountByEntity[domainEntityType] = 0;
      } else {
        nameCountByEntity[domainEntityType]++;
      }

      return domainEntityType + '_' + nameCountByEntity[domainEntityType];
    }

    function specimenLinkType(processingType, options) {
      var slt = {
        id: testUtils.uuid(),
        processingTypeId: processingType.id,

        expectedInputChange:   faker.random.number({precision: 0.5}),
        expectedOutputChange:  faker.random.number({precision: 0.5}),
        inputCount:            faker.random.number(5) + 1,
        outputCount:           faker.random.number(5) + 1
      };

      options = options || {};

      if (options.inputGroup) {
        slt.inputGroupId = options.inputGroup.id;
      }
      if (options.outputGroup) {
        slt.outputGroupId = options.outputGroup.id;
      }
      if (options.inputContainerType) {
        slt.inputContainerTypeId = options.inputContainerType.id;
      }
      if (options.outputContainerType) {
        slt.outputContainerTypeId = options.outputContainerType.id;
      }

      slt.annotationTypeData = [];

      return _.extend(slt, commonFields());
    }

    function stringNext() {
      return domainEntityNameNext();
    }

    function processingType(study) {
      var pt =  {
        id:          testUtils.uuid(),
        studyId:     study.id,
        name:        domainEntityNameNext(ENTITY_NAME_PROCESSING_TYPE()),
        description: randomFakerLoremWord(),
        enabled:     false
      };
      return _.extend(pt, commonFields());
    }

    /**
     * Returns a collection event type as returned by the server.
     */
    function collectionEventType(study, options) {
      var cet = {
        id:              testUtils.uuid(),
        studyId:         study.id,
        name:            domainEntityNameNext(ENTITY_NAME_COLLECTION_EVENT_TYPE()),
        description:     randomFakerLoremWord(),
        specimenSpecs:   [],
        annotationTypes: []
      };

      options = options || {};
      cet.recurring = _.isUndefined(options.recurring) ? false : options.recurring;
      return _.extend(cet,
                      commonFields(),
                      _.pick(options, 'specimenSpecs', 'annotationTypes'));
    }

    function randomAnatomicalSourceType() {
      return faker.random.array_element(AnatomicalSourceType.values());
    }

    function randomPreservationType() {
      return faker.random.array_element(PreservationType.values());
    }

    function randomPreservationTemperatureTypeType() {
      return faker.random.array_element(PreservationTemperatureType.values());
    }

    function randomSpecimenType() {
      return faker.random.array_element(SpecimenType.values());
    }

    function collectionSpecimenSpec(options) {
      var spec = {
        uniqueId:                    testUtils.uuid(),
        name:                        domainEntityNameNext(ENTITY_NAME_SPECIMEN_GROUP()),
        description:                 randomFakerLoremWord(),
        units:                       'mL',
        anatomicalSourceType:        randomAnatomicalSourceType(),
        preservationType:            randomPreservationType(),
        preservationTemperatureType: randomPreservationTemperatureTypeType(),
        specimenType:                randomSpecimenType(),
        maxCount:                    1,
        amount:                      0.5
      };
      options = options || {};
      return _.extend(spec, options);
    }

    function specimenGroup(study) {
      var sg = {
        id:                          testUtils.uuid(),
        studyId:                     study.id,
        name:                        domainEntityNameNext(ENTITY_NAME_SPECIMEN_GROUP()),
        description:                 randomFakerLoremWord(),
        units:                       'mL',
        anatomicalSourceType:        randomAnatomicalSourceType(),
        preservationType:            randomPreservationType(),
        preservationTemperatureType: randomPreservationTemperatureTypeType(),
        specimenType:                randomSpecimenType()
      };
      return _.extend(sg, commonFields());
    }

    /**
     * If you need a study annotatoin type then use function 'studyAnnotationType'.
     *
     * @param {ValueType} option.valueType the type of annotation Type to create. Valid types are: Text,
     * Number, DateTime and Select.
     *
     * @param {Int} option.maxValueCount when valueType is 'Select', use 1 for single selection or '2' for
     * multiple selection.
     */
    function annotationType(options) {
      options = options || {};

      if (!options.valueType) {
        options.valueType = AnnotationValueType.TEXT();
      }

      var at = {
        uniqueId:  testUtils.uuid(),
        valueType: options.valueType,
        name:      domainEntityNameNext(ENTITY_NAME_ANNOTATION_TYPE()),
        options:   [],
        required:   false
      };

      if (options.valueType === AnnotationValueType.SELECT()) {
        if (_.isUndefined(options.maxValueCount)) {
          options.maxValueCount = 1;
        }

        if (_.isUndefined(options.options)) {
          at.options = _.map(_.range(2), function() {
            return domainEntityNameNext(ENTITY_NAME_ANNOTATION_TYPE());
          });
        } else {
          at.options = options.options;
        }
      }

      if (!_.isUndefined(options.maxValueCount)) {
        at.maxValueCount = options.maxValueCount;
      }

      return at;
    }

    function allAnnotationTypes() {
      var annotationTypes = _.map(AnnotationValueType.values(), function (valueType) {
        return annotationType({ valueType: valueType });
      });
      annotationTypes.push(annotationType({
        valueType:     AnnotationValueType.SELECT(),
        maxValueCount: 2,
        options:       [ 'opt1', 'opt2', 'opt3' ]
      }));
      return annotationTypes;
    }

    function study(options) {
      var s =  {
        id:              testUtils.uuid(),
        name:            domainEntityNameNext(ENTITY_NAME_STUDY()),
        description:     randomFakerLoremWord(),
        annotationTypes: [],
        status:          StudyStatus.DISABLED()
      };
      options = options || {};
      return _.extend(s, commonFields(), options);
    }

    function annotation(value, annotationType) {
      var annot = {
        annotationTypeId: annotationType.id,
        selectedValues:   []
      };

      switch (annotationType.valueType) {

      case AnnotationValueType.TEXT():
      case AnnotationValueType.DATE_TIME():
        annot.stringValue = value;
        break;

      case AnnotationValueType.NUMBER():
        annot.numberValue = value;
        break;

      case AnnotationValueType.SELECT():
        if (value) {
          if (annotationType.maxValueCount === 1) {
            annot.selectedValues =  [{ annotationTypeId: annotationType.id, value: value }];
          } else if (annotationType.maxValueCount > 1) {
            annot.selectedValues =_.map(value, function (v) {
              return { annotationTypeId: annotationType.id, value: v };
            });
          } else {
            throw new Error('invalid max value count for annotation: ' + annotationType.maxValueCount);
          }
        }
        break;

      default:
        throw new Error('invalid value type: ' + annotationType.valueType);
      }

      return annot;
    }

    function valueForAnnotation(annotationType) {
      switch (annotationType.valueType) {

      case AnnotationValueType.TEXT():
        return stringNext();

      case AnnotationValueType.NUMBER():
        return faker.random.number({precision: 0.05}).toString();

      case AnnotationValueType.DATE_TIME():
        // has to be in UTC format, with no seconds or milliseconds
        return moment(faker.date.past(1))
          .set({
            'millisecond': 0,
            'second':      0
          })
          .local()
          .format();

      case AnnotationValueType.SELECT():
        if (annotationType.maxValueCount === 1) {
          return annotationType.options[0];
        } else if (annotationType.maxValueCount === 2) {
          return annotationType.options;
        } else {
          throw new Error('invalid max value count: ' + annotationType.maxValueCount);
        }
      }

      throw new Error('invalid value type: ' + annotationType.valueType);
    }

    function participant(options) {
      options = options || {};

      var p =  {
        id:          testUtils.uuid(),
        studyId:     options.studyId || null,
        uniqueId:    domainEntityNameNext(ENTITY_NAME_PARTICIPANT())
      };

      options.annotationTypes = options.annotationTypes || {};
      p.annotations = _.map(options.annotationTypes, function (annotationType) {
        return annotation(valueForAnnotation(annotationType), annotationType);
      });

      return _.extend(p, commonFields());
    }

    function collectionEvent(options) {
      options = options || {};

      var ce =  {
        id:                    testUtils.uuid(),
        participantId:         options.participantId || null,
        collectionEventTypeId: options.collectionEventTypeId || null,
        timeCompleted:         moment(faker.date.recent(10)).format(),
        visitNumber:           options.visitNumber || 1
      };

      options.annotationTypes = options.annotationTypes || {};
      ce.annotations = _.map(options.annotationTypes, function (annotationType) {
        return annotation(valueForAnnotation(annotationType), annotationType);
      });

      return _.extend(ce, commonFields());
    }

    function centre(options) {
      var c =  {
        id:          testUtils.uuid(),
        name:        domainEntityNameNext(ENTITY_NAME_CENTRE()),
        description: randomFakerLoremWord(),
        status:      CentreStatus.DISABLED()
      };
      options = options || {};
      return _.extend(c, commonFields(), options);
    }

    /**
     * This is a value object, so it does not have the common fields.
     */
    function location(options) {
      var loc = {
        uniqueId:       testUtils.uuid(),
        name:           domainEntityNameNext(ENTITY_NAME_LOCATION()),
        street:         faker.address.streetAddress(),
        city:           faker.address.city(),
        province:       faker.address.state(),
        postalCode:     faker.address.zipCode(),
        poBoxNumber:    randomFakerLoremWord(),
        countryIsoCode: randomFakerLoremWord()
      };
      options = options || {};
      _.extend(loc, options);
      return loc;
    }

    function user() {
      var u =  {
        id:          testUtils.uuid(),
        name:        domainEntityNameNext(ENTITY_NAME_USER()),
        email:       faker.internet.email(),
        avatarUrl:   faker.internet.avatar(),
        status:      UserStatus.REGISTERED()
      };
      return _.extend(u, commonFields());
    }

    function pagedResult(entities) {
      return {
        items:    entities,
        page:     1,
        offset:   0,
        total:    20,
        pageSize: 5,
        next:     2,
        maxPages: 4
      };
    }
  }

  /*jshint camelcase: true */

  return jsonEntities;
});
