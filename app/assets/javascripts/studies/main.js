/** Study service */
define(function (require) {
  'use strict';

  var angular                      = require('angular'),
      ceventAnnotTypesService      = require('./ceventAnnotTypesService'),
      participantsService          = require('./participantsService'),
      participantAnnotTypesService = require('./participantAnnotTypesService'),
      processingTypesService       = require('./processingTypesService'),
      spcLinkAnnotTypesService     = require('./spcLinkAnnotTypesService'),
      specimenGroupsService        = require('./specimenGroupsService'),
      StudyAnnotTypesService       = require('./StudyAnnotTypesService'),
      studiesService               = require('./studiesService');

  var module = angular.module('biobank.studies', []);

  module.factory('StudyAnnotTypesService',       StudyAnnotTypesService);

  module.service('ceventAnnotTypesService',      ceventAnnotTypesService);
  module.service('participantAnnotTypesService', participantAnnotTypesService);
  module.service('participantsService',          participantsService);
  module.service('processingTypesService',       processingTypesService);
  module.service('spcLinkAnnotTypesService',     spcLinkAnnotTypesService);
  module.service('specimenGroupsService',        specimenGroupsService);
  module.service('studiesService',               studiesService);

  return module;
});
