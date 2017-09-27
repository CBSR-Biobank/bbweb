/**
 * Domain module
 *
 * Provides factories for various client side domain objects.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
import angular from 'angular';

const MODULE_NAME = 'biobank.domain';

angular.module(MODULE_NAME, [])

  .factory('AnnotationTypeViewer',                require('./AnnotationTypeViewer/AnnotationTypeViewer.js'))
  .factory('ConcurrencySafeEntity',               require('./ConcurrencySafeEntity'))
  .factory('DomainEntity',                        require('./DomainEntity'))
  .factory('DomainError',                         require('./DomainError'))
  .factory('EntityName',                          require('./EntityName'))
  .factory('EntityViewer',                        require('./EntityViewer'))
  .factory('Location',                            require('./Location'))

  .constant('AnnotationMaxValueCount',            require('./annotations/AnnotationMaxValueCount'))

  .constant('ShipmentItemState',                  require('./centre/ShipmentItemState'))
  .constant('centreLocationInfoSchema',           require('./centre/centreLocationInfoSchema'))
  .constant('SpecimenState',                      require('./participants/SpecimenState'))

  .constant('AnatomicalSourceType',               require('./AnatomicalSourceType/AnatomicalSourceType'))
  .constant('PreservationType',                   require('./PreservationType/PreservationType'))
  .constant('PreservationTemperatureType',        require('./PreservationTemperatureType/PreservationTemperatureType'))
  .constant('ShipmentState',                      require('./centre/ShipmentState/ShipmentState'))

  .constant('AnnotationValueType',                require('./AnnotationValueType/AnnotationValueType'))
  .constant('SpecimenType',                       require('./study/SpecimenType'))

  .factory('EntityInfo',                          require('./access/EntityInfo'))
  .factory('EntitySet',                           require('./access/EntitySet/EntitySet'))
  .factory('MembershipBase',                      require('./access/MembershipBase/MembershipBase'))
  .factory('Membership',                          require('./access/Membership/Membership'))
  .factory('UserMembership',                      require('./access/UserMembership'))

  .factory('AnnotationType',                      require('./annotations/AnnotationType'))
  .factory('HasAnnotations',                      require('./annotations/HasAnnotations'))
  .factory('HasAnnotationTypes',                  require('./annotations/HasAnnotationTypes'))
  .factory('NumberAnnotation',                    require('./annotations/NumberAnnotation'))
  .factory('SingleSelectAnnotation',              require('./annotations/SingleSelectAnnotation'))
  .factory('TextAnnotation',                      require('./annotations/TextAnnotation'))
  .factory('Annotation',                          require('./annotations/Annotation/Annotation.js'))
  .factory('DateTimeAnnotation',                  require('./annotations/DateTimeAnnotation/DateTimeAnnotation'))
  .factory('MultipleSelectAnnotation',            require('./annotations/MultipleSelectAnnotation/MultipleSelectAnnotation'))
  .service('annotationFactory',                   require('./annotations/annotationFactory/annotationFactory'))
  .service('annotationTypeValidation',            require('./annotations/annotationTypeValidation'))

  .factory('Centre',                              require('./centre/Centre/Centre'))
  .factory('CentreName',                          require('./centre/CentreName/CentreName'))
  .factory('CentreCounts',                        require('./centre/CentreCounts/CentreCounts'))
  .factory('Shipment',                            require('./centre/Shipment/Shipment'))
  .factory('ShipmentSpecimen',                    require('./centre/ShipmentSpecimen/ShipmentSpecimen'))

  .factory('CollectionEventType',                 require('./study/CollectionEventType/CollectionEventType'))
  .factory('CollectionSpecimenDescription',       require('./study/CollectionSpecimenDescription'))
  .factory('HasCollectionSpecimenDescriptions',   require('./study/HasCollectionSpecimenDescriptions'))
  .factory('ProcessingTypeViewer',                require('./study/ProcessingTypeViewer'))
  .factory('SpcLinkTypeViewer',                   require('./study/SpcLinkTypeViewer/SpcLinkTypeViewer'))
  .factory('SpecimenGroup',                       require('./study/SpecimenGroup/SpecimenGroup'))
  .factory('SpecimenGroupViewer',                 require('./study/SpecimenGroupViewer'))
  .factory('SpecimenLinkAnnotationType',          require('./study/SpecimenLinkAnnotationType/SpecimenLinkAnnotationType'))
  .factory('SpecimenLinkType',                    require('./study/SpecimenLinkType/SpecimenLinkType'))
  .factory('Study',                               require('./study/Study/Study'))
  .factory('StudyName',                           require('./study/StudyName/StudyName'))
  .factory('StudyCounts',                         require('./study/StudyCounts/StudyCounts'))
  .factory('StudyAnnotationType',                 require('./study/StudyAnnotationType/StudyAnnotationType'))
  .factory('StudyViewer',                         require('./study/StudyViewer'))
  .service('ProcessingDto',                       require('./study/ProcessingDto/ProcessingDto'))
  .service('ProcessingType',                      require('./study/ProcessingType/ProcessingType'))
  .service('studyAnnotationTypeValidation',       require('./study/studyAnnotationTypeValidation'))

  .factory('Participant',                         require('./participants/Participant/Participant'))
  .factory('CollectionEvent',                     require('./participants/CollectionEvent/CollectionEvent'))
  .factory('Specimen',                            require('./participants/Specimen/Specimen'))

  .factory('User',                                require('./user/User/User'))
  .factory('UserName',                            require('./user/UserName/UserName'))
  .factory('UserViewer',                          require('./user/UserViewer'))
  .service('UserCounts',                          require('./user/UserCounts/UserCounts'))

  .factory('SearchFilter',                        require('./filters/SearchFilter'))
  .factory('EmailFilter',                         require('./filters/EmailFilter'))
  .factory('NameFilter',                          require('./filters/NameFilter'))
  .factory('StateFilter',                         require('./filters/StateFilter'))

  .constant('UserState',                         require('./user/UserState'))
  .constant('StudyState',                        require('./study/StudyState'))
  .constant('CentreState',                       require('./centre/CentreState'));

export default MODULE_NAME;
