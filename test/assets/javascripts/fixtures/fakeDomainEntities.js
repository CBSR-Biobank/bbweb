/**
 * Generates fake domain entities as returned by the server.
 *
 * This has to be an AngularJS service so that it's dependencies from the real application
 * can be injected (i.e. AnnotationValueType).
 */
define('biobank.fakeDomainEntities', [
  'angular',
  'underscore',
  'faker',
  'moment',
  'biobank.testUtils'
], function(angular,
            _,
            faker,
            moment,
            utils) {
  'use strict';

  var module = angular.module('biobank.fakeDomainEntities', []);

  module.service('fakeDomainEntities', fakeDomainEntities);

  fakeDomainEntities.$inject = [
    'AnnotationValueType',
    'AnatomicalSourceType',
    'PreservationTemperatureType',
    'PreservationType',
    'SpecimenType'
  ];

  function fakeDomainEntities(AnnotationValueType,
                              AnatomicalSourceType,
                              PreservationTemperatureType,
                              PreservationType,
                              SpecimenType) {
    var entityNames = [];

    var service = {
      domainEntityNameNext:              domainEntityNameNext,
      specimenGroupData:                 specimenGroupData,
      annotationTypeData:                annotationTypeData,
      studyAnnotationType:               studyAnnotationType,
      specimenLinkType:                  specimenLinkType,
      processingType:                    processingType,
      collectionEventType:               collectionEventType,
      specimenGroup:                     specimenGroup,
      annotationType:                    annotationType,
      study:                             study,

      ENTITY_NAME_PROCESSING_TYPE:       ENTITY_NAME_PROCESSING_TYPE,
      ENTITY_NAME_SPECIMEN_LINK_TYPE:    ENTITY_NAME_SPECIMEN_LINK_TYPE,
      ENTITY_NAME_COLLECTION_EVENT_TYPE: ENTITY_NAME_COLLECTION_EVENT_TYPE,
      ENTITY_NAME_SPECIMEN_GROUP:        ENTITY_NAME_SPECIMEN_GROUP,
      ENTITY_NAME_ANNOTATION_TYPE:       ENTITY_NAME_ANNOTATION_TYPE,
      ENTITY_NAME_STUDY:                 ENTITY_NAME_STUDY
    };
    return service;

    function ENTITY_NAME_PROCESSING_TYPE()       { return 'processingType'; }
    function ENTITY_NAME_SPECIMEN_LINK_TYPE()    { return 'specimenLinkType'; }
    function ENTITY_NAME_COLLECTION_EVENT_TYPE() { return 'collectionEventType'; }
    function ENTITY_NAME_SPECIMEN_GROUP()        { return 'specimenGroup'; }
    function ENTITY_NAME_ANNOTATION_TYPE()       { return 'annotationType'; }
    function ENTITY_NAME_STUDY()                 { return 'study'; }

    function entityCommonFields() {
      return {
        version:      0,
        timeAdded:    moment(faker.date.recent(10)).format(),
        timeModified: moment(faker.date.recent(5)).format()
      };
    }

    /**
     * Generates a unique name for a domain entity type. If domain entity type is undefined, then a unique
     * string is generated.
     *
     * @param domainEntityType the name of the domain entity type. Eg: 'study', 'centre', 'user', etc.
     */
    function domainEntityNameNext(domainEntityType) {
      domainEntityType = domainEntityType || 'string';

      if (!entityNames[domainEntityType]) {
        entityNames[domainEntityType] = [];
      }

      var newName = domainEntityType + '_' + entityNames[domainEntityType].length;
      entityNames[domainEntityType].push(newName);
      return newName;
    }

    function specimenGroupData(specimenGroup) {
      return {
        specimenGroupId: specimenGroup.id,
        maxCount: faker.random.number() + 1,
        amount: faker.random.number({precision: 0.5}) + 1
      };
    }

    function annotationTypeData(annotationType) {
      return {
        annotationTypeId: annotationType.id,
        required: utils.randomBoolean()
      };
    }

    function specimenLinkType(processingType, options) {
      var slt = {
        id: utils.uuid(),
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
      if (options.annotationTypes) {
        slt.annotationTypeData = _.map(options.annotationTypes, function(at) {
          return annotationTypeData(at);
        });
      }

      return _.extend(slt, entityCommonFields());
    }

    function processingType(study) {
      var pt =  {
        id:          utils.uuid(),
        studyId:     study.id,
        name:        domainEntityNameNext(ENTITY_NAME_PROCESSING_TYPE()),
        description: faker.lorem.words(1),
        enabled:     false
      };
      return _.extend(pt, entityCommonFields());

    }

    /**
     * Returns a collection event type as returned by the server.
     */
    function collectionEventType(study, options) {
      var cet = {
        id:                 utils.uuid(),
        studyId:            study.id,
        name:               domainEntityNameNext(ENTITY_NAME_COLLECTION_EVENT_TYPE()),
        description:        faker.lorem.words(1)
      };

      options = options || {};

      if (options.specimenGroups) {
        cet.specimenGroupData = _.map(options.specimenGroups, function(sg) {
          return specimenGroupData(sg);
        });
      }
      if (options.annotationTypes) {
        cet.annotationTypeData = _.map(options.annotationTypes, function(at) {
          return annotationTypeData(at);
        });
      }

      cet.recurring = _.isUndefined(options.recurring) ? false : options.recurring;

      return _.extend(cet, entityCommonFields());
    }

    function randomAnatomicalSourceType() {
      faker.random.array_element(AnatomicalSourceType.values());
    }

    function randomPreservationType() {
      faker.random.array_element(PreservationType.values());
    }

    function randomPreservationTemperatureTypeType() {
      faker.random.array_element(PreservationTemperatureType.values());
    }

    function randomSpecimenType() {
      faker.random.array_element(SpecimenType.values());
    }

    function specimenGroup(study) {
      var sg = {
        id:                          utils.uuid(),
        studyId:                     study.id,
        name:                        domainEntityNameNext(ENTITY_NAME_SPECIMEN_GROUP()),
        description:                 faker.lorem.words(1),
        units:                       'mL',
        anatomicalSourceType:        randomAnatomicalSourceType(),
        preservationType:            randomPreservationType(),
        preservationTemperatureType: randomPreservationTemperatureTypeType(),
        specimenType:                randomSpecimenType()
      };
      return _.extend(sg, entityCommonFields());
    }

    /**
     * If you need a study annotatoin type then use function 'studyAnnotationType'.
     *
     * @param {Study} study the study this annotation type belongs to.
     *
     * @param {Boolean} option.required use only when creating an Participant Annotation Type.
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
        id:        utils.uuid(),
        valueType: options.valueType,
        name:      domainEntityNameNext(ENTITY_NAME_ANNOTATION_TYPE()),
        options:   []
      };

      if (options.valueType === AnnotationValueType.SELECT()) {
        if (_.isUndefined(options.maxValueCount)) {
          options.maxValueCount = 1;
        }

        at.maxValueCount = options.maxValueCount;

        if (_.isUndefined(options.options)) {
          at.options = _.map(_.range(2), function() {
            return domainEntityNameNext(ENTITY_NAME_ANNOTATION_TYPE());
          });
        } else {
          at.options = options.options;
        }
      }

      if (!_.isUndefined(options.required)) {
        at.required = options.required;
      }

      return _.extend(at, entityCommonFields());
    }

    function studyAnnotationType(study, options) {
      return _.extend(annotationType(options), { studyId: study.id });
    }

    function study() {
      var study =  {
        id:          utils.uuid(),
        name:        domainEntityNameNext(ENTITY_NAME_STUDY()),
        description: faker.lorem.words(1),
        status:      'Disabled'
      };
      return _.extend(study, entityCommonFields());
    }

  }

});

