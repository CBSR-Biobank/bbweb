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
  module.factory('EntityViewer',                        require('./EntityViewer'));
  module.factory('Location',                            require('./Location'));
  module.factory('LocationViewer',                      require('./LocationViewer'));

  module.service('AnatomicalSourceType',                require('./AnatomicalSourceType'));
  module.service('AnnotationValueType',                 require('./AnnotationValueType'));
  module.service('CentreStatus',                        require('./centre/CentreStatus'));
  module.service('PreservationTemperatureType',         require('./PreservationTemperatureType'));
  module.service('PreservationType',                    require('./PreservationType'));

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
  module.service('SpecimenType',                        require('./study/SpecimenType'));
  module.service('StudyStatus',                         require('./study/StudyStatus'));
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
