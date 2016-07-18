/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'lodash',
  'faker',
  'moment'
], function(angular, _, faker, moment) {
  'use strict';

  /*jshint camelcase: false */

  factory.$inject = [
    'AnnotationValueType',
    'AnatomicalSourceType',
    'PreservationTemperatureType',
    'PreservationType',
    'SpecimenType',
    'StudyStatus',
    'CentreStatus',
    'UserStatus',
    'ShipmentState',
    'ShipmentItemState',
    'bbwebConfig'
  ];

  /**
   * Generates JSON domain entities as if returned by the server.
   *
   * This has to be an AngularJS service so that it's dependencies from the real application
   * can be injected (i.e. AnnotationValueType).
   */
  function factory(AnnotationValueType,
                   AnatomicalSourceType,
                   PreservationTemperatureType,
                   PreservationType,
                   SpecimenType,
                   StudyStatus,
                   CentreStatus,
                   UserStatus,
                   ShipmentState,
                   ShipmentItemState,
                   bbwebConfig) {

    var defaultEntities = {},
        entityCount = 0,
        valueTypeCount = 0,
        commonFieldNames = _.keys(commonFields),
        service;

    service = {
      // domain entities
      specimenLinkType:                  specimenLinkType,
      defaultSpecimenLinkType:           defaultSpecimenLinkType,

      processingType:                    processingType,
      defaultProcessingType:             defaultProcessingType,

      collectionEventType:               collectionEventType,
      defaultCollectionEventType:        defaultCollectionEventType,

      specimenGroup:                     specimenGroup,
      defaultSpecimenGroup:              defaultSpecimenGroup,

      study:                             study,
      defaultStudy:                      defaultStudy,

      participant:                       participant,
      defaultParticipant:                defaultParticipant,

      collectionEvent:                   collectionEvent,
      defaultCollectionEvent:            defaultCollectionEvent,

      specimen:                          specimen,
      defaultSpecimen:                   defaultSpecimen,

      centre:                            centre,
      defaultCentre:                     defaultCentre,

      shipment:                          shipment,
      defaultShipment:                   defaultShipment,

      shipmentSpecimen:                  shipmentSpecimen,
      defaultShipmentSpecimen:           defaultShipmentSpecimen,

      user:                              user,
      defaultUser:                       defaultUser,

      // value types
      annotationType:                    annotationType,
      allAnnotationTypes:                allAnnotationTypes,
      annotation:                        annotation,
      valueForAnnotation:                valueForAnnotation,
      collectionSpecimenSpec:            collectionSpecimenSpec,
      location:                          location,
      centreLocations:                   centreLocations,

      // utilities
      domainEntityNameNext:              domainEntityNameNext,
      stringNext:                        stringNext,
      emailNext:                         emailNext,
      urlNext:                           urlNext,
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
    function ENTITY_NAME_SPECIMEN()              { return 'specimen'; }

    function ENTITY_NAME_CENTRE()                { return 'centre'; }
    function ENTITY_NAME_LOCATION()              { return 'location'; }
    function ENTITY_NAME_SHIPMENT()              { return 'shipment'; }
    function ENTITY_NAME_SHIPMENT_SPECIMEN()     { return 'shipmentSpecimen'; }

    function ENTITY_NAME_USER()                  { return 'user'; }

    function commonFields() {
      return {
        version:      0,
        timeAdded:    moment(faker.date.recent(10)).format(),
        timeModified: moment(faker.date.recent(5)).format()
      };
    }

    function stringNext() {
      return domainEntityNameNext();
    }

    function emailNext() {
      return faker.internet.email();
    }

    function urlNext() {
      return faker.internet.url();
    }

    function updateDefaultEntity(entityName, entity) {
      defaultEntities[entityName] = entity;
      entityCount += 1;
    }

    function defaultEntity(entityName, createFunc) {
      if (_.isUndefined(defaultEntities[entityName])) {
        createFunc();
      }
      return defaultEntities[entityName];
    }
    /**
     * Generates a unique name for a domain entity type. If domain entity type is undefined, then a unique
     * string is generated.
     *
     * @param domainEntityType the name of the domain entity type. Eg: 'study', 'centre', 'user', etc.
     */
    function domainEntityNameNext(domainEntityType) {
      domainEntityType = domainEntityType || 'string';
      return _.uniqueId(domainEntityType + '_');
    }

    function specimenLinkType(options) {
      var processingType = defaultProcessingType(),
          defaults = {
            id:                    domainEntityNameNext(ENTITY_NAME_SPECIMEN_LINK_TYPE()),
            processingTypeId:      processingType.id,
            expectedInputChange:   faker.random.number({precision: 0.5}),
            expectedOutputChange:  faker.random.number({precision: 0.5}),
            inputGroupId:          null,
            outputGroupId:         null,
            inputCount:            faker.random.number(5) + 1,
            outputCount:           faker.random.number(5) + 1,
            inputContainerTypeId:  null,
            outputContainerTypeId: null
          },
          validKeys = commonFieldNames.concat(_.keys(defaults)),
          slt = _.extend(defaults, commonFields(), _.pick(options || {}, validKeys));

      updateDefaultEntity(ENTITY_NAME_SPECIMEN_LINK_TYPE(), slt);
      return slt;
    }

    function defaultSpecimenLinkType() {
      return defaultEntity(ENTITY_NAME_SPECIMEN_LINK_TYPE(), specimenLinkType);
    }

    function processingType(options) {
      var study = defaultStudy(),
          defaults = {
            id:          domainEntityNameNext(ENTITY_NAME_PROCESSING_TYPE()),
            studyId:     study.id,
            name:        stringNext(),
            description: faker.lorem.sentences(4),
            enabled:     false
          },
          validKeys = commonFieldNames.concat(_.keys(defaults)),
          pt = _.extend(defaults, commonFields(), _.pick(options || {}, validKeys));
      updateDefaultEntity(ENTITY_NAME_PROCESSING_TYPE(), pt);
      return pt;
    }

    function defaultProcessingType() {
      return defaultEntity(ENTITY_NAME_PROCESSING_TYPE(), processingType);
    }

    /**
     * Returns a collection event type as returned by the server.
     */
    function collectionEventType(options) {
      var study = defaultStudy(),
          defaults = {
            id:              domainEntityNameNext(ENTITY_NAME_COLLECTION_EVENT_TYPE()),
            studyId:         study.id,
            name:            stringNext(),
            description:     faker.lorem.sentences(4),
            specimenSpecs:   [],
            annotationTypes: [],
            recurring:       false
          },
          validKeys = commonFieldNames.concat(_.keys(defaults)),
          cet = _.extend(defaults, commonFields(), _.pick(options || {}, validKeys));
      updateDefaultEntity(ENTITY_NAME_COLLECTION_EVENT_TYPE(), cet);
      return cet;
    }

    function defaultCollectionEventType() {
      return defaultEntity(ENTITY_NAME_COLLECTION_EVENT_TYPE(), collectionEventType);
    }

    function randomAnatomicalSourceType() {
      return faker.random.arrayElement(_.values(AnatomicalSourceType));
    }

    function randomPreservationType() {
      return faker.random.arrayElement(_.values(PreservationType));
    }

    function randomPreservationTemperatureTypeType() {
      return faker.random.arrayElement(_.values(PreservationTemperatureType));
    }

    function randomSpecimenType() {
      return faker.random.arrayElement(_.values(SpecimenType));
    }

    function specimenGroup(options) {
      var study = defaultStudy(),
          defaults = {
            id:                          domainEntityNameNext(ENTITY_NAME_SPECIMEN_GROUP()),
            studyId:                     study.id,
            name:                        stringNext(),
            description:                 faker.lorem.sentences(4),
            units:                       'mL',
            anatomicalSourceType:        randomAnatomicalSourceType(),
            preservationType:            randomPreservationType(),
            preservationTemperatureType: randomPreservationTemperatureTypeType(),
            specimenType:                randomSpecimenType()
          },
          validKeys = commonFieldNames.concat(_.keys(defaults)),
          sg = _.extend(defaults, commonFields(), _.pick(options || {}, validKeys));
      updateDefaultEntity(ENTITY_NAME_SPECIMEN_GROUP(), sg);
      return sg;
    }

    function defaultSpecimenGroup() {
      return defaultEntity(ENTITY_NAME_SPECIMEN_GROUP(), specimenGroup);
    }

    function study(options) {
      var defaults =  { id:              domainEntityNameNext(ENTITY_NAME_STUDY()),
                        name:            stringNext(),
                        description:     faker.lorem.sentences(4),
                        annotationTypes: [],
                        status:          StudyStatus.DISABLED
                      },
          validKeys = commonFieldNames.concat(_.keys(defaults)),
          s = _.extend(defaults, commonFields(), _.pick(options || {}, validKeys));
      updateDefaultEntity(ENTITY_NAME_STUDY(), s);
      return s;
    }

    function defaultStudy() {
      return defaultEntity(ENTITY_NAME_STUDY(), study);
    }

    /**
     * If defaultStudy has annotation types, then participant will have annotations based on the study's,
     * unless options.annotationTypes is defined.
     */
    function participant(options) {
      var study = defaultStudy(),
          defaults = {
            id:          domainEntityNameNext(ENTITY_NAME_PARTICIPANT()),
            studyId:     study.id,
            uniqueId:    domainEntityNameNext(ENTITY_NAME_PARTICIPANT()),
            annotations: []
          },
          validKeys = commonFieldNames.concat(_.keys(defaults)),
          p;

      options = options || {};

      p = _.extend(defaults, commonFields(), _.pick(options, validKeys));

      if (!options.annotations) {
        // assign annotation types
        if (options.annotationTypes) {
          p.annotations = annotations(options.annotationTypes);
        } else if (study.annotationTypes) {
          p.annotations = annotations(study.annotationTypes);
        }
      }

      updateDefaultEntity(ENTITY_NAME_PARTICIPANT(), p);
      return p;
    }

    function defaultParticipant() {
      return defaultEntity(ENTITY_NAME_PARTICIPANT(), participant);
    }

    function collectionEvent(options) {
      var participant = defaultParticipant(),
          collectionEventType = defaultCollectionEventType(),
          defaults = {
            id:                    domainEntityNameNext(ENTITY_NAME_COLLECTION_EVENT()),
            participantId:         participant.id,
            collectionEventTypeId: collectionEventType.id,
            timeCompleted:         moment(faker.date.recent(10)).format(),
            visitNumber:           1,
            annotations:           []
          },
          validKeys = commonFieldNames.concat(_.keys(defaults)),
          ce;

      options = options || {};
      ce = _.extend(defaults, commonFields(), _.pick(options, validKeys));

      if (!options.annotations) {
        // assign annotation types
        if (options.annotationTypes) {
          ce.annotations = annotations(options.annotationTypes);
        } else if (collectionEventType.annotationTypes) {
          ce.annotations = annotations(collectionEventType.annotationTypes);
        }
      }

      updateDefaultEntity(ENTITY_NAME_COLLECTION_EVENT(), ce);
      return ce;
    }

    function defaultCollectionEvent() {
      return defaultEntity(ENTITY_NAME_COLLECTION_EVENT(), collectionEvent);
    }

    function specimen(options, specimenSpec) {
      var collectionEventType = defaultCollectionEventType(),
          centre = defaultCentre(),
          defaults = {
            id:                  domainEntityNameNext(ENTITY_NAME_SPECIMEN()),
            inventoryId:         domainEntityNameNext(ENTITY_NAME_SPECIMEN()),
            specimenSpecId:      null,
            originLocationId:    null,
            locationId:          null,
            timeCreated:         moment(faker.date.recent(10)).format(),
            amount:              1,
            status:              'UsableSpecimen'
          },
          validKeys = commonFieldNames.concat(_.keys(defaults)),
          spc;

      options = options || {};

      if (collectionEventType.specimenSpecs && (collectionEventType.specimenSpecs.length > 0)) {
        defaults.specimenSpecId = collectionEventType.specimenSpecs[0].uniqueId;
      }

      if (centre.locations && (centre.locations.length > 0)) {
        defaults.originLocationId = centre.locations[0].uniqueId;
        defaults.locationId = defaults.originLocationId;
      }

      spc = _.extend(defaults, commonFields(), _.pick(options, validKeys));
      updateDefaultEntity(ENTITY_NAME_SPECIMEN(), spc);
      return spc;
    }

    function defaultSpecimen() {
      return defaultEntity(ENTITY_NAME_SPECIMEN(), specimen);
    }

    function centre(options) {
      var defaults = { id:          domainEntityNameNext(ENTITY_NAME_CENTRE()),
                       name:        stringNext(),
                       description: stringNext(),
                       status:      CentreStatus.DISABLED,
                       studyIds:    [],
                       locations:   []
                     },
          validKeys = commonFieldNames.concat(_.keys(defaults)),
          c = _.extend(defaults, commonFields(), _.pick(options || {}, validKeys));
      updateDefaultEntity(ENTITY_NAME_CENTRE(), c);
      return c;
    }

    function defaultCentre() {
      return defaultEntity(ENTITY_NAME_CENTRE(), centre);
    }

    function shipment(options) {
      var defaults = { id:             domainEntityNameNext(ENTITY_NAME_SHIPMENT()),
                       state:          ShipmentState.CREATED,
                       courierName:    stringNext(),
                       trackingNumber: stringNext(),
                       fromLocationId: location().uniqueId,
                       toLocationId:   location().uniqueId
                     },
          validKeys = commonFieldNames.concat(_.keys(defaults)),
          s = _.extend(defaults, commonFields(), _.pick(options || {}, validKeys));
      updateDefaultEntity(ENTITY_NAME_SHIPMENT(), s);
      return s;
    }

    function defaultShipment() {
      return defaultEntity(ENTITY_NAME_SHIPMENT(), shipment);
    }

    function shipmentSpecimen(options) {
      var shipment = defaultShipment(),
          specimen = defaultSpecimen(),
          defaults = { id:           domainEntityNameNext(ENTITY_NAME_SHIPMENT()),
                       state:        ShipmentItemState.PRESENT,
                       shipmentId:   shipment.id,
                       specimenId:   specimen.id,
                       locationId:   location().uniqueId,
                       locationName: stringNext(),
                       timeCreated:  moment(faker.date.recent(10)).format(),
                       amount:       1,
                       units:        stringNext(),
                       status:       'UsableSpecimen'},
          validKeys = commonFieldNames.concat(_.keys(defaults)),
          ss = _.extend(defaults, commonFields(), _.pick(options || {}, validKeys));
      updateDefaultEntity(ENTITY_NAME_SHIPMENT_SPECIMEN(), ss);
      return ss;
    }

    function defaultShipmentSpecimen() {
      return defaultEntity(ENTITY_NAME_SHIPMENT_SPECIMEN(), shipment);
    }

    function user(options) {
      var defaults = { id:        domainEntityNameNext(ENTITY_NAME_USER()),
                       name:      stringNext(),
                       email:     stringNext(),
                       avatarUrl: null,
                       status:    UserStatus.REGISTERED
                     },
          validKeys = commonFieldNames.concat(_.keys(defaults)),
          u = _.extend(defaults, commonFields(), _.pick(options || {}, validKeys));
      updateDefaultEntity(ENTITY_NAME_USER(), u);
      return u;
    }

    function defaultUser() {
      return defaultEntity(ENTITY_NAME_USER(), user);
    }

    /**
     * @param {ValueType} option.valueType the type of annotation Type to create. Valid types are: Text,
     * Number, DateTime and Select.
     *
     * @param {Int} option.maxValueCount when valueType is 'Select', use 1 for single selection or '2' for
     * multiple selection.
     */
    function annotationType(options) {
      var defaults = { uniqueId:      domainEntityNameNext(ENTITY_NAME_ANNOTATION_TYPE()),
                       name:          stringNext(),
                       description:   null,
                       valueType:     AnnotationValueType.TEXT,
                       options:       [],
                       maxValueCount: null,
                       required:      false
                     },
          validKeys = _.keys(defaults),
          at;

      options = options || {};

      if (!options.valueType) {
        options.valueType = AnnotationValueType.TEXT;
      }

      if (options.valueType === AnnotationValueType.SELECT) {
        if (_.isUndefined(options.maxValueCount)) {
          options.maxValueCount = 1;
        }

        if (_.isUndefined(options.options)) {
          options.options = _.map(_.range(2), function() {
            return domainEntityNameNext(ENTITY_NAME_ANNOTATION_TYPE());
          });
        }
      }

      at = _.extend(defaults, _.pick(options, validKeys));
      valueTypeCount += 1;
      return at;
    }

    function allAnnotationTypes() {
      var annotationTypes = _.map(_.values(AnnotationValueType), function (valueType) {
        return annotationType({ valueType: valueType });
      });
      annotationTypes.push(annotationType({
        valueType:     AnnotationValueType.SELECT,
        maxValueCount: 2,
        options:       [ 'opt1', 'opt2', 'opt3' ]
      }));
      return annotationTypes;
    }

    function collectionSpecimenSpec(options) {
      var defaults = { uniqueId:                    domainEntityNameNext(ENTITY_NAME_SPECIMEN_GROUP()),
                       name:                        stringNext(),
                       description:                 faker.lorem.sentences(4),
                       units:                       'mL',
                       anatomicalSourceType:        randomAnatomicalSourceType(),
                       preservationType:            randomPreservationType(),
                       preservationTemperatureType: randomPreservationTemperatureTypeType(),
                       specimenType:                randomSpecimenType(),
                       maxCount:                    1,
                       amount:                      0.5
                     },
          validKeys = _.keys(defaults),
          spec = _.extend(defaults, _.pick(options || {}, validKeys));
      valueTypeCount += 1;
      return spec;
    }

    /**
     * @param options.value The value for the annotation.
     */
    function annotation(options, annotationType) {
      var defaults = { annotationTypeId: null,
                       stringValue:      null,
                       numberValue:      null,
                       selectedValues:   []
                     },
          validKeys = _.keys(defaults),
          annotation = _.extend(defaults, _.pick(options || {}, validKeys));

      options = options || {};
      annotationType = annotationType || {};

      if (annotationType.uniqueId) {
        annotation.annotationTypeId = annotationType.uniqueId;
      }

      if (!_.isUndefined(options.value) && annotationType.valueType) {
        switch (annotationType.valueType) {
        case AnnotationValueType.TEXT:
        case AnnotationValueType.DATE_TIME:
          annotation.stringValue = options.value;
          break;

        case AnnotationValueType.NUMBER:
          annotation.numberValue = options.value;
          break;

        case AnnotationValueType.SELECT:
          if (options.value !== '') {
            if (annotationType.maxValueCount === 1) {
              annotation.selectedValues =  [ options.value ];
            } else if (annotationType.maxValueCount > 1) {
              annotation.selectedValues = options.value;
            } else {
              throw new Error('invalid max value count for annotation: ' + annotationType.maxValueCount);
            }
          }
          break;

        default:
          throw new Error('invalid annotation value type: ' + annotationType.valueType);
        }
      }

      return annotation;
    }

    function valueForAnnotation(annotationType) {
      switch (annotationType.valueType) {

      case AnnotationValueType.TEXT:
        return stringNext();

      case AnnotationValueType.NUMBER:
        return faker.random.number({precision: 0.05}).toString();

      case AnnotationValueType.DATE_TIME:
        // has to be in UTC format, with no seconds or milliseconds
        return moment(faker.date.past(1))
          .set({
            'millisecond': 0,
            'second':      0
          })
          .local()
          .format();

      case AnnotationValueType.SELECT:
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

    function annotations(annotationTypes) {
      return _.map(annotationTypes, function (annotationType) {
        var value = valueForAnnotation(annotationType);
        return annotation({ value: value }, annotationType);
      });
    }

    /**
     * This is a value object, so it does not have the common fields.
     */
    function location(options) {
      var defaults = { uniqueId:       domainEntityNameNext(ENTITY_NAME_LOCATION()),
                       name:           stringNext(),
                       street:         faker.address.streetAddress(),
                       city:           faker.address.city(),
                       province:       faker.address.state(),
                       postalCode:     faker.address.zipCode(),
                       poBoxNumber:    faker.address.zipCode(),
                       countryIsoCode: faker.address.country()
                     },
          validKeys = _.keys(defaults),
          at = _.extend(defaults, _.pick(options || {}, validKeys));
      valueTypeCount += 1;
      return at;
    }

    function centreLocations(centres) {
      return _.flatMap(centres, function (centre) {
        return _.map(centre.locations, function (location) {
          return {
            centreId:     centre.id,
            locationId:   location.uniqueId,
            centreName:   centre.name,
            locationName: location.name
          };
        });
      });
    }

    function pagedResult(entities) {
      return {
        items:    entities,
        page:     1,
        offset:   0,
        total:    entities.length,
        pageSize: 5,
        next:     2,
        maxPages: 4
      };
    }
  }

  /*jshint camelcase: true */

  return factory;
});
