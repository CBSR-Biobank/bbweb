/**
 * Domain module
 *
 * Provides factories for various client side domain objects.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var angular = require('angular'),
      name = 'biobank.domain',
      module;

  module = angular.module('biobank.domain', []);

  module.factory('AnnotationTypeViewer',                require('./AnnotationTypeViewer'));
  module.factory('ConcurrencySafeEntity',               require('./ConcurrencySafeEntity'));
  module.service('domainEntityService',                 require('./domainEntityService') );
  module.factory('DomainError',                         require('./DomainError'));
  module.factory('EntityViewer',                        require('./EntityViewer'));
  module.factory('Location',                            require('./Location'));
  module.factory('LocationViewer',                      require('./LocationViewer'));

  module.constant('AnatomicalSourceType',               require('./AnatomicalSourceType'));
  module.constant('PreservationType',                   require('./PreservationType'));
  module.constant('PreservationTemperatureType',        require('./PreservationTemperatureType'));
  module.constant('ShipmentState',                      require('./centre/ShipmentState'));
  module.constant('ShipmentItemState',                  require('./centre/ShipmentItemState'));

  module.constant('AnnotationValueType',                require('./AnnotationValueType'));
  module.constant('SpecimenType',                       require('./study/SpecimenType'));

  module.factory('Annotation',                          require('./annotations/Annotation'));
  module.factory('AnnotationType',                      require('./annotations/AnnotationType'));
  module.factory('DateTimeAnnotation',                  require('./annotations/DateTimeAnnotation'));
  module.factory('MultipleSelectAnnotation',            require('./annotations/MultipleSelectAnnotation'));
  module.factory('NumberAnnotation',                    require('./annotations/NumberAnnotation'));
  module.factory('SingleSelectAnnotation',              require('./annotations/SingleSelectAnnotation'));
  module.factory('TextAnnotation',                      require('./annotations/TextAnnotation'));
  module.factory('hasAnnotationTypes',                  require('./annotations/hasAnnotationTypes'));
  module.factory('hasAnnotations',                      require('./annotations/hasAnnotations'));
  module.service('AnnotationMaxValueCount',             require('./annotations/AnnotationMaxValueCount'));
  module.service('annotationFactory',                   require('./annotations/annotationFactory'));
  module.service('annotationTypeValidation',            require('./annotations/annotationTypeValidation'));

  module.factory('Centre',                              require('./centre/Centre'));
  module.factory('CentreCounts',                        require('./centre/CentreCounts'));
  module.factory('Shipment',                            require('./centre/Shipment'));
  module.factory('ShipmentSpecimen',                    require('./centre/ShipmentSpecimen'));

  module.factory('CollectionEventType',                 require('./study/CollectionEventType'));
  module.factory('CollectionSpecimenSpec',              require('./study/CollectionSpecimenSpec'));
  module.factory('hasCollectionSpecimenSpecs',          require('./study/hasCollectionSpecimenSpecs'));
  module.factory('ProcessingTypeViewer',                require('./study/ProcessingTypeViewer'));
  module.factory('SpcLinkTypeViewer',                   require('./study/SpcLinkTypeViewer'));
  module.factory('SpecimenGroup',                       require('./study/SpecimenGroup'));
  module.factory('SpecimenGroupViewer',                 require('./study/SpecimenGroupViewer'));
  module.factory('SpecimenLinkAnnotationType',          require('./study/SpecimenLinkAnnotationType'));
  module.factory('SpecimenLinkType',                    require('./study/SpecimenLinkType'));
  module.factory('Study',                               require('./study/Study'));
  module.factory('StudyCounts',                         require('./study/StudyCounts'));
  module.factory('StudyAnnotationType',                 require('./study/StudyAnnotationType'));
  module.factory('StudyViewer',                         require('./study/StudyViewer'));
  module.service('ProcessingDto',                       require('./study/ProcessingDto'));
  module.service('ProcessingType',                      require('./study/ProcessingType'));
  module.service('studyAnnotationTypeValidation',       require('./study/studyAnnotationTypeValidation'));

  module.factory('Participant',                         require('./participants/Participant'));
  module.factory('CollectionEvent',                     require('./participants/CollectionEvent'));
  module.factory('Specimen',                            require('./participants/Specimen'));

  module.factory('User',                                require('./user/User'));
  module.factory('UserViewer',                          require('./user/UserViewer'));
  module.service('UserCounts',                          require('./user/UserCounts'));

  module.constant('UserStatus',                         require('./user/UserStatus'));
  module.constant('StudyStatus',                        require('./study/StudyStatus'));
  module.constant('CentreStatus',                       require('./centre/CentreStatus'));

  return {
    name: name,
    module: module
  };});
