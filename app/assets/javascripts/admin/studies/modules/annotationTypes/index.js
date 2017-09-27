/**
 * Study module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
import angular      from 'angular';
import biobankUsers from '../../../../users';

const MODULE_NAME = 'admin.studies.components.annotationTypes';

angular
  .module(MODULE_NAME, [ biobankUsers ])
  .component('collectionEventAnnotationTypeAdd',
             require('./collectionEventAnnotationTypeAdd/collectionEventAnnotationTypeAddComponent'))
  .component('collectionEventAnnotationTypeView',
             require('./collectionEventAnnotationTypeView/collectionEventAnnotationTypeViewComponent'))
  .component('participantAnnotationTypeAdd',
             require('./participantAnnotationTypeAdd/participantAnnotationTypeAddComponent'))
  .component('participantAnnotationTypeView',
             require('./participantAnnotationTypeView/participantAnnotationTypeViewComponent'));

export default MODULE_NAME;
