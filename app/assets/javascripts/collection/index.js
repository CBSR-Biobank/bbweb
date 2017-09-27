/**
 * The Specimen collection module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
import UsersModule from '../users';
import angular     from 'angular';

const CollectionModule = angular.module('biobank.collection', [ UsersModule ])
      .config(require('./states'))

      .component('ceventSpecimensView',
                 require('./components/ceventSpecimensView/ceventSpecimensViewComponent'))

      .component('specimenView',        require('./components/specimenView/specimenViewComponent'))
      .component('collection',          require('./components/collection/collectionComponent'))
      .component('ceventAdd',           require('./components/ceventAdd/ceventAddComponent'))
      .component('ceventGetType',       require('./components/ceventGetType/ceventGetTypeComponent'))
      .component('ceventView',          require('./components/ceventView/ceventViewComponent'))
      .component('ceventsList',         require('./components/ceventsList/ceventsListComponent'))
      .component('selectStudy',         require('./components/selectStudy/selectStudyComponent'))
      .component('participantAdd',      require('./components/participantAdd/participantAddComponent'))
      .component('participantGet',      require('./components/participantGet/participantGetComponent'))
      .component('participantView',     require('./components/participantView/participantViewComponent'))
      .component('participantSummary',
                 require('./components/participantSummary/participantSummaryComponent'))
      .component('ceventsAddAndSelect',
                 require('./components/ceventsAddAndSelect/ceventsAddAndSelectComponent'))

      .service('specimenAddModal',          require('./services/specimenAddModal/specimenAddModalService'))
      .service('specimenStateLabelService', require('./services/specimenStateLabelService'))
      .name;

export default CollectionModule;
