/** Study service */
define(function (require) {
  'use strict';

  var angular                      = require('angular'),
      ceventAnnotationTypesService      = require('./ceventAnnotationTypesService'),
      participantsService          = require('./participantsService'),
      participantAnnotationTypesService = require('./participantAnnotationTypesService'),
      specimenGroupsService        = require('./specimenGroupsService'),
      spcLinkAnnotationTypesService     = require('./spcLinkAnnotationTypesService'),
      StudyAnnotationTypesService       = require('./StudyAnnotationTypesService'),
      studiesService               = require('./studiesService');

  var module = angular.module('biobank.studies', []);

  module.factory('StudyAnnotationTypesService',       StudyAnnotationTypesService);

  module.service('ceventAnnotationTypesService',      ceventAnnotationTypesService);
  module.service('participantAnnotationTypesService', participantAnnotationTypesService);
  module.service('specimenGroupsService',        specimenGroupsService);
  module.service('participantsService',          participantsService);
  module.service('spcLinkAnnotationTypesService',     spcLinkAnnotationTypesService);
  module.service('studiesService',               studiesService);

  return module;
});
