/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
import AdminCentresModule from './centres';
import AdminStudiesModule from './studies';
import AdminUsersModule   from './users';
import CentresModule      from '../centres';
import CommonModule       from '../common';
import angular            from 'angular';
import states             from './states';

const AdminModule = angular.module('biobank.admin',
                                   [
                                     CommonModule,
                                     CentresModule,
                                     AdminCentresModule,
                                     AdminStudiesModule,
                                     AdminUsersModule
                                   ])
      .config(states)

      .controller('AnnotationTypeAddController', require('./controllers/AnnotationTypeAddController'))

      .component('annotationTypeSummary', require('./components/annotationTypeSummary/annotationTypeSummaryComponent'))
      .component('biobankAdmin',          require('./components/biobankAdmin/biobankAdminComponent'))
      .component('locationAdd',           require('./components/locationAdd/locationAddComponent'))
      .component('annotationTypeAdd',     require('./components/annotationTypeAdd/annotationTypeAddComponent'))
      .component('annotationTypeView',    require('./components/annotationTypeView/annotationTypeViewComponent'))

      .service('adminService',               require('./services/adminService/adminService'))
      .service('annotationTypeUpdateModal',
               require('./services/annotationTypeUpdateModal/annotationTypeUpdateModalService'))

      .factory('AnnotationTypeModals',   require('./services/AnnotationTypeModals'))
      .factory('ParticipantAnnotationTypeModals',
               require('./services/studies/ParticipantAnnotationTypeModals'))
      .factory('CollectionEventAnnotationTypeModals',
               require('./services/studies/CollectionEventAnnotationTypeModals'))
      .factory('SpecimenLinkAnnotationTypeModals',
               require('./services/studies/SpecimenLinkAnnotationTypeModals'))
      .name;

export default AdminModule;
