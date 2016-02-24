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

  module.factory('AnnotationType',                      require('./AnnotationType'));
  module.factory('AnnotationTypes',                     require('./AnnotationTypes'));
  module.factory('AnnotationTypeViewer',                require('./AnnotationTypeViewer'));
  module.factory('ConcurrencySafeEntity',               require('./ConcurrencySafeEntity'));
  module.service('domainEntityService',                 require('./domainEntityService') );
  module.factory('EntityViewer',                        require('./EntityViewer'));
  module.factory('Location',                            require('./Location'));
  module.factory('LocationViewer',                      require('./LocationViewer'));

  module.service('AnatomicalSourceType',                require('./AnatomicalSourceType'));
  module.service('AnnotationValueType',                 require('./AnnotationValueType'));
  module.service('AnnotationMaxValueCount',             require('./AnnotationMaxValueCount'));
  module.service('CentreStatus',                        require('./centre/CentreStatus'));
  module.service('PreservationTemperatureType',         require('./PreservationTemperatureType'));
  module.service('PreservationType',                    require('./PreservationType'));
  module.service('annotationTypeValidation',            require('./annotationTypeValidation'));

  module.factory('Annotation',                          require('./annotations/Annotation'));
  module.factory('DateTimeAnnotation',                  require('./annotations/DateTimeAnnotation'));
  module.factory('MultipleSelectAnnotation',            require('./annotations/MultipleSelectAnnotation'));
  module.factory('NumberAnnotation',                    require('./annotations/NumberAnnotation'));
  module.factory('SingleSelectAnnotation',              require('./annotations/SingleSelectAnnotation'));
  module.factory('TextAnnotation',                      require('./annotations/TextAnnotation'));
  module.service('annotationFactory',                   require('./annotations/AnnotationFactory'));

  module.factory('Centre',                              require('./centre/Centre'));
  module.factory('CentreCounts',                        require('./centre/CentreCounts'));

  module.factory('CeventTypeViewer',                    require('./study/CeventTypeViewer'));
  module.factory('CollectionDto',                       require('./study/CollectionDto'));
  module.factory('CollectionEventType',                 require('./study/CollectionEventType'));
  module.factory('CollectionSpecimenSpec',              require('./study/CollectionSpecimenSpec'));
  module.factory('CollectionSpecimenSpecs',             require('./study/CollectionSpecimenSpecs'));
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
  module.service('SpecimenType',                        require('./study/SpecimenType'));
  module.service('StudyStatus',                         require('./study/StudyStatus'));
  module.service('participantAnnotationTypeValidation', require('./study/participantAnnotationTypeValidation'));
  module.service('studyAnnotationTypeValidation',       require('./study/studyAnnotationTypeValidation'));

  module.factory('Participant',                         require('./participants/Participant'));
  module.factory('CollectionEvent',                     require('./participants/CollectionEvent'));

  module.factory('User',                                require('./user/User'));
  module.factory('UserViewer',                          require('./user/UserViewer'));
  module.service('UserCounts',                          require('./user/UserCounts'));
  module.service('UserStatus',                          require('./user/UserStatus'));

  return {
    name: name,
    module: module
  };});
