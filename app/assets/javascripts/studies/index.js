/**
 * Studies configuration module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
import angular from 'angular';

const StudiesModule = angular.module('biobank.studies', [])
  .factory('StudyAnnotationTypesService',       require('./services/StudyAnnotationTypes/StudyAnnotationTypesService'))

  .service('annotationValueTypeLabelService',   require('./services/annotationValueTypeLabelService'))
  .service('specimenGroupsService',             require('./services/specimenGroupsService/specimenGroupsService'))
  .service('spcLinkAnnotationTypesService',     require('./services/spcLinkAnnotationTypesService'))
  .service('studyStateLabelService',            require('./services/studyStateLabelService'))
  .name;

export default StudiesModule;
