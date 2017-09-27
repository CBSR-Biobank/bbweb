/**
 * Studies configuration module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
import angular from 'angular';

const MODULE_NAME = 'biobank.studies';

angular.module(MODULE_NAME, [])
  .factory('StudyAnnotationTypesService',       require('./StudyAnnotationTypesService'))

  .service('annotationValueTypeLabelService',   require('./services/annotationValueTypeLabelService'))
  .service('specimenGroupsService',             require('./services/specimenGroupsService'))
  .service('spcLinkAnnotationTypesService',     require('./services/spcLinkAnnotationTypesService'))
  .service('studyStateLabelService',            require('./services/studyStateLabelService'));

export default MODULE_NAME;
