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

  module = angular.module(name, [])
    .factory('StudyAnnotationTypesService',       require('./StudyAnnotationTypesService'))

    .service('annotationValueTypeLabelService',   require('./services/annotationValueTypeLabelService'))
    .service('specimenGroupsService',             require('./services/specimenGroupsService'))
    .service('spcLinkAnnotationTypesService',     require('./services/spcLinkAnnotationTypesService'))
    .service('studyStateLabelService',            require('./services/studyStateLabelService'));

  return module;
});
