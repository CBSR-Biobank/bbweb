/** Study service */
define(function (require) {
  'use strict';

  var angular                      = require('angular'),
      ceventAnnotTypesService      = require('./ceventAnnotTypesService'),
      participantsService          = require('./participantsService'),
      participantAnnotTypesService = require('./participantAnnotTypesService'),
      specimenGroupsService        = require('./specimenGroupsService'),
      spcLinkAnnotTypesService     = require('./spcLinkAnnotTypesService'),
      StudyAnnotTypesService       = require('./StudyAnnotTypesService'),
      studiesService               = require('./studiesService');

  var module = angular.module('biobank.studies', []);

  module.factory('StudyAnnotTypesService',       StudyAnnotTypesService);

  module.service('ceventAnnotTypesService',      ceventAnnotTypesService);
  module.service('participantAnnotTypesService', participantAnnotTypesService);
  module.service('specimenGroupsService',        specimenGroupsService);
  module.service('participantsService',          participantsService);
  module.service('spcLinkAnnotTypesService',     spcLinkAnnotTypesService);
  module.service('studiesService',               studiesService);

  return module;
});
