/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
import angular from 'angular';
import states  from './states';
import biobankCommon from '../common';
import biobankCentres from '../centres';
import biobankAdminCentres from './centres';
import biobankAdminStudies from './studies';

const MODULE_NAME = 'biobank.admin';

// users    = require('biobank.admin.users');

// 'biobank.studies'

angular
  .module(MODULE_NAME,
          [
            biobankCommon,
            biobankCentres,
            biobankAdminCentres,
            biobankAdminStudies
          ])
  .config(states)

  .controller('AnnotationTypeAddController', require('./controllers/AnnotationTypeAddController'))

  .component('annotationTypeSummary',
             require('./components/annotationTypeSummary/annotationTypeSummaryComponent'))
  .component('biobankAdmin',          require('./components/biobankAdmin/biobankAdminComponent'))
  .component('locationAdd',           require('./components/locationAdd/locationAddComponent'))
  .component('annotationTypeAdd',     require('./components/annotationTypeAdd/annotationTypeAddComponent'))
  .component('annotationTypeView',    require('./components/annotationTypeView/annotationTypeViewComponent'))

  .service('adminService',               require('./services/adminService'))
  .service('annotationTypeUpdateModal',
           require('./services/annotationTypeUpdateModal/annotationTypeUpdateModalService'))

  .factory('AnnotationTypeModals',   require('./services/AnnotationTypeModals'))
  .factory('ParticipantAnnotationTypeModals',
           require('./services/studies/ParticipantAnnotationTypeModals'))
  .factory('CollectionEventAnnotationTypeModals',
           require('./services/studies/CollectionEventAnnotationTypeModals'))
  .factory('SpecimenLinkAnnotationTypeModals',
           require('./services/studies/SpecimenLinkAnnotationTypeModals'));

export default MODULE_NAME;
