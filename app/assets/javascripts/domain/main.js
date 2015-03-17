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
      AnnotationTypeSet                   = require('./AnnotationTypeSet'),
      AnnotationTypeViewer                = require('./AnnotationTypeViewer'),
      AnnotationValueType                 = require('./AnnotationValueType'),
      annotationTypeValidation            = require('./annotationTypeValidation'),

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
      ProcessingTypeSet                   = require('./study/ProcessingTypeSet'),
      ProcessingTypeViewer                = require('./study/ProcessingTypeViewer'),
      SpcLinkTypeViewer                   = require('./study/SpcLinkTypeViewer'),
      SpecimenGroupDataSet                = require('./study/SpecimenGroupDataSet'),
      SpecimenGroupSet                    = require('./study/SpecimenGroupSet'),
      SpecimenGroupViewer                 = require('./study/SpecimenGroupViewer'),
      SpecimenLinkAnnotationType          = require('./study/SpecimenLinkAnnotationType'),
      SpecimenLinkType                    = require('./study/SpecimenLinkType'),
      SpecimenType                        = require('./study/SpecimenType'),
      Study                               = require('./study/Study'),
      StudyAnnotationType                 = require('./study/StudyAnnotationType'),
      StudyStatus                         = require('./study/StudyStatus'),
      StudyViewer                         = require('./study/StudyViewer'),
      participantAnnotationTypeValidation = require('./study/participantAnnotationTypeValidation'),
      studyAnnotationTypeValidation       = require('./study/studyAnnotationTypeValidation'),

      User                                = require('./user/User'),
      UserStatus                          = require('./user/UserStatus'),
      UserViewer                          = require('./user/UserViewer');


  var module = angular.module('biobank.domain', []);

  module.factory('AnnotationHelper',                    AnnotationHelper);
  module.factory('AnnotationType',                      AnnotationType);
  module.factory('AnnotationTypeDataSet',               AnnotationTypeDataSet);
  module.factory('AnnotationTypeSet',                   AnnotationTypeSet);
  module.factory('AnnotationTypeViewer',                AnnotationTypeViewer);
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
  module.factory('ProcessingTypeSet',                   ProcessingTypeSet);
  module.factory('ProcessingTypeViewer',                ProcessingTypeViewer);
  module.factory('SpcLinkTypeViewer',                   SpcLinkTypeViewer);
  module.factory('SpecimenGroupDataSet',                SpecimenGroupDataSet);
  module.factory('SpecimenGroupSet',                    SpecimenGroupSet);
  module.factory('SpecimenGroupViewer',                 SpecimenGroupViewer);
  module.factory('SpecimenLinkAnnotationType',          SpecimenLinkAnnotationType);
  module.factory('SpecimenLinkType',                    SpecimenLinkType);
  module.factory('Study',                               Study);
  module.factory('StudyAnnotationType',                 StudyAnnotationType);
  module.factory('StudyViewer',                         StudyViewer);
  module.factory('User',                                User);
  module.factory('UserViewer',                          UserViewer);

  module.service('AnatomicalSourceType',                AnatomicalSourceType);
  module.service('AnnotationValueType',                 AnnotationValueType);
  module.service('CentreStatus',                        CentreStatus);
  module.service('PreservationTemperatureType',         PreservationTemperatureType);
  module.service('PreservationType',                    PreservationType);
  module.service('SpecimenType',                        SpecimenType);
  module.service('StudyStatus',                         StudyStatus);
  module.service('UserStatus',                          UserStatus);
  module.service('annotationTypeValidation',            annotationTypeValidation);
  module.service('participantAnnotationTypeValidation', participantAnnotationTypeValidation);
  module.service('studyAnnotationTypeValidation',       studyAnnotationTypeValidation);

  return module;
});
