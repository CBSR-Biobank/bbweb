/**
 * Generates fake domain entities as returned by the server.
 */
define(
  'biobank.fakeDomainEntities',
  [
    'underscore',
    'faker',
    'moment',
    'biobank.testUtils'
  ],
  function(_, faker, moment, utils) {

    var entityNames = [];

    var domainEntities = {
      domainEntityNameNext: domainEntityNameNext,
      specimenGroupData:    specimenGroupData,
      annotationTypeData:   annotationTypeData,
      studyAnnotationType:  studyAnnotationType,
      specimenLinkType:     specimenLinkType,
      processingType:       processingType,
      collectionEventType:  collectionEventType,
      specimenGroup:        specimenGroup,
      annotationType:       annotationType,
      study:                study
    };

    function entityCommonFields() {
      return {
        version:      0,
        timeAdded:    moment(faker.date.recent(10)).format(),
        timeModified: moment(faker.date.recent(5)).format()
      };
    }

    /**
     * Generates a unique name for a domain entity type.
     *
     * @param domainEntityType the name of the domain entity type. Eg: 'study', 'centre', 'user', etc.
     */
    function domainEntityNameNext(domainEntityType) {
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
        name:        domainEntityNameNext('study'),
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
        name:               domainEntityNameNext('collectionEventType'),
        description:        faker.lorem.words(1),
        recurring:          utils.randomBoolean()
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

      return _.extend(cet, entityCommonFields());
    }

    function specimenGroup(study) {
      var sg = {
        id:                          utils.uuid(),
        studyId:                     study.id,
        name:                        domainEntityNameNext('specimenGroup'),
        description:                 faker.lorem.words(1),
        units:                       'mL',
        anatomicalSourceType:        'Blood',
        preservationType:            'Fresh Specimen',
        preservationTemperatureType: '-80 C',
        specimenType:                'Plasma'
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
        options.valueType = 'Text';
      }

      var at = {
        id:        utils.uuid(),
        valueType: options.valueType,
        name:      domainEntityNameNext('annotationType'),
        options:   []
      };

      if (options.valueType == 'Select') {
        if (!options.maxValueCount) {
          options.maxValueCount = 1;
        }

        at.maxValueCount = options.maxValueCount;
        at.options = _.map(_.range(2), function() { return domainEntityNameNext('annotationType'); });
      }

      if (options.required) {
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
        name:        domainEntityNameNext('study'),
        description: faker.lorem.words(1),
        status:      'Disabled'
      };
      return _.extend(study, entityCommonFields());
    }

    return domainEntities;
  });
