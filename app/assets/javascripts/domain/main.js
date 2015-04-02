/**
 * Domain module
 *
 * Provides factories for various client side domain objects.
 */
define(function (require) {
  'use strict';

  var angular                             = require('angular'),

      AnatomicalSourceType                = require('./AnatomicalSourceType'),
      AnnotationHelper                    = require('./AnnotationHelper'),
      AnnotationType                      = require('./AnnotationType'),
      AnnotationTypeDataSet               = require('./AnnotationTypeDataSet'),
      AnnotationTypeViewer                = require('./AnnotationTypeViewer'),
      AnnotationValueType                 = require('./AnnotationValueType'),
      AnnotationMaxValueCount             = require('./AnnotationMaxValueCount'),
      annotationTypeValidation            = require('./annotationTypeValidation'),
      domainEntityService                 = require('./domainEntityService'),

      Centre                              = require('./centre/Centre'),
      CentreCounts                        = require('./centre/CentreCounts'),
      CentreStatus                        = require('./centre/CentreStatus'),

      ConcurrencySafeEntity               = require('./ConcurrencySafeEntity'),
      EntityViewer                        = require('./EntityViewer'),
      Location                            = require('./Location'),
      LocationViewer                      = require('./LocationViewer'),
      PreservationTemperatureType         = require('./PreservationTemperatureType'),
      PreservationType                    = require('./PreservationType'),

      CeventTypeViewer                    = require('./study/CeventTypeViewer'),
      CollectionEventAnnotationType       = require('./study/CollectionEventAnnotationType'),
      CollectionEventType                 = require('./study/CollectionEventType'),
      Participant                         = require('./study/Participant'),
      ParticipantAnnotationType           = require('./study/ParticipantAnnotationType'),
      ProcessingType                      = require('./study/ProcessingType'),
      ProcessingTypeViewer                = require('./study/ProcessingTypeViewer'),
      SpcLinkTypeViewer                   = require('./study/SpcLinkTypeViewer'),
      SpecimenGroup                       = require('./study/SpecimenGroup'),
      SpecimenGroupDataSet                = require('./study/SpecimenGroupDataSet'),
      SpecimenGroupViewer                 = require('./study/SpecimenGroupViewer'),
      SpecimenLinkAnnotationType          = require('./study/SpecimenLinkAnnotationType'),
      SpecimenLinkType                    = require('./study/SpecimenLinkType'),
      SpecimenType                        = require('./study/SpecimenType'),
      Study                               = require('./study/Study'),
      StudyCounts                         = require('./study/StudyCounts'),
      StudyAnnotationType                 = require('./study/StudyAnnotationType'),
      StudyStatus                         = require('./study/StudyStatus'),
      StudyViewer                         = require('./study/StudyViewer'),
      participantAnnotationTypeValidation = require('./study/participantAnnotationTypeValidation'),
      studyAnnotationTypeValidation       = require('./study/studyAnnotationTypeValidation'),

      User                                = require('./user/User'),
      UserCounts                          = require('./user/UserCounts'),
      UserStatus                          = require('./user/UserStatus'),
      UserViewer                          = require('./user/UserViewer');


  var module = angular.module('biobank.domain', []);

  module.factory('AnnotationHelper',                    AnnotationHelper);
  module.factory('AnnotationType',                      AnnotationType);
  module.factory('AnnotationTypeDataSet',               AnnotationTypeDataSet);
  module.factory('AnnotationTypeViewer',                AnnotationTypeViewer);
  module.service('domainEntityService',                 domainEntityService);

  module.factory('Centre',                              Centre);
  module.factory('CentreCounts',                        CentreCounts);
  module.factory('CeventTypeViewer',                    CeventTypeViewer);
  module.factory('CollectionEventAnnotationType',       CollectionEventAnnotationType);
  module.factory('CollectionEventType',                 CollectionEventType);
  module.factory('ConcurrencySafeEntity',               ConcurrencySafeEntity);
  module.factory('EntityViewer',                        EntityViewer);
  module.factory('Location',                            Location);
  module.factory('LocationViewer',                      LocationViewer);
  module.factory('Participant',                         Participant);
  module.factory('ParticipantAnnotationType',           ParticipantAnnotationType);
  module.factory('ProcessingTypeViewer',                ProcessingTypeViewer);
  module.factory('SpcLinkTypeViewer',                   SpcLinkTypeViewer);
  module.factory('SpecimenGroup',                       SpecimenGroup);
  module.factory('SpecimenGroupDataSet',                SpecimenGroupDataSet);
  module.factory('SpecimenGroupViewer',                 SpecimenGroupViewer);
  module.factory('SpecimenLinkAnnotationType',          SpecimenLinkAnnotationType);
  module.factory('SpecimenLinkType',                    SpecimenLinkType);

  module.factory('Study',                               Study);
  module.factory('StudyCounts',                         StudyCounts);
  module.factory('StudyAnnotationType',                 StudyAnnotationType);
  module.factory('StudyViewer',                         StudyViewer);

  module.service('AnatomicalSourceType',                AnatomicalSourceType);
  module.service('AnnotationValueType',                 AnnotationValueType);
  module.service('AnnotationMaxValueCount',             AnnotationMaxValueCount);
  module.service('CentreStatus',                        CentreStatus);
  module.service('PreservationTemperatureType',         PreservationTemperatureType);
  module.service('PreservationType',                    PreservationType);
  module.service('ProcessingType',                      ProcessingType);
  module.service('SpecimenType',                        SpecimenType);
  module.service('StudyStatus',                         StudyStatus);
  module.service('annotationTypeValidation',            annotationTypeValidation);
  module.service('participantAnnotationTypeValidation', participantAnnotationTypeValidation);
  module.service('studyAnnotationTypeValidation',       studyAnnotationTypeValidation);

  module.factory('User',                                User);
  module.factory('UserViewer',                          UserViewer);
  module.service('UserCounts',                          UserCounts);
  module.service('UserStatus',                          UserStatus);

  return module;
});
