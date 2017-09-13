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

  var angular = require('angular');

  return angular.module('biobank.domain', [])

    .factory('AnnotationTypeViewer',                require('./AnnotationTypeViewer'))
    .factory('ConcurrencySafeEntity',               require('./ConcurrencySafeEntity'))
    .factory('DomainEntity',                        require('./DomainEntity'))
    .factory('DomainError',                         require('./DomainError'))
    .factory('EntityName',                          require('./EntityName'))
    .factory('EntityViewer',                        require('./EntityViewer'))
    .factory('Location',                            require('./Location'))

    .constant('AnatomicalSourceType',               require('./AnatomicalSourceType'))
    .constant('PreservationType',                   require('./PreservationType'))
    .constant('PreservationTemperatureType',        require('./PreservationTemperatureType'))
    .constant('ShipmentState',                      require('./centre/ShipmentState'))
    .constant('ShipmentItemState',                  require('./centre/ShipmentItemState'))
    .constant('centreLocationInfoSchema',           require('./centre/centreLocationInfoSchema'))
    .constant('SpecimenState',                      require('./participants/SpecimenState'))

    .constant('AnnotationValueType',                require('./AnnotationValueType'))
    .constant('AnnotationMaxValueCount',            require('./annotations/AnnotationMaxValueCount'))
    .constant('SpecimenType',                       require('./study/SpecimenType'))

    .factory('EntityInfo',                          require('./access/EntityInfo'))
    .factory('EntitySet',                           require('./access/EntitySet'))
    .factory('MembershipBase',                      require('./access/MembershipBase'))
    .factory('Membership',                          require('./access/Membership'))
    .factory('UserMembership',                      require('./access/UserMembership'))

    .factory('Annotation',                          require('./annotations/Annotation'))
    .factory('AnnotationType',                      require('./annotations/AnnotationType'))
    .factory('DateTimeAnnotation',                  require('./annotations/DateTimeAnnotation'))
    .factory('MultipleSelectAnnotation',            require('./annotations/MultipleSelectAnnotation'))
    .factory('NumberAnnotation',                    require('./annotations/NumberAnnotation'))
    .factory('SingleSelectAnnotation',              require('./annotations/SingleSelectAnnotation'))
    .factory('TextAnnotation',                      require('./annotations/TextAnnotation'))
    .factory('HasAnnotationTypes',                  require('./annotations/HasAnnotationTypes'))
    .factory('HasAnnotations',                      require('./annotations/HasAnnotations'))
    .service('annotationFactory',                   require('./annotations/annotationFactory'))
    .service('annotationTypeValidation',            require('./annotations/annotationTypeValidation'))

    .factory('Centre',                              require('./centre/Centre'))
    .factory('CentreName',                          require('./centre/CentreName'))
    .factory('CentreCounts',                        require('./centre/CentreCounts'))
    .factory('Shipment',                            require('./centre/Shipment'))
    .factory('ShipmentSpecimen',                    require('./centre/ShipmentSpecimen'))

    .factory('CollectionEventType',                 require('./study/CollectionEventType'))
    .factory('CollectionSpecimenDescription',       require('./study/CollectionSpecimenDescription'))
    .factory('HasCollectionSpecimenDescriptions',   require('./study/HasCollectionSpecimenDescriptions'))
    .factory('ProcessingTypeViewer',                require('./study/ProcessingTypeViewer'))
    .factory('SpcLinkTypeViewer',                   require('./study/SpcLinkTypeViewer'))
    .factory('SpecimenGroup',                       require('./study/SpecimenGroup'))
    .factory('SpecimenGroupViewer',                 require('./study/SpecimenGroupViewer'))
    .factory('SpecimenLinkAnnotationType',          require('./study/SpecimenLinkAnnotationType'))
    .factory('SpecimenLinkType',                    require('./study/SpecimenLinkType'))
    .factory('Study',                               require('./study/Study'))
    .factory('StudyName',                           require('./study/StudyName'))
    .factory('StudyCounts',                         require('./study/StudyCounts'))
    .factory('StudyAnnotationType',                 require('./study/StudyAnnotationType'))
    .factory('StudyViewer',                         require('./study/StudyViewer'))
    .service('ProcessingDto',                       require('./study/ProcessingDto'))
    .service('ProcessingType',                      require('./study/ProcessingType'))
    .service('studyAnnotationTypeValidation',       require('./study/studyAnnotationTypeValidation'))

    .factory('Participant',                         require('./participants/Participant'))
    .factory('CollectionEvent',                     require('./participants/CollectionEvent'))
    .factory('Specimen',                            require('./participants/Specimen'))

    .factory('User',                                require('./user/User'))
    .factory('UserName',                            require('./user/UserName'))
    .factory('UserViewer',                          require('./user/UserViewer'))
    .service('UserCounts',                          require('./user/UserCounts'))

    .factory('SearchFilter',                        require('./filters/SearchFilter'))
    .factory('EmailFilter',                         require('./filters/EmailFilter'))
    .factory('NameFilter',                          require('./filters/NameFilter'))
    .factory('StateFilter',                         require('./filters/StateFilter'))

    .constant('UserState',                         require('./user/UserState'))
    .constant('StudyState',                        require('./study/StudyState'))
    .constant('CentreState',                       require('./centre/CentreState'));
});
