/**
 * Study module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
import UsersModule  from '../../../../users';
import angular      from 'angular';

const AdminStudiesAnnotationTypesModule = angular.module('admin.studies.annotationTypes', [ UsersModule ])
  .component('collectionEventAnnotationTypeAdd',
             require('./collectionEventAnnotationTypeAdd/collectionEventAnnotationTypeAddComponent'))
  .component('collectionEventAnnotationTypeView',
             require('./collectionEventAnnotationTypeView/collectionEventAnnotationTypeViewComponent'))
  .component('participantAnnotationTypeAdd',
             require('./participantAnnotationTypeAdd/participantAnnotationTypeAddComponent'))
  .component('participantAnnotationTypeView',
             require('./participantAnnotationTypeView/participantAnnotationTypeViewComponent'))
  .name;

export default AdminStudiesAnnotationTypesModule;
