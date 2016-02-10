/**
 * Studies configuration module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var angular = require('angular'),
      name = 'biobank.studies',
      module;

  module = angular.module(name, []);

  module.factory('StudyAnnotationTypesService',       require('./StudyAnnotationTypesService'));

  module.service('ceventAnnotationTypesService',      require('./ceventAnnotationTypesService'));
  module.service('participantAnnotationTypesService', require('./participantAnnotationTypesService'));
  module.service('specimenGroupsService',             require('./specimenGroupsService'));
  module.service('spcLinkAnnotationTypesService',     require('./spcLinkAnnotationTypesService'));
  module.service('studiesService',                    require('./studiesService'));

  return module;
});
